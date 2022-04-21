package com.blue.cacheserver.cache;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public class Cache<K, V> {

    private Map<K, V> map = new ConcurrentHashMap<>();

    // Todo : eviction 메소드
    public V eviction(K removeKey) {
        return map.remove(removeKey);
    }

    private Cache() {
        // Singleton Pattern
    }

    private static class LazyHolder {
        public static final Cache INSTANCE = new Cache();
    }

    public static Cache getInstance() {
        return LazyHolder.INSTANCE;
    }
}
