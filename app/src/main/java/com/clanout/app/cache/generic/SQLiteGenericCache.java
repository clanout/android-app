package com.clanout.app.cache.generic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache.core.DatabaseManager;
import com.clanout.app.cache.core.SQLiteCacheContract;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteGenericCache implements GenericCache
{
    private static final String TAG = "GenericCache";

    private static SQLiteGenericCache instance;

    public static SQLiteGenericCache getInstance()
    {
        if (instance == null)
        {
            instance = new SQLiteGenericCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;
    private Map<String, String> memoryStore;

    private SQLiteGenericCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
        memoryStore = new HashMap<>();
        Timber.d("SQLiteGenericCache initialized");
    }

    @Override
    public void put(final String key, final String value)
    {
        memoryStore.put(key, value);
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("GeneticCache.put() on thread = " + Thread.currentThread()
                                                                               .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Generic.SQL_DELETE);
                            statement.bindString(1, key);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            statement = db.compileStatement(SQLiteCacheContract.Generic.SQL_INSERT);
                            statement.bindString(1, key);
                            statement.bindString(2, value);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            db.setTransactionSuccessful();
                            db.endTransaction();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("[Insert] " + key + " = " + value);
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Insert failed for key = " + key + " [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public String get(String key)
    {
        String value = memoryStore.get(key);
        if (value == null)
        {
            try
            {
                SQLiteDatabase db = databaseManager.openConnection();

                String[] projection = {SQLiteCacheContract.Generic.COLUMN_VALUE};
                String selection = SQLiteCacheContract.Generic.COLUMN_KEY + " = ?";
                String[] selectionArgs = {key};

                Cursor cursor = db
                        .query(SQLiteCacheContract.Generic.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();

                try
                {
                    value = cursor.getString(0);
                }
                catch (Exception e)
                {
                    Timber.d("Unable to find '" + key + "' in cache");
                }

                cursor.close();
                databaseManager.closeConnection();
            }
            catch (Exception e)
            {
                Timber.e("Error while reading key = " + key + " from cache [" + e
                        .getMessage() + "]");
            }
        }
        return value;
    }

    @Override
    public void delete(final String key)
    {
        memoryStore.remove(key);
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("GeneticCache.remove() on thread = " + Thread.currentThread()
                                                                                  .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Generic.SQL_DELETE);
                            statement.bindString(1, key);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("[Deleted] " + key);
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Delete failed for key = " + key + " [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void put(String key, Object value)
    {
        put(key, gson.toJson(value));
    }

    @Override
    public <T> T get(String key, Class<T> type)
    {
        String valueJson = get(key);
        T value = null;
        try
        {
            value = gson.fromJson(valueJson, type);
        }
        catch (Exception e)
        {
            Timber.e("Unable to parse json into object for key = " + key + " [" + e
                    .getMessage() + "]");
            e.printStackTrace();
            value = null;
        }
        return value;
    }

    @Override
    public void deleteAll()
    {
        memoryStore = new HashMap<>();
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("GeneticCache.deleteAll() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Generic.SQL_DELETE_ALL);
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("[Cleared Generic Cache]");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to clear generic cache " + "[" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }
}
