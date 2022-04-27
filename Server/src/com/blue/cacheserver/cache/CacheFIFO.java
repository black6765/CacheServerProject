package com.blue.cacheserver.cache;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;

// Cache의 구현체로, eviction에 대해서 FIFO 알고리즘 적용
public class CacheFIFO<K, V> implements Cache<K, V> {
    private final int MAX_SIZE = 4;

    // 데이터를 관리할 ConcurrentHashMap cacheMemory
    private Map<K, V> cacheMemory = new ConcurrentHashMap<>();

    private List<K> expireQueue = new LinkedList<>();
    List<K> removedKeyList = new LinkedList<>();

    public void eviction() {
        System.out.println("\n[Eviction start]");

        // LRU 알고리즘으로 eviction 대상을 삭제 처리
        for (int i = 0; i < (MAX_SIZE / 2); i++) {
            System.out.println(expireQueue); // 디버그
            K target = expireQueue.remove(0);
            cacheMemory.remove(target);
            removedKeyList.add(target);
            System.out.println(expireQueue); // 디버그
        }

        System.out.println("- Removed key list -");
        System.out.println(removedKeyList);
        removedKeyList.clear();
        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public V put(K key, V value) {
        if (cacheMemory.size() >= MAX_SIZE) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction();

        }
        expireQueue.add(expireQueue.size(), key);

        return cacheMemory.put(key, value);
    }

    public V get(K key) {
        // FIFO 알고리즘에서는 put 기준으로 반드시 먼저 들어온 데이터부터 제거
        // 따라서 life-time의 갱신이 없음
        return cacheMemory.get(key);
    }

    public V remove(K key) {
        int idx = expireQueue.indexOf(key);
        if (idx != -1) {
            expireQueue.remove(idx);
        }

        return cacheMemory.remove(key);
    }

    private CacheFIFO() {
        // Forbidden for Singleton Pattern
    }

    // Singleton pattern using "LazyHolder"
    private static class LazyHolderFIFO {
        public static final CacheFIFO<?, ?> INSTANCE = new CacheFIFO<>();
    }

    public static CacheFIFO<?, ?> getInstance() {
        return CacheFIFO.LazyHolderFIFO.INSTANCE;
    }

}
