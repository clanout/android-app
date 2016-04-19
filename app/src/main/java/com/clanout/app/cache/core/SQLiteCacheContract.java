package com.clanout.app.cache.core;

public abstract class SQLiteCacheContract
{
    public static abstract class Event
    {
        public static final String TABLE_NAME = "event_cache";

        public static final String COLUMN_ID = "event_id";
        public static final String COLUMN_CONTENT = "json";
        public static final String COLUMN_CHAT_SEEN_TIMESTAMP = "chat_seen_timestamp";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT, " +
                COLUMN_CHAT_SEEN_TIMESTAMP + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?)";
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_CHAT_SEEN_TIMESTAMP = "UPDATE " + TABLE_NAME + " SET " + COLUMN_CHAT_SEEN_TIMESTAMP + " = ? WHERE " + COLUMN_ID + " = ?";
    }

    public static abstract class FacebookFriends
    {
        public static final String TABLE_NAME = "facebook_friends_cache";

        public static final String COLUMN_ID = "user_id";
        public static final String COLUMN_CONTENT = "json";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?,?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";
    }

    public static abstract class PhoneContacts
    {
        public static final String TABLE_NAME = "phone_contacts_cache";

        public static final String COLUMN_ID = "user_id";
        public static final String COLUMN_CONTENT = "json";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_CONTENT + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?,?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";
    }

    public static final class Generic
    {
        public static final String TABLE_NAME = "generic_cache";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_VALUE = "value";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_KEY + " TEXT PRIMARY KEY, " +
                COLUMN_VALUE + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + " VALUES (?,?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " where key = ?";
        public static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;
    }

    public static final class Notification
    {
        public static final String TABLE_NAME = "notification_cache";

        public static final String COLUMN_ID = "notification_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_EVENT_ID = "event_id";
        public static final String COLUMN_EVENT_NAME = "event_name";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_USER_NAME = "user_name";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_TIMESTAMP_RECEIVED = "timestamp_received";
        public static final String COLUMN_IS_NEW = "is_new";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TYPE + " INTEGER, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_EVENT_ID + " TEXT, " +
                COLUMN_EVENT_NAME + " TEXT, " +
                COLUMN_USER_ID + " TEXT, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_TIMESTAMP_RECEIVED + " INTEGER, " +
                COLUMN_IS_NEW + " TEXT)";

        public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME + "  VALUES (?,?,?,?,?,?,?,?, ?, ?, ?)";
        public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;
        public static final String SQL_MARK_READ = "UPDATE " + TABLE_NAME + " SET " + COLUMN_IS_NEW + " = ?";
        public static final String SQL_DELETE_ONE = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        public static final String SQL_COUNT_NEW = "SELECT count(*) as new_count FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_NEW + " = 'true'";
    }
}
