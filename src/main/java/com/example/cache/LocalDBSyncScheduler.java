package com.example.cache;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalDBSyncScheduler {
    private final Repository repository;

    public LocalDBSyncScheduler(Repository repository) {
        this.repository = repository;
    }

    public void sync(Map<String, AtomicInteger> syncBuffer) {
        Timer timer = new Timer();
        long interval = 10 * 1000;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                repository.bulkUpdate(syncBuffer);
            }
        }, 0, interval);
    }
}
