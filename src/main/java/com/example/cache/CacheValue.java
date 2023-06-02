package com.example.cache;

public class CacheValue {
    String value;
    boolean expired;

    public CacheValue(String value) {
        this.value = value;
    }

    public void expire() {
        expired = true;
    }
}
