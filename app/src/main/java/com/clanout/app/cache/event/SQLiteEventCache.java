package com.clanout.app.cache.event;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache.core.DatabaseManager;
import com.clanout.app.cache.core.SQLiteCacheContract;
import com.clanout.app.model.Event;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteEventCache implements EventCache
{
    private static final String TAG = "EventCache";

    private static SQLiteEventCache instance;

    public static SQLiteEventCache getInstance()
    {
        if (instance == null)
        {
            instance = new SQLiteEventCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;

    private SQLiteEventCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
        Timber.d("SQLiteEventCache initialized");
    }

    @Override
    public Observable<List<Event>> getEvents()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Event>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Event>> subscriber)
                    {
                        List<Event> events = new ArrayList<Event>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Event.COLUMN_CONTENT
                        };

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Event.TABLE_NAME, projection, null, null, null, null, null);
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast())
                        {
                            String eventJson = cursor.getString(0);
                            Event event = gson.fromJson(eventJson, Event.class);
                            events.add(event);

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(events);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Event> getEvent(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<Event>()
                {
                    @Override
                    public void call(Subscriber<? super Event> subscriber)
                    {
                        SQLiteDatabase db = databaseManager.openConnection();

                        String[] projection = {
                                SQLiteCacheContract.Event.COLUMN_CONTENT
                        };
                        String selection = SQLiteCacheContract.Event.COLUMN_ID + " = ?";
                        String[] selectionArgs = {eventId};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Event.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                        cursor.moveToFirst();

                        Event event = null;
                        if (!cursor.isAfterLast())
                        {
                            String eventJson = cursor.getString(0);
                            event = gson.fromJson(eventJson, Event.class);
                        }
                        else
                        {
                            Timber.d("Event not present in cache (" + eventId + ")");
                        }

                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(event);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void reset(final List<Event> events)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.reset() on thread = " + Thread.currentThread()
                                                                               .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();

                            if (!events.isEmpty())
                            {
                                statement = db
                                        .compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
                                for (Event event : events)
                                {
                                    statement.bindString(1, event.getId());
                                    statement.bindString(2, gson.toJson(event));
                                    statement.bindString(3, "");
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
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("Event cache reset. [New Size = " + events.size() + "]");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to reset events cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void save(final Event event)
    {
        getChatSeenTimestamp(event.getId())
                .flatMap(new Func1<DateTime, Observable<Object>>()
                {
                    @Override
                    public Observable<Object> call(final DateTime chatTimestamp)
                    {
                        return Observable
                                .create(new Observable.OnSubscribe<Object>()
                                {
                                    @Override
                                    public void call(Subscriber<? super Object> subscriber)
                                    {
                                        synchronized (TAG)
                                        {
                                            Timber.v("EventCache.save() on thread = " + Thread
                                                    .currentThread()
                                                    .getName());

                                            SQLiteDatabase db = databaseManager.openConnection();
                                            db.beginTransactionNonExclusive();

                                            SQLiteStatement statement = db
                                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
                                            statement.bindString(1, event.getId());
                                            statement.execute();
                                            statement.clearBindings();
                                            statement.close();

                                            statement = db
                                                    .compileStatement(SQLiteCacheContract.Event.SQL_INSERT);
                                            statement.bindString(1, event.getId());
                                            statement.bindString(2, gson.toJson(event));

                                            if (chatTimestamp != null)
                                            {
                                                statement.bindString(3, chatTimestamp.toString());
                                            }
                                            else
                                            {
                                                statement.bindString(3, "");
                                            }

                                            statement.execute();
                                            statement.clearBindings();
                                            statement.close();

                                            db.setTransactionSuccessful();
                                            db.endTransaction();
                                            databaseManager.closeConnection();

                                            subscriber.onCompleted();
                                        }
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("Inserted one event [" + event.getId() + "] in cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to insert event_id = " + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void deleteAll()
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.deleteAll() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            db.beginTransactionNonExclusive();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE);
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
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("Event cache cleared");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete events cache [" + e.getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void delete(final String eventId)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.delete() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_DELETE_ONE);
                            statement.bindString(1, eventId);
                            statement.execute();
                            statement.clearBindings();
                            statement.close();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        Timber.d("Deleted one event [" + eventId + "] from cache");
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to delete event_id = " + eventId + " [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    @Override
    public void updateChatSeenTimestamp(final String eventId, final DateTime timestamp)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG)
                        {
                            Timber.v("EventCache.updateChatSeenTimestamp() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();
                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Event.SQL_CHAT_SEEN_TIMESTAMP);
                            statement.bindString(1, timestamp.toString());
                            statement.bindString(2, eventId);
                            statement.execute();
                            statement.close();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.e("Unable to update chat timestamp for event (" + eventId + ") [" + e
                                .getMessage() + "]");
                    }

                    @Override
                    public void onNext(Object o)
                    {
                    }
                });
    }

    @Override
    public Observable<DateTime> getChatSeenTimestamp(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<DateTime>()
                {
                    @Override
                    public void call(Subscriber<? super DateTime> subscriber)
                    {
                        SQLiteDatabase db = databaseManager.openConnection();

                        String[] projection = {SQLiteCacheContract.Event.COLUMN_CHAT_SEEN_TIMESTAMP};
                        String selection = SQLiteCacheContract.Event.COLUMN_ID + " = ?";
                        String[] selectionArgs = {eventId};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Event.TABLE_NAME, projection, selection, selectionArgs, null, null, null);
                        cursor.moveToFirst();

                        DateTime timestamp = null;
                        if (!cursor.isAfterLast())
                        {
                            try
                            {
                                String timestampJson = cursor.getString(0);
                                timestamp = DateTime.parse(timestampJson);
                            }
                            catch (Exception e)
                            {
                            }
                        }
                        else
                        {
                            Timber.d("Event not present in cache (" + eventId + ")");
                        }

                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(timestamp);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }
}
