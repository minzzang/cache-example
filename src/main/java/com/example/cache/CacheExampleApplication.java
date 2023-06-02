package com.example.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheExampleApplication {
    private static final KeyStore KEY_STORE = new KeyStore();
    private static final MCache M_CACHE = new MCache();

    private static final Repository DB = new Repository();
    private static final CacheStore CACHE_STORE = new CacheStore();
    private static final Cache<Object, Object> CAFFEINE = Caffeine
            .newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build();
    private static final LocalDBSyncScheduler SCHEDULER = new LocalDBSyncScheduler(DB);
    private static final int MAX_ITERATION_COUNT = 10_000;
    private static final int N_THREADS = 100;

    public static void main(String[] args) {
        System.out.println("쓰레드 개수 : " + N_THREADS);
        printHeapMemory();

        SCHEDULER.sync(CACHE_STORE.getSyncBuffer());

        // loading cache - mCache
        동시에_10000번_호출(() -> getMCache());
        flush한_후에_100번_호출(() -> getMCache());
        expire가_50퍼센트_된_캐시_동시에_10000번_호출(() -> getMCache());

        // loading cache - caffeine
        동시에_10000번_호출(() -> getCaffeineCache());
        flush한_후에_100번_호출(() -> getCaffeineCache());
        expire가_50퍼센트_된_캐시_동시에_10000번_호출(() -> getCaffeineCache());

        // cache store - hashmap
        특정_key_동시에_100개_감소();
        특정_key_동시에_101개_감소();
        동시에_10000개_읽기_100개_감소();

        // cache store - concurrentHashMap
        CACHE_STORE.toConcurrentHashMap();

        특정_key_동시에_100개_감소();
        특정_key_동시에_101개_감소();
        동시에_10000개_읽기_100개_감소();
    }

    private static void flush한_후에_100번_호출(Runnable task) {
        flush();

        System.out.println("flush한 후에 100번 호출");
        test( 100, task);
        System.out.println();
    }

    private static void 동시에_10000번_호출(Runnable task) {
        createCache();

        System.out.println("동시에 10000번 호출");
        test( MAX_ITERATION_COUNT, task);
        System.out.println();
    }

    private static void expire가_50퍼센트_된_캐시_동시에_10000번_호출(Runnable task) {
        createCache();

        for (int i = 1; i <= 500; i++) {
            String key = KEY_STORE.randomKeyByPercentage();
            M_CACHE.expire(key);
            CAFFEINE.invalidate(key);
        }

        System.out.println("expire가 50퍼센트 된 캐시를 동시에 10000번 호출");
        test( MAX_ITERATION_COUNT, task);
        System.out.println();
    }

    private static void 특정_key_동시에_100개_감소() {
        createCache();

        final String key = "1";
        System.out.println("특정 key를 동시에 100개 감소");
        System.out.println("초기 값 : " + CACHE_STORE.read(key));

        test( 100, () -> CACHE_STORE.write(key, -1));
        System.out.println("감소 후 값 : " + CACHE_STORE.read(key));
        System.out.println();
    }

    private static void 특정_key_동시에_101개_감소() {
        createCache();

        final String key = "1";
        System.out.println("특정 key를 동시에 101개 감소");
        System.out.println("초기 값 : " + CACHE_STORE.read(key));

        test( 101, () -> CACHE_STORE.write(key, -1));
        System.out.println("감소 후 값 : " + CACHE_STORE.read(key));
        System.out.println();
    }

    private static void 동시에_10000개_읽기_100개_감소() {
        createCache();

        System.out.println("동시에 10000개 읽기와 100개 감소");
        System.out.println("초기 값 : " + CACHE_STORE.getTotalWriteCount());

        AtomicInteger atomicInteger1 = new AtomicInteger(0);
        test(MAX_ITERATION_COUNT, () -> readAndWrite(atomicInteger1));
        System.out.println("감소 후 값 : " + CACHE_STORE.getTotalWriteCount());
        System.out.println();
    }

    private static void createCache() {
        for (int i = 1; i <= 1000; i++) {
            String key = i + "";
            M_CACHE.put(key, key);
            CAFFEINE.put(key, key);
            CACHE_STORE.put(key, 100);
        }
    }
    private static void getMCache() {
        M_CACHE.get(KEY_STORE.randomKeyByPercentage());
    }

    private static void getCaffeineCache() {
        CAFFEINE.get(KEY_STORE.randomKeyByPercentage(),
                key -> DB.find(key.toString()));
    }

    private static void flush() {
        M_CACHE.flush();
        CAFFEINE.invalidateAll();
    }

    private static void readAndWrite(AtomicInteger count) {
        int index = new Random().nextInt(MAX_ITERATION_COUNT);

        if (index <= 130 && count.getAndIncrement() < 100) {
            CACHE_STORE.write(KEY_STORE.randomKeyByPercentage(), -1);
        }
        CACHE_STORE.read(KEY_STORE.randomKeyByPercentage());
    }

    private static void test(int iterationCount, Runnable task) {
        long startTime = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(N_THREADS);
        for (int i = 0; i < iterationCount; i++) {
            executorService.submit(task);
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("쓰레드 풀 종료 중 예외가 발생했습니다: " + e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("실행시간: " + (endTime - startTime) + "ms");

        printHeapMemory();
    }

    private static void printHeapMemory() {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        System.out.println("현재 힙 메모리 사이즈: " + heapSize / (1024 * 1024) + "MB");
        System.out.println("최대 힙 메모리 사이즈: " + heapMaxSize / (1024 * 1024) + "MB");
        System.out.println("free 메모리 사이즈: " + heapFreeSize / (1024 * 1024) + "MB");
        System.out.println();
    }
}
