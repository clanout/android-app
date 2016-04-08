package com.clanout.app.cache.generic;

public interface GenericCache
{
    void put(String key, String value);

    String get(String key);

    void delete(String key);

    void put(String key, Object value);

    <T> T get(String key, Class<T> type);

    void deleteAll();
}
