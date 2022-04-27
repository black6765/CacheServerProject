package com.blue.cacheserver.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;


public class CacheImpl<K, V> implements Cache<K, V>{

    private final int MAX_SIZE = 4;

    // 데이터를 관리할 ConcurrentHashMap cacheMemory
    private Map<K, V> cacheMemory = new ConcurrentHashMap<>();

    // delimiter인 "=<-->=" 문자열을 기준으로 앞 부분은 타임스탬프, 뒷 부분은 cacheMemory의 Key 값이 저장
    private List<K> queue = new LinkedList<>();

    List<K> removedKeyList = new LinkedList<>();

    // Todo : eviction 메소드 작성
    public void eviction() {
        System.out.println("\n[Eviction start]");

        // LRU 알고리즘으로 eviction 대상을 삭제 처리
        for (int i = 0; i < (MAX_SIZE / 2); i++) {
            System.out.println(queue); // 디버그
            K target = queue.remove(0);
            cacheMemory.remove(target);
            removedKeyList.add(target);
            System.out.println(queue); // 디버
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
        queue.add(queue.size(), key);

        return cacheMemory.put(key, value);
    }

    public V get(K key) {
        int idx = queue.indexOf(key);
        // Get 연산을 수행한 앤트리는 큐에 다시 넣어서 life-time을 갱신함
        queue.add(queue.size() - 1, queue.remove(idx));

        return cacheMemory.get(key);
    }

    public V remove(K key) {
        int idx = queue.indexOf(key);
        queue.remove(idx);

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
