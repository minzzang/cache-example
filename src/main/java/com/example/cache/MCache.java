package com.example.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MCache {
    private final Map<String, CacheValue> cache = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> counter = new ConcurrentHashMap<>();
    private final Repository db = new Repository();

    public String get(String key) {
        CacheValue cacheValue = cache.computeIfAbsent(key, k -> init(key));
        if (!cacheValue.expired) {
            return cacheValue.value;
        }

        AtomicInteger first = counter.get(key);

        if (first.getAndIncrement() == 0) {
            String dbValue = db.find(key);
            this.put(key, dbValue);
            return dbValue;
        } else {
            return cacheValue.value;
        }
    }
    private CacheValue init(String key) {
        String dbValue = db.find(key);
        counter.put(key, new AtomicInteger(0));
        return new CacheValue(dbValue);
    }

    public void put(String key, String value) {
        cache.put(key, new CacheValue(value));
        counter.put(key, new AtomicInteger(0));
    }

    public void expire(String key) {
        CacheValue cacheValue = cache.get(key);

        if (cacheValue != null) {
            cacheValue.expire();
        }
    }

    public void flush() {
        cache.clear();
    }
}
