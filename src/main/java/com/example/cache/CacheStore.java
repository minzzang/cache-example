package com.example.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheStore {
    private Map<String, AtomicInteger> cache = new HashMap<>();
    private final Map<String, AtomicInteger> syncBuffer = new ConcurrentHashMap<>();

    public void toConcurrentHashMap() {
        cache = new ConcurrentHashMap<>();
    }

    public Integer read(String key) {
        return cache.get(key).intValue();
    }

    public void write(String key, int value) {
        AtomicInteger cacheValue = cache.get(key);

        if (cacheValue.addAndGet(value) < 0) {
            cacheValue.set(0);
        }

        syncBuffer.putIfAbsent(key, cacheValue);
    }

    public void put(String key, int value) {
        cache.put(key, new AtomicInteger(value));
    }

    public int getTotalWriteCount() {
        int remainSum = 0;
        for (AtomicInteger value : cache.values()) {
            remainSum += value.get();
        }

        int initialSum = 100 * cache.size();
        return initialSum - remainSum;
    }

    public Map<String, AtomicInteger> getSyncBuffer() {
        return syncBuffer;
    }
}
