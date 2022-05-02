package com.blue.cacheserver.cache;

import java.nio.charset.StandardCharsets;
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
    private long removeAllExpiredEntryTime = 0;
    private int expireQueueSize;
    private final int EVICTION_SAMPLE_SIZE = 5;

    private int curCacaheMemorySize = 0;

    private Cache(Builder builder) {
        maxSize = builder.maxSize;
        initSize = builder.initSize;
        expireMilliSecTime = builder.expireMilliSecTime;
        expireCheckSecTime = builder.expireCheckMilliSecTime;
        removeAllExpiredEntryTime = builder.removeAllExpiredEntryTime;
        expireQueueSize = builder.expireQueueSize;
    }

    public static class Builder {
        private int maxSize = 128;
        private int initSize = 64;
        private long expireMilliSecTime = 6000;
        private long expireCheckMilliSecTime = 3000;
        private long removeAllExpiredEntryTime = 0;
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
            expireMilliSecTime = val;
            return this;
        }

        public Builder expireCheckMilliSecTime(long val) {
            expireCheckMilliSecTime = val;
            return this;
        }

        public Builder removeAllExpiredEntryTime(long val) {
            removeAllExpiredEntryTime = val;
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
    Deque<BytesKey> evictionSampleQueue = new ConcurrentLinkedDeque<>();

    public long getExpireMilliSecTime() {
        return expireMilliSecTime;
    }

    public long getExpireCheckSecTime() {
        return expireCheckSecTime;
    }

    public long getRemoveAllExpiredEntryTime() {
        return removeAllExpiredEntryTime;
    }

    public Map<BytesKey, CacheValue> getCacheMemory() {
        return cacheMemory;
    }

    public Deque<BytesKey> getExpireQueue() {
        return expireQueue;
    }

    public int getExpireQueueSize() {
        return expireQueueSize;
    }

    public void eviction(int extraSize) {
        System.out.println("\n[Eviction start]");

        // 캐시 사이즈에 여유가 생길 때 까지 반복
        while (extraSize + curCacaheMemorySize > maxSize) {
            if (expireQueue.isEmpty()) {

                List<BytesKey> bytesKeys = new ArrayList<>(cacheMemory.keySet());

                // 샘플을 Math.min(EVICTION_SAMPLE_SIZE, 현재 cacheMemory 원소 개수)개 만들 때 까지 while (true)
                // 샘플 리스트에서 timeStamp가 가장 오래된 것을 최종적으로 삭제
                int repeatNum = Math.min(EVICTION_SAMPLE_SIZE, cacheMemory.size());
                int repeatCount = 0;

                while (true) {
                    if (repeatCount == repeatNum) {
                        break;
                    }
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

                curCacaheMemorySize -= oldestCacheValue.getByteSize();
                System.out.println(curCacaheMemorySize);

                evictionSampleQueue.clear();

            } else {
                BytesKey expiredKey = expireQueue.pollFirst();
                curCacaheMemorySize -= cacheMemory.get(expiredKey).getByteSize();
                cacheMemory.remove(expiredKey);
                System.out.println(expiredKey);
            }
        }
        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public byte[] put(byte[] key, byte[] value, Instant timeStamp) {
        final int thisSize = key.length + value.length;

        if (thisSize > maxSize) {
            System.out.println("Input data is bigger than max cache size");
            return null;
        }

        if (curCacaheMemorySize + thisSize > maxSize) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction(curCacaheMemorySize + thisSize - maxSize);
        }

        BytesKey putKey = new BytesKey(key);
        CacheValue putValue = new CacheValue(value, timeStamp, thisSize);
        CacheValue returnValue = cacheMemory.put(putKey, putValue);

        if (returnValue == null) {
            curCacaheMemorySize += thisSize;
            return null;
        }

        // 새로 갱신된 value의 크기를 더하고, 이전 value의 크기를 뺌
        curCacaheMemorySize = curCacaheMemorySize + putValue.getByteSize() - returnValue.getByteSize();

        if (returnValue.isExpired()) {
            expireQueue.remove(putKey);
            return "Expired key".getBytes(StandardCharsets.UTF_8);
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

        if (returnVal.isExpired()) {
            curCacaheMemorySize -= returnVal.getByteSize();
            BytesKey removeKey = new BytesKey(key);
            cacheMemory.remove(removeKey);
            expireQueue.remove(removeKey);
            return "Expired key".getBytes(StandardCharsets.UTF_8);
        }

        returnVal.setTimeStamp(timeStamp);

        return returnVal.getValue();
    }

    // remove 시에 expireQueue에서도 지워줘야 함
    public byte[] remove(byte[] key, Instant timeStamp) {
        BytesKey removeKey = new BytesKey(key);
        CacheValue returnVal = cacheMemory.remove(removeKey);

        if (returnVal == null) {
            return null;
        }

        if (expireQueue.contains(removeKey)) {
            expireQueue.remove(removeKey);
        }

        curCacaheMemorySize -= returnVal.getByteSize();

        if (returnVal.isExpired()) {
            expireQueue.remove(removeKey);
            return "Expired key".getBytes(StandardCharsets.UTF_8);
        }

        return returnVal.getValue();
    }

    public int removeAllExpiredEntry() {
        int removedSize = 0;
        for (ConcurrentHashMap.Entry<BytesKey, CacheValue> entry : this.getCacheMemory().entrySet()) {
            BytesKey entryKey = entry.getKey();
            CacheValue entryValue = entry.getValue();

            if (entryValue.isExpired()) {
                cacheMemory.remove(entryKey);
                expireQueue.clear();
                curCacaheMemorySize -= entryValue.getByteSize();
                removedSize += entryValue.getByteSize();
            }
        }

        System.out.println("Remove all expired entries. " + removedSize + " bytes removed");
        return curCacaheMemorySize;
    }

}