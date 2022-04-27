package com.blue.cacheserver.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;

// Cache의 기본 구현체로, eviction에 대해서 LRU 알고리즘 적용
public class CacheImpl<K, V> implements Cache<K, V> {

    private final int MAX_SIZE = 4;

    // 데이터를 관리할 ConcurrentHashMap cacheMemory
    private Map<K, V> cacheMemory = new ConcurrentHashMap<>();

    /*
     * removeFirst(), addLast() 메소드를 사용하기 위해 LinkedList
     * eviction과 관련하여 life-time 순의 큐 자료구조
     * put 연산 시에 큐 마지막에 key가 저장됨
     * eviction을 실행할 때 큐의 첫 번째 인덱스에서 제거
     * get과 remove 연산 시에 인덱스 접근을 하기 때문에 완전한 큐 자료구조는 아님
     */
    private LinkedList<K> queue = new LinkedList<>();

    // 삭제된 키들을 출력하기 위한 list
    List<K> removedKeyList = new LinkedList<>();

    public void eviction() {
        System.out.println("\n[Eviction start]");

        // LRU 알고리즘으로 eviction 대상을 삭제 처리
        for (int i = 0; i < (MAX_SIZE / 2); i++) {
//            System.out.println(queue); // 디버그
            K target = queue.removeFirst();
            cacheMemory.remove(target);
            removedKeyList.add(target);
//            System.out.println(queue); // 디버그
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
        queue.addLast(key);

        return cacheMemory.put(key, value);
    }

    public V get(K key) {
        int idx = queue.indexOf(key);

        if (idx != -1) {
            // Get 연산을 수행한 앤트리는 큐에 다시 넣어서 life-time을 갱신함
            K refreshKey = queue.remove(idx);
            queue.addLast(refreshKey);
        }


        return cacheMemory.get(key);
    }

    public V remove(K key) {
        int idx = queue.indexOf(key);
        if (idx != -1) {
            queue.remove(idx);
        }

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