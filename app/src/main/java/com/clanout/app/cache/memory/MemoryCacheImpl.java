package com.clanout.app.cache.memory;

import java.util.HashMap;
import java.util.Map;

public class MemoryCacheImpl implements MemoryCache
{
    private static MemoryCacheImpl instance;

    public static MemoryCacheImpl getInstance()
    {
        if (instance == null)
        {
            instance = new MemoryCacheImpl();
        }

        return instance;
    }

    private Map<String, Object> data;

    public MemoryCacheImpl()
    {
        data = new HashMap<>();
    }

    @Override
    public void delete(String key)
    {
        data.remove(key);
    }

    @Override
    public void put(String key, Object value)
    {
        data.put(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> type)
    {
        try
        {
            return type.cast(data.get(key));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void deleteAll()
    {
        data.clear();
    }
}
