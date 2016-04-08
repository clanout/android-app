package com.clanout.app.cache._core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

public class DatabaseManager
{
    private static final String TAG = "DatabaseManager";

    private static SQLiteCacheHelper sqliteCacheHelper;
    private static DatabaseManager instance;

    private SQLiteDatabase database;
    private AtomicInteger connectionCounter;

    private DatabaseManager()
    {
        connectionCounter = new AtomicInteger(0);
    }

    public static synchronized void init(Context context)
    {
        instance = new DatabaseManager();
        sqliteCacheHelper = new SQLiteCacheHelper(context);
        Timber.d("DatabaseManager initialized");
    }

    public static synchronized DatabaseManager getInstance()
    {
        if(instance == null)
        {
            Timber.e("DatabaseManager not initialized");
            throw new IllegalStateException("DatabaseManager not initialized");
        }
        return instance;
    }

    public synchronized SQLiteDatabase openConnection()
    {
        if(connectionCounter.incrementAndGet() == 1)
        {
            database = sqliteCacheHelper.getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeConnection()
    {
        if(connectionCounter.decrementAndGet() == 0)
        {
            database.close();
            database = null;
        }
    }
}
