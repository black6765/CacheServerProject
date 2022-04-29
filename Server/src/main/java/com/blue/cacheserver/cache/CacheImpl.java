package com.blue.cacheserver.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;

public class CacheImpl<K, V> implements Cache<K, V> {

    final int MAX_SIZE = 8;
    final int INIT_SIZE = MAX_SIZE / 2;

    Map<K, V> cacheMemory = new ConcurrentHashMap<>(INIT_SIZE);

    LinkedList<K> evictionQueue = new LinkedList<>();

    List<K> removedKeyList = new LinkedList<>();

    public void eviction() {
        System.out.println("\n[Eviction start]");

        for (int i = 0; i < (MAX_SIZE / 2); i++) {
            K target = evictionQueue.removeFirst();
            cacheMemory.remove(target);
            removedKeyList.add(target);
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
        evictionQueue.addLast(key);

        return cacheMemory.put(key, value);
    }

    public V get(K key) {
        int idx = evictionQueue.indexOf(key);

        if (idx != -1) {
            K refreshKey = evictionQueue.remove(idx);
            evictionQueue.addLast(refreshKey);
        }

        return cacheMemory.get(key);
    }

    public V remove(K key) {
        int idx = evictionQueue.indexOf(key);
        if (idx != -1) {
            evictionQueue.remove(idx);
        }

        return cacheMemory.remove(key);
    }

    public CacheImpl() {

    }
}