package com.example.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Repository {
    private int postFix;
    private final Map<String, Integer> db = new HashMap<>();

    public String find(String key) {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return key + postFix++;
    }

    public void bulkUpdate(Map<String, AtomicInteger> origin) {
        Map<String, AtomicInteger> copy = new HashMap<>(origin);
        Map<String, Integer> updatedData = new HashMap<>();

        copy.forEach((key, value) -> {
            origin.remove(key, value);
            updatedData.put(key, value.get());
        });

        try {
            Thread.sleep(100L);
            db.putAll(updatedData);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
