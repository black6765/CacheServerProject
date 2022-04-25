package com.blue.cacheserver.cache;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Cache<K, V> {

    private Map<K, V> map = new ConcurrentHashMap<>();

    // Todo : eviction 메소드 작성
    public V eviction(K removeKey) {
        return map.remove(removeKey);
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    public V get(K key) {
        return map.get(key);
    }

    public V remove(K key) {
        return map.remove(key);
    }

    private Cache() {
        // Forbidden for Singleton Pattern
    }

    private static class LazyHolder {
        public static final Cache INSTANCE = new Cache();
    }

    public static Cache getInstance() {
        return LazyHolder.INSTANCE;
    }
}
