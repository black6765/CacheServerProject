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
    private long expireMilliSecTime;
    private long expireCheckSecTime;
    private int expireQueueSize;

    private int curCacaheMemorySize = 0;

    private Cache(Builder builder) {
        maxSize = builder.maxSize;
        initSize = builder.initSize;
        expireMilliSecTime = builder.expireMilliSecTime;
        expireCheckSecTime = builder.expireCheckMilliSecTime;
        expireQueueSize = builder.expireQueueSize;
    }

    public static class Builder {
        private int maxSize = 128;
        private int initSize = 64;
        private long expireMilliSecTime = 6000;
        private long expireCheckMilliSecTime = 3000;
        private int expireQueueSize = 10;

        public Builder maxSize(int val) {
            maxSize = val;
            return this;
        }

        public Builder initSize(int val) {
            initSize = val;
            return this;
        }

        public Builder expireMilliSecTime(long val) {
            expireCheckMilliSecTime = val;
            return this;
        }

        public Builder expireCheckMilliSecTime(long val) {
            expireCheckMilliSecTime = val;
            return this;
        }

        public Builder expireQueueSize(int val) {
            expireQueueSize = val;
            return this;
        }

        public Cache build() {
            return new Cache(this);
        }
    }

    Map<BytesKey, CacheValue> cacheMemory = new ConcurrentHashMap<>(initSize);
    Deque<BytesKey> expireQueue = new ConcurrentLinkedDeque<>();
    Deque<BytesKey> removedQueue = new ConcurrentLinkedDeque<>();

    public long getExpireMilliSecTime() {
        return expireMilliSecTime;
    }

    public long getExpireCheckSecTime() {
        return expireCheckSecTime;
    }

    public Map<BytesKey, CacheValue> getCacheMemory() {
        return cacheMemory;
    }

    public Deque<BytesKey> getExpireQueue() {
        return expireQueue;
    }

    public void eviction(int extraSize) {
        System.out.println("\n[Eviction start]");

        if (expireQueue.isEmpty()) {
            while (true) {
                List<BytesKey> bytesKeys = new ArrayList<>(cacheMemory.keySet());
                Random r = new Random();
                // 샘플을 Math.min(5, curSize)개 만들 때 까지 while (true)
                // 각 샘플을 리스트로 새로운 리스트로 추가
                // 이때 샘플은 extraSize - randomCacheValue.getByteSize() < maxSize 조건을 만족시켜야함
                // 샘플 리스트에서 timeStamp가 가장 오래된 것을 최종적으로 삭제
                // ! 랜덤으로 샘플링할 필요가 있을까?
                // ! 그냥 작은 사이즈의 큐를 두고 가장 오래된 타임스탬프를 관리하면...
                // ! 처음에 오래된 5개가 쌓인 후 eviction 될 떄 까지 갱신되지 않다가
                // ! 이빅션된 후에 새로운 값이 들어오기 떄문에 중간의 데이터들은 반영안되는 문제
                BytesKey randomKey = bytesKeys.get(r.nextInt(bytesKeys.size()));
                CacheValue randomCacheValue = cacheMemory.get(randomKey);

//                if (randomCacheValue. && extraSize - randomCacheValue.getByteSize() < maxSize) {
//
//                }
            }
        }

        BytesKey expiredKey = expireQueue.pollFirst();
        cacheMemory.remove(expiredKey);

        System.out.println("- Removed key -");
        System.out.println(expiredKey);
        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public byte[] put(byte[] key, byte[] value, Instant timeStamp) {
        final int thisSize = key.length + value.length;

        System.out.println("DEBUG: thisSize = " + thisSize);

        if (curCacaheMemorySize + thisSize >= maxSize) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction(curCacaheMemorySize + thisSize);
        }

        BytesKey putKey = new BytesKey(key);
        CacheValue putValue = new CacheValue(value, timeStamp, thisSize);
        CacheValue returnValue = cacheMemory.put(putKey, putValue);

        if (returnValue == null) {
            return null;
        }

        if (timeStamp.isBefore(returnValue.getTimeStamp())) {
            // Debug
            System.out.println("Timing");
            cacheMemory.put(putKey, returnValue);
            return putValue.getValue();
        }

        return returnValue.getValue();
    }

    public byte[] get(byte[] key, Instant timeStamp) {
        CacheValue returnVal = cacheMemory.get(new BytesKey(key));

        if (returnVal == null) {
            return null;
        }
        returnVal.setTimeStamp(timeStamp);

        return returnVal.getValue();
    }

    // remove 시에 expireQueue에서도 지워줘야 함
    public byte[] remove(byte[] key, Instant timeStamp) {
        BytesKey removeKey = new BytesKey(key);
        CacheValue returnVal = cacheMemory.remove(removeKey);

        if (expireQueue.contains(removeKey)) {
            expireQueue.remove(removeKey);
        }
//        if (returnVal == null) {
//            return null;
//        }
//
//        if (removedQueue.contains(removeKey)) {
//            return returnVal.getValue();
//        }
//
//        if (removedQueue.size() > removedQueueSize) {
//            removedQueue.pollFirst();
//        }
//        removedQueue.offerLast(removeKey);
//
//        // Debug
//        System.out.println(removedQueue.toString());

        return returnVal.getValue();
    }
}