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
    private int expireQueueSize;
    private final int EVICTION_SAMPLE_SIZE = 5;

    private int curCacaheMemorySize = 0;

    private Cache(Builder builder) {
        maxSize = builder.maxSize;
        initSize = builder.initSize;
        expireMilliSecTime = builder.expireRegistMilliSecTime;
        expireCheckSecTime = builder.expireCheckMilliSecTime;
        expireQueueSize = builder.expireQueueSize;
    }

    public static class Builder {
        private int maxSize = 128;
        private int initSize = 64;
        private long expireRegistMilliSecTime = 6000;
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
            expireRegistMilliSecTime = val;
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
    Deque<BytesKey> evictionSampleQueue = new ConcurrentLinkedDeque<>();

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

    public int getExpireQueueSize() {
        return expireQueueSize;
    }

    // extraSize 쪽 변경필요.
    public void eviction(int extraSize) {
        System.out.println("\n[Eviction start]");

        while (extraSize + curCacaheMemorySize > maxSize) {
            if (expireQueue.isEmpty()) {

                List<BytesKey> bytesKeys = new ArrayList<>(cacheMemory.keySet());

                // 샘플을 Math.min(5, curSize)개 만들 때 까지 while (true)
                // 각 샘플을 리스트로 새로운 리스트로 추가
                // 이때 샘플은 extraSize - randomCacheValue.getByteSize() < maxSize 조건을 만족시켜야함
                // 샘플 리스트에서 timeStamp가 가장 오래된 것을 최종적으로 삭제
                // ! 랜덤으로 샘플링할 필요가 있을까?
                // ! 그냥 작은 사이즈의 큐를 두고 가장 오래된 타임스탬프를 관리하면...
                // ! 처음에 오래된 5개가 쌓인 후 eviction 될 떄 까지 갱신되지 않다가
                // ! 이빅션된 후에 새로운 값이 들어오기 떄문에 중간의 데이터들은 반영안되는 문제
                int repeatNum = Math.min(EVICTION_SAMPLE_SIZE, cacheMemory.size());
                int repeatCount = 0;

                // 일반적으로 참이 될 수 없는 조건문
                if (cacheMemory.isEmpty()) {
                    System.out.println(cacheMemory.isEmpty());
                    break;
                }

                while (true) {
                    if (repeatCount == repeatNum) {
                        break;
                    }
                    Random random = new Random();
                    BytesKey randomKey = bytesKeys.get(random.nextInt(bytesKeys.size()));

                    // 이 부분에서 계속 반복되는 것으로 확인 고쳐야함.
                    if (evictionSampleQueue.contains(randomKey)) {
                        continue;
                    }

                    evictionSampleQueue.offerLast(randomKey);
                    repeatCount++;

                }

//                System.out.println("DEBUG: evictionSampleQueue = " + evictionSampleQueue);

                BytesKey oldestKey = evictionSampleQueue.pollFirst();
                CacheValue oldestCacheValue = cacheMemory.get(oldestKey);
                for (BytesKey key : evictionSampleQueue) {
                    CacheValue cacheValue = cacheMemory.get(key);
                    if (cacheValue.getTimeStamp().isBefore(oldestCacheValue.getTimeStamp())) {
                        oldestKey = key;
                        oldestCacheValue = cacheValue;
                    }
                }


//                System.out.println("oldestKey = " + oldestKey);
                cacheMemory.remove(oldestKey);

                curCacaheMemorySize -= oldestCacheValue.getByteSize();
//                System.out.println("oldestCacheValue = " + oldestCacheValue.getByteSize());
//                System.out.println(curCacaheMemorySize);

                evictionSampleQueue.clear();

            } else {

                BytesKey expiredKey = expireQueue.pollFirst();
                curCacaheMemorySize -= cacheMemory.get(expiredKey).getByteSize();
                cacheMemory.remove(expiredKey);
                System.out.println("Debug: expiredKey = " + expiredKey);

                System.out.println("- Removed key -");
                System.out.println(expiredKey);
                System.out.println(SERVER_CACHE_EVICTION_MSG);
            }
        }
        return;
    }

    public byte[] put(byte[] key, byte[] value, Instant timeStamp) {
        final int thisSize = key.length + value.length;

        if (thisSize > maxSize) {
            System.out.println("Input data is bigger than max cache size");
            return null;
        }

        System.out.println("DEBUG: thisSize = " + thisSize);
        System.out.println("DEBUG: curCacheMemorySize = " + curCacaheMemorySize);

        if (curCacaheMemorySize + thisSize > maxSize) {
            System.out.println(curCacaheMemorySize + thisSize);
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

        return returnVal.getValue();
    }
}