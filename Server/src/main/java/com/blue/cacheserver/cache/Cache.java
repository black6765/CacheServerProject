package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;

public class Cache {
    private final int maxSize;
    private int initSize;
    private final long expireTime;
    private final long expireCheckTime;

    private int curCacheMemorySize = 0;

    Map<BytesKey, CacheValue> cacheMemory = new ConcurrentHashMap<>(initSize);
    Deque<BytesKey> evictionSampleQueue = new ConcurrentLinkedDeque<>();

    private Cache(Builder builder) {
        maxSize = builder.maxSize;
        initSize = builder.initSize;
        expireTime = builder.expireTime;
        expireCheckTime = builder.expireCheckTime;
    }

    public static class Builder {
        private int maxSize = 128;
        private int initSize = 64;
        private long expireTime = 6000;
        private long expireCheckTime = 500;

        public Builder maxSize(int val) {
            maxSize = val;
            return this;
        }

        public Builder initSize(int val) {
            initSize = val;
            return this;
        }

        public Builder expireTime(long val) {
            expireTime = val;
            return this;
        }

        public Builder expireCheckTime(long val) {
            expireCheckTime = val;
            return this;
        }

        public Cache build() {
            return new Cache(this);
        }
    }

    public String initSettingToString() {
        return "{" +
                "maxSize=" + maxSize +
                ", initSize=" + initSize +
                ", expireTime=" + expireTime +
                ", expireCheckTime=" + expireCheckTime +
                '}';
    }

    public int getCurCacheMemorySize() {
        return curCacheMemorySize;
    }

    public void setCurCacheMemorySize(int curCacheMemorySize) {
        this.curCacheMemorySize = curCacheMemorySize;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public long getExpireCheckTime() {
        return expireCheckTime;
    }

    public Map<BytesKey, CacheValue> getCacheMemory() {
        return cacheMemory;
    }

    public void eviction(int extraSize) {
        // ?????? ???????????? ????????? ?????? ??? ?????? ??????
        while (extraSize + curCacheMemorySize > maxSize) {
            List<BytesKey> bytesKeys = new ArrayList<>(cacheMemory.keySet());

            // ????????? Math.min(EVICTION_SAMPLE_SIZE, ?????? cacheMemory ?????? ??????)??? ?????? ??? ?????? while (true)
            // ?????? ??????????????? timeStamp??? ?????? ????????? ?????? ??????????????? ??????
            final int EVICTION_SAMPLE_SIZE = 5;
            int repeatNum = Math.min(EVICTION_SAMPLE_SIZE, cacheMemory.size());
            int repeatCount = 0;

            while (repeatCount < repeatNum) {
                Random random = new Random();
                BytesKey randomKey = bytesKeys.get(random.nextInt(bytesKeys.size()));

                // ?????? ????????? ?????? ?????? ????????? ??????
                if (evictionSampleQueue.contains(randomKey)) {
                    continue;
                }

                evictionSampleQueue.offerLast(randomKey);
                repeatCount++;
            }

            BytesKey oldestKey = evictionSampleQueue.pollFirst();
            CacheValue oldestCacheValue = cacheMemory.get(oldestKey);
            for (BytesKey key : evictionSampleQueue) {
                CacheValue cacheValue = cacheMemory.get(key);
                if (cacheValue.getTimeStamp().isBefore(oldestCacheValue.getTimeStamp())) {
                    oldestKey = key;
                    oldestCacheValue = cacheValue;
                }
            }
            cacheMemory.remove(oldestKey);
            curCacheMemorySize -= oldestCacheValue.getByteSize();

            evictionSampleQueue.clear();
        }

        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public byte[] put(byte[] key, byte[] value, Instant timeStamp) {
        final int thisSize = key.length + value.length;

        if (thisSize > maxSize) {
            System.out.println("Input data is bigger than max cache size");
            return null;
        }

        // ?????? ?????????????????? ???????????? ????????? ???????????? ???????????? maxSize?????? ??? ??? eviction
        if (curCacheMemorySize + thisSize > maxSize) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction(curCacheMemorySize + thisSize - maxSize);
        }

        BytesKey putKey = new BytesKey(key);
        CacheValue putValue = new CacheValue(value, timeStamp, thisSize);
        CacheValue returnValue = cacheMemory.put(putKey, putValue);

        if (returnValue == null) {
            curCacheMemorySize += thisSize;
            return null;
        }

        // ?????? ????????? value??? ????????? ?????????, ?????? value??? ????????? ???
        curCacheMemorySize = curCacheMemorySize + putValue.getByteSize() - returnValue.getByteSize();

        // expireTime??? ???????????? ?????? ????????? ?????? ?????? ?????? ???????????? ?????? ?????? ???????????? ??????
        long elapsedTime = Instant.now().toEpochMilli() - returnValue.getExpireTimeStamp().toEpochMilli();
        if (elapsedTime >= expireTime) {
            return null;
        }

        if (timeStamp.isBefore(returnValue.getTimeStamp())) {
            cacheMemory.put(putKey, returnValue);
            return putValue.getValue();
        }

        return returnValue.getValue();
    }

    public byte[] get(byte[] key, Instant timeStamp) {
        BytesKey getKey = new BytesKey(key);
        CacheValue getValue = cacheMemory.get(getKey);

        if (getValue == null) {
            return null;
        }

        long elapsedTime = Instant.now().toEpochMilli() - getValue.getExpireTimeStamp().toEpochMilli();
        if (elapsedTime >= expireTime) {
            curCacheMemorySize -= getValue.getByteSize();
            cacheMemory.remove(getKey);
            return null;
        }

        // LRU(Eviction)??? ???????????? ??????????????? ??????
        getValue.setTimeStamp(timeStamp);

        return getValue.getValue();
    }

    public byte[] remove(byte[] key) {
        BytesKey removeKey = new BytesKey(key);
        CacheValue removeValue = cacheMemory.remove(removeKey);

        if (removeValue == null) {
            return null;
        }
        curCacheMemorySize -= removeValue.getByteSize();

        long elapsedTime = Instant.now().toEpochMilli() - removeValue.getExpireTimeStamp().toEpochMilli();
        if (elapsedTime >= expireTime) {
            return null;
        }

        return removeValue.getValue();
    }

}
