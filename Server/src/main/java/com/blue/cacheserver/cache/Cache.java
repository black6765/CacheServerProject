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
        return "Cache Setting: {" +
                "maxSize=" + maxSize +
                ", initSize=" + initSize +
                ", expireTime=" + expireTime +
                ", expireCheckTime=" + expireCheckTime +
                '}';
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
        // 캐시 사이즈에 여유가 생길 때 까지 반복
        while (extraSize + curCacheMemorySize > maxSize) {
            List<BytesKey> bytesKeys = new ArrayList<>(cacheMemory.keySet());

            // 샘플을 Math.min(EVICTION_SAMPLE_SIZE, 현재 cacheMemory 원소 개수)개 만들 때 까지 while (true)
            // 샘플 리스트에서 timeStamp가 가장 오래된 것을 최종적으로 삭제
            final int EVICTION_SAMPLE_SIZE = 5;
            int repeatNum = Math.min(EVICTION_SAMPLE_SIZE, cacheMemory.size());
            int repeatCount = 0;

            while (repeatCount < repeatNum) {
                Random random = new Random();
                BytesKey randomKey = bytesKeys.get(random.nextInt(bytesKeys.size()));

                // 이미 선택된 값이 다시 선택될 경우
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

        // 새로 갱신된 value의 크기를 더하고, 이전 value의 크기를 뺌
        curCacheMemorySize = curCacheMemorySize + putValue.getByteSize() - returnValue.getByteSize();

        if (timeStamp.isBefore(returnValue.getTimeStamp())) {
            cacheMemory.put(putKey, returnValue);
            return putValue.getValue();
        }

        return returnValue.getValue();
    }

    public byte[] get(byte[] key, Instant timeStamp) {
        CacheValue returnValue = cacheMemory.get(new BytesKey(key));

        if (returnValue == null) {
            return null;
        }

        returnValue.setTimeStamp(timeStamp);

        return returnValue.getValue();
    }

    public byte[] remove(byte[] key) {
        BytesKey removeKey = new BytesKey(key);
        CacheValue returnValue = cacheMemory.remove(removeKey);

        if (returnValue == null) {
            return null;
        }
        curCacheMemorySize -= returnValue.getByteSize();

        return returnValue.getValue();
    }

}
