package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;


public class Cache {
    private final int maxSize;
    private int initSize;
    private int expireTimeMilliSec;

    private Cache(Builder builder) {
        maxSize = builder.maxSize;
        initSize = builder.initSize;
        expireTimeMilliSec = builder.expireTimeMilliSec;
    }

    public static class Builder {
        private int maxSize = 128;
        private int initSize = 64;
        private int expireTimeMilliSec = 60000;

        public Builder maxSize(int val) {
            maxSize = val;
            return this;
        }

        public Builder initSize(int val) {
            initSize = val;
            return this;
        }

        public Builder expireTimeMilliSec(int val) {
            expireTimeMilliSec = val;
            return this;
        }

        public Cache build() {
            return new Cache(this);
        }
    }

    Map<BytesKey, CacheValue> cacheMemory = new ConcurrentHashMap<>(initSize);
    Deque<BytesKey> expireQueue = new ConcurrentLinkedDeque<>();

    public Map<BytesKey, CacheValue> getCacheMemory() {
        return cacheMemory;
    }

    public Deque<BytesKey> getExpireQueue() {
        return expireQueue;
    }

    public void eviction() {
//        System.out.println("\n[Eviction start]");
//
//        for (int i = 0; i < (MAX_SIZE / 2); i++) {
//            K target = evictionQueue.removeFirst();
//            cacheMemory.remove(target);
//            removedKeyList.add(target);
//        }
//
//        System.out.println("- Removed key list -");
//        System.out.println(removedKeyList);
//        removedKeyList.clear();
//        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public byte[] put(byte[] key, byte[] value) {
        final int thisSize = key.length + value.length;

        if (cacheMemory.size() + thisSize >= maxSize) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction();
        }

        CacheValue returnVal = cacheMemory.put(new BytesKey(key), new CacheValue(value, Instant.now()));

        if (returnVal == null)
            return null;

        return returnVal.getValue();
    }

    public byte[] get(byte[] key) {
//        int idx = evictionQueue.indexOf(key);
//
//        if (idx != -1) {
//            K refreshKey = evictionQueue.remove(idx);
//            evictionQueue.addLast(refreshKey);
//        }

        CacheValue returnVal = cacheMemory.get(new BytesKey(key));
        if (returnVal == null) return null;

        return returnVal.getValue();
    }

    public byte[] remove(byte[] key) {
//        int idx = evictionQueue.indexOf(key);
//        if (idx != -1) {
//            evictionQueue.remove(idx);
//        }

        CacheValue returnVal = cacheMemory.remove(new BytesKey(key));
        if (returnVal == null) return null;

        return returnVal.getValue();
    }
}