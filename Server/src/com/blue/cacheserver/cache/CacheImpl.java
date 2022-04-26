package com.blue.cacheserver.cache;

import com.blue.cacheserver.message.ServerException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_ALMOST_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;


public class CacheImpl<K, V> implements Cache<K, V>{

    private final int MAX_SIZE = 4;

    // 데이터를 관리할 ConcurrentHashMap cacheMemory
    private Map<K, V> cacheMemory = new ConcurrentHashMap<>();

    // put 시점의 타임스탬프 값을 key, cacheMemory의 key를 value로 하는 TreeMap
    // TreeMap의 특성으로 자동으로 key 값인 타임스탬프(시간) 순으로 정렬됨
    private TreeMap<Long, K> timeStampMap = new TreeMap<>();

    // Todo : eviction 메소드 작성
    public void eviction() {
        System.out.println("\n[Eviction start]");
        Set<Long> keySet = timeStampMap.keySet();
        List<K> removedKeyList = new LinkedList<>();

        // 타임스탬프가 가장 오래된 entry 중 최대 크기의 일정 비율을 삭제 처리
        for (int i = 0; i < (MAX_SIZE / 2); i++) {
            K target = timeStampMap.remove(timeStampMap.firstKey());
            cacheMemory.remove(target);
            removedKeyList.add(target);
        }

        System.out.println("- Removed key list -");
        System.out.println(removedKeyList);
    }

    public V put(K key, V value) {
        if (cacheMemory.size() >= MAX_SIZE) {
            System.out.println(SERVER_CACHE_ALMOST_FULL_MSG);
            eviction();
            System.out.println(SERVER_CACHE_EVICTION_MSG);
        }

        timeStampMap.put(System.currentTimeMillis(), key);
        return cacheMemory.put(key, value);
    }

    public V get(K key) {
        return cacheMemory.get(key);
    }

    public V remove(K key) {
        return cacheMemory.remove(key);
    }

    private CacheImpl() {
        // Forbidden for Singleton Pattern
    }

    // Singleton pattern using "LazyHolder"
    private static class LazyHolder {
        public static final CacheImpl<?, ?> INSTANCE = new CacheImpl<>();
    }

    public static CacheImpl<?, ?> getInstance() {
        return LazyHolder.INSTANCE;
    }
}
