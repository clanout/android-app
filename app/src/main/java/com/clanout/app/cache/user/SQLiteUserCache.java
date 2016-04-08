package com.clanout.app.cache.user;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache._core.DatabaseManager;
import com.clanout.app.cache._core.SQLiteCacheContract;
import com.clanout.app.model.Friend;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteUserCache implements UserCache
{
    private static final String TAG = "UserCache";
    private static final String SYNCHRONIZATION_TAG = "thread_save_friends";

    private static SQLiteUserCache instance;

    public static SQLiteUserCache getInstance()
    {
        if (instance == null)
        {
            instance = new SQLiteUserCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;

    private SQLiteUserCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
        Timber.d("SQLiteUserCache initialized");
    }

    @Override
    public Observable<List<Friend>> getFriends()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Friend>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Friend>> subscriber)
                    {
                        List<Friend> friends = new ArrayList<Friend>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {SQLiteCacheContract.FacebookFriends.COLUMN_CONTENT};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.FacebookFriends.TABLE_NAME, projection, null, null, null, null, null);
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast())
                        {
                            String friendJson = cursor.getString(0);

                            Friend friend = gson.fromJson(friendJson, Friend.class);
                            friends.add(friend);

                            cursor.moveToNext();
                        }

                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(friends);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Friend>> getContacts()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Friend>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Friend>> subscriber)
                    {
                        List<Friend> contacts = new ArrayList<Friend>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {SQLiteCacheContract.PhoneContacts.COLUMN_CONTENT};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.PhoneContacts.TABLE_NAME, projection, null, null, null, null, null);
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast())
                        {
                            String contactJson = cursor.getString(0);

                            Friend contact = gson.fromJson(contactJson, Friend.class);
                            contacts.add(contact);

                            cursor.moveToNext();
                        }

                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(contacts);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void saveFriends(final List<Friend> friends)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (SYNCHRONIZATION_TAG)
                        {
                            Timber.v("UserCache.saveFriends() on thread = " + Thread.currentThread()
                                                                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.FacebookFriends.SQL_DELETE);
                            statement.execute();
                            statement.close();

                            if (!friends.isEmpty())
                            {
                                statement = db
                                        .compileStatement(SQLiteCacheContract.FacebookFriends.SQL_INSERT);
                                for (Friend friend : friends)
                                {
                                    statement.bindString(1, friend.getId());
                                    statement.bindString(2, gson.toJson(friend));
                                    statement.execute();
                                    statement.clearBindings();
                                }
                                statement.close();
                            }

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
                        Timber.d("Saved " + friends.size() + " friends in cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to save friends cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void saveContacts(final List<Friend> contacts)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("UserCache.saveContacts() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.PhoneContacts.SQL_DELETE);
                            statement.execute();
                            statement.close();

                            if (!contacts.isEmpty())
                            {
                                statement = db
                                        .compileStatement(SQLiteCacheContract.PhoneContacts.SQL_INSERT);
                                for (Friend contact : contacts)
                                {
                                    statement.bindString(1, contact.getId());
                                    statement.bindString(2, gson.toJson(contact));
                                    statement.execute();
                                    statement.clearBindings();
                                }
                                statement.close();
                            }

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
                        Timber.d("Saved " + contacts.size() + " contacts in cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to save contacts cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteFriends()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("UserCache.deleteFriends() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.FacebookFriends.SQL_DELETE);
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
                        Timber.d("Friends cache cleared");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete friends cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteContacts()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("UserCache.deleteContacts() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.PhoneContacts.SQL_DELETE);
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
                        Timber.d("Contacts cache cleared");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete contacts cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }
}
