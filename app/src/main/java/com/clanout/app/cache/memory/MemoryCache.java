package com.clanout.app.cache.memory;

public interface MemoryCache
{
    void put(String key, Object value);

    <T> T get(String key, Class<T> type);

    void delete(String key);

    void deleteAll();
}
