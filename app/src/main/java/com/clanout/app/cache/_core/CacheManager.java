package com.clanout.app.cache._core;

import com.clanout.app.cache.event.EventCache;
import com.clanout.app.cache.event.SQLiteEventCache;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.cache.generic.SQLiteGenericCache;
import com.clanout.app.cache.memory.MemoryCache;
import com.clanout.app.cache.memory.MemoryCacheImpl;
import com.clanout.app.cache.notification.NotificationCache;
import com.clanout.app.cache.notification.SQLiteNotificationCache;
import com.clanout.app.cache.user.SQLiteUserCache;
import com.clanout.app.cache.user.UserCache;

public class CacheManager
{
    public static GenericCache getGenericCache()
    {
        return SQLiteGenericCache.getInstance();
    }

    public static EventCache getEventCache()
    {
        return SQLiteEventCache.getInstance();
    }

    public static UserCache getUserCache()
    {
        return SQLiteUserCache.getInstance();
    }

    public static NotificationCache getNotificationCache()
    {
        return SQLiteNotificationCache.getInstance();
    }

    public static MemoryCache getMemoryCache()
    {
        return MemoryCacheImpl.getInstance();
    }

    public static void clearAllCaches()
    {
        GenericCache genericCache = getGenericCache();
        UserCache userCache = getUserCache();
        EventCache eventCache = getEventCache();
        NotificationCache notificationCache = getNotificationCache();

        genericCache.deleteAll();

        userCache.deleteFriends();
        userCache.deleteContacts();

        eventCache.deleteAll();

        notificationCache.clearAll();
    }

    public static void clearFriendsCache()
    {
        UserCache userCache = getUserCache();
        EventCache eventCache = getEventCache();

        userCache.deleteFriends();
        userCache.deleteContacts();
        eventCache.deleteAll();
    }
}
