package com.clanout.app.cache.notification;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache._core.DatabaseManager;
import com.clanout.app.cache._core.SQLiteCacheContract;
import com.clanout.app.model.Notification;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SQLiteNotificationCache implements NotificationCache
{
    private static final String TAG = "NotificationCache";

    private static SQLiteNotificationCache instance;

    public static SQLiteNotificationCache getInstance()
    {
        if (instance == null) {
            instance = new SQLiteNotificationCache();
        }
        return instance;
    }

    private DatabaseManager databaseManager;
    private Gson gson;

    private SQLiteNotificationCache()
    {
        databaseManager = DatabaseManager.getInstance();
        gson = GsonProvider.getGson();
        Timber.d("SQLiteNotificationCache initialized");
    }

    @Override
    public Observable<Object> put(final Notification notification)
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.put() on thread = " + Thread.currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification.SQL_INSERT);

                            statement.bindLong(1, notification.getId());
                            statement.bindLong(2, notification.getType());
                            statement.bindString(3, notification.getTitle());
                            statement.bindString(4, notification.getMessage());
                            statement.bindString(5, notification.getEventId());
                            statement.bindString(6, notification.getEventName());
                            statement.bindString(7, notification.getUserId());
                            statement.bindString(8, notification.getUserName());
                            statement.bindLong(9, notification.getTimestamp().getMillis());
                            statement.bindLong(10, notification.getTimestampReceived().getMillis());
                            statement.bindString(11, String.valueOf(notification.isNew()));

                            statement.execute();
                            statement.close();
                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Notification>> getAll()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Notification>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Notification>> subscriber)
                    {

                        Timber.v("NotificationCache.read() on thread = " + Thread.currentThread()
                                .getName());

                        List<Notification> notifications = new ArrayList<Notification>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Notification.COLUMN_ID,
                                SQLiteCacheContract.Notification.COLUMN_TYPE,
                                SQLiteCacheContract.Notification.COLUMN_TITLE,
                                SQLiteCacheContract.Notification.COLUMN_MESSAGE,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_ID,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_NAME,
                                SQLiteCacheContract.Notification.COLUMN_USER_ID,
                                SQLiteCacheContract.Notification.COLUMN_USER_NAME,
                                SQLiteCacheContract.Notification.COLUMN_TIMESTAMP,
                                SQLiteCacheContract.Notification.COLUMN_IS_NEW
                        };

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Notification.TABLE_NAME, projection,
                                        null, null, null, null, SQLiteCacheContract.Notification
                                                .COLUMN_TIMESTAMP + " DESC");
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            try {
                                Notification notification = new Notification
                                        .Builder(cursor.getInt(0))
                                        .type(cursor.getInt(1))
                                        .title(cursor.getString(2))
                                        .message(cursor.getString(3))
                                        .eventId(cursor.getString(4))
                                        .eventName(cursor.getString(5))
                                        .userId(cursor.getString(6))
                                        .userName(cursor.getString(7))
                                        .timestamp(new DateTime(cursor.getLong(8)))
                                        .isNew(Boolean.parseBoolean(cursor.getString(9)))
                                        .build();

                                notifications.add(notification);
                            }
                            catch (Exception e) {
                                Timber.v("Unable to process a notification [" + e
                                        .getMessage() + "]");
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(notifications);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> clear()
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.remove() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification.SQL_DELETE);
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> markRead()
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.markRead() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification
                                            .SQL_MARK_READ);
                            statement.bindString(1, String.valueOf(false));
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Object> clear(final int notificationId)
    {
        return Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.remove(id) on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            SQLiteDatabase db = databaseManager.openConnection();

                            SQLiteStatement statement = db
                                    .compileStatement(SQLiteCacheContract.Notification
                                            .SQL_DELETE_ONE);
                            statement.bindLong(1, notificationId);
                            statement.execute();
                            statement.close();

                            databaseManager.closeConnection();

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Boolean> isAvaliable()
    {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>()
                {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.isAvailable() on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            boolean isAvailable = false;

                            SQLiteDatabase db = databaseManager.openConnection();

                            Cursor cursor = db
                                    .rawQuery(SQLiteCacheContract.Notification.SQL_COUNT_NEW, null);
                            if (cursor.moveToFirst()) {
                                do {
                                    try {
                                        int count = Integer.parseInt(cursor.getString(0));
                                        if (count > 0) {
                                            isAvailable = true;
                                            break;
                                        }
                                    }
                                    catch (Exception e) {
                                    }
                                }
                                while (cursor.moveToNext());
                            }
                            cursor.close();
                            databaseManager.closeConnection();

                            subscriber.onNext(isAvailable);
                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Notification>> getAllForType(final int type)
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Notification>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Notification>> subscriber)
                    {

                        Timber.v("NotificationCache.getAllForType() on thread = " + Thread
                                .currentThread()
                                .getName());

                        List<Notification> notifications = new ArrayList<Notification>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Notification.COLUMN_ID,
                                SQLiteCacheContract.Notification.COLUMN_TYPE,
                                SQLiteCacheContract.Notification.COLUMN_TITLE,
                                SQLiteCacheContract.Notification.COLUMN_MESSAGE,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_ID,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_NAME,
                                SQLiteCacheContract.Notification.COLUMN_USER_ID,
                                SQLiteCacheContract.Notification.COLUMN_USER_NAME,
                                SQLiteCacheContract.Notification.COLUMN_TIMESTAMP,
                                SQLiteCacheContract.Notification.COLUMN_IS_NEW
                        };

                        String selection = SQLiteCacheContract.Notification.COLUMN_TYPE + " = ?";
                        String[] selectionArgs = {String.valueOf(type)};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Notification.TABLE_NAME, projection,
                                        selection, selectionArgs, null, null, SQLiteCacheContract
                                                .Notification.COLUMN_TIMESTAMP + " DESC");
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            try {
                                Notification notification = new Notification
                                        .Builder(cursor.getInt(0))
                                        .type(cursor.getInt(1))
                                        .title(cursor.getString(2))
                                        .message(cursor.getString(3))
                                        .eventId(cursor.getString(4))
                                        .eventName(cursor.getString(5))
                                        .userId(cursor.getString(6))
                                        .userName(cursor.getString(7))
                                        .timestamp(new DateTime(cursor.getLong(8)))
                                        .isNew(Boolean.parseBoolean(cursor.getString(9)))
                                        .build();

                                notifications.add(notification);
                            }
                            catch (Exception e) {
                                Timber.v("Unable to process a notification [" + e
                                        .getMessage() + "]");
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(notifications);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Notification>> getAllForEvent(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Notification>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Notification>> subscriber)
                    {

                        Timber.v("NotificationCache.getAllForType() on thread = " + Thread
                                .currentThread()
                                .getName());

                        List<Notification> notifications = new ArrayList<Notification>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Notification.COLUMN_ID,
                                SQLiteCacheContract.Notification.COLUMN_TYPE,
                                SQLiteCacheContract.Notification.COLUMN_TITLE,
                                SQLiteCacheContract.Notification.COLUMN_MESSAGE,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_ID,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_NAME,
                                SQLiteCacheContract.Notification.COLUMN_USER_ID,
                                SQLiteCacheContract.Notification.COLUMN_USER_NAME,
                                SQLiteCacheContract.Notification.COLUMN_TIMESTAMP,
                                SQLiteCacheContract.Notification.COLUMN_IS_NEW
                        };

                        String selection = SQLiteCacheContract.Notification.COLUMN_EVENT_ID + " =" +
                                " ?";
                        String[] selectionArgs = {String.valueOf(eventId)};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Notification.TABLE_NAME, projection,
                                        selection, selectionArgs, null, null, SQLiteCacheContract
                                                .Notification.COLUMN_TIMESTAMP + " DESC");
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            try {
                                Notification notification = new Notification
                                        .Builder(cursor.getInt(0))
                                        .type(cursor.getInt(1))
                                        .title(cursor.getString(2))
                                        .message(cursor.getString(3))
                                        .eventId(cursor.getString(4))
                                        .eventName(cursor.getString(5))
                                        .userId(cursor.getString(6))
                                        .userName(cursor.getString(7))
                                        .timestamp(new DateTime(cursor.getLong(8)))
                                        .isNew(Boolean.parseBoolean(cursor.getString(9)))
                                        .build();

                                notifications.add(notification);
                            }
                            catch (Exception e) {
                                Timber.v("Unable to process a notification [" + e
                                        .getMessage() + "]");
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(notifications);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Notification>> getAll(final int type, final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<List<Notification>>()
                {
                    @Override
                    public void call(Subscriber<? super List<Notification>> subscriber)
                    {

                        Timber.v("NotificationCache.getAllForType() on thread = " + Thread
                                .currentThread()
                                .getName());

                        List<Notification> notifications = new ArrayList<Notification>();

                        SQLiteDatabase db = databaseManager.openConnection();
                        String[] projection = {
                                SQLiteCacheContract.Notification.COLUMN_ID,
                                SQLiteCacheContract.Notification.COLUMN_TYPE,
                                SQLiteCacheContract.Notification.COLUMN_TITLE,
                                SQLiteCacheContract.Notification.COLUMN_MESSAGE,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_ID,
                                SQLiteCacheContract.Notification.COLUMN_EVENT_NAME,
                                SQLiteCacheContract.Notification.COLUMN_USER_ID,
                                SQLiteCacheContract.Notification.COLUMN_USER_NAME,
                                SQLiteCacheContract.Notification.COLUMN_TIMESTAMP,
                                SQLiteCacheContract.Notification.COLUMN_IS_NEW
                        };

                        String selection = SQLiteCacheContract.Notification.COLUMN_EVENT_ID + " =" +
                                " ? AND " + SQLiteCacheContract.Notification.COLUMN_TYPE + " = ?";
                        String[] selectionArgs = {String.valueOf(eventId), String.valueOf(type)};

                        Cursor cursor = db
                                .query(SQLiteCacheContract.Notification.TABLE_NAME, projection,
                                        selection, selectionArgs, null, null, SQLiteCacheContract
                                                .Notification.COLUMN_TIMESTAMP + " DESC");
                        cursor.moveToFirst();
                        while (!cursor.isAfterLast()) {
                            try {
                                Notification notification = new Notification
                                        .Builder(cursor.getInt(0))
                                        .type(cursor.getInt(1))
                                        .title(cursor.getString(2))
                                        .message(cursor.getString(3))
                                        .eventId(cursor.getString(4))
                                        .eventName(cursor.getString(5))
                                        .userId(cursor.getString(6))
                                        .userName(cursor.getString(7))
                                        .timestamp(new DateTime(cursor.getLong(8)))
                                        .isNew(Boolean.parseBoolean(cursor.getString(9)))
                                        .build();

                                notifications.add(notification);
                            }
                            catch (Exception e) {
                                Timber.v("Unable to process a notification [" + e
                                        .getMessage() + "]");
                            }

                            cursor.moveToNext();
                        }
                        cursor.close();
                        databaseManager.closeConnection();

                        subscriber.onNext(notifications);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Boolean> clear(final List<Integer> notifificationIds)
    {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>()
                {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber)
                    {
                        synchronized (TAG) {
                            Timber.v("NotificationCache.clearList(ids) on thread = " + Thread
                                    .currentThread()
                                    .getName());

                            if(notifificationIds.size() > 0) {
                                SQLiteDatabase db = databaseManager.openConnection();
                                db.beginTransactionNonExclusive();

                                for (Integer notificationId : notifificationIds) {
                                    SQLiteStatement statement = db
                                            .compileStatement(SQLiteCacheContract.Notification
                                                    .SQL_DELETE_ONE);
                                    statement.bindLong(1, notificationId);
                                    statement.execute();
                                    statement.close();
                                }

                                db.setTransactionSuccessful();
                                db.endTransaction();

                                databaseManager.closeConnection();

                            }

                            subscriber.onNext(true);

                            subscriber.onCompleted();
                        }
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void clearAll()
    {

        synchronized (TAG) {
            Timber.v("NotificationCache.remove() on thread = " + Thread
                    .currentThread()
                    .getName());

            SQLiteDatabase db = databaseManager.openConnection();

            SQLiteStatement statement = db
                    .compileStatement(SQLiteCacheContract.Notification.SQL_DELETE);
            statement.execute();
            statement.close();

            databaseManager.closeConnection();
        }
    }
}
