package com.blue.cacheserver.cache;

// CacheImpl을 상속하여, eviction에 대해서 FIFO 알고리즘 적용
public class CacheImplFIFO<K, V> extends CacheImpl<K, V> {
    @Override
    public V get(K key) {
        return cacheMemory.get(key);
    }

    private CacheImplFIFO() {
        // Forbidden for Singleton Pattern
    }

    // Singleton pattern using "LazyHolder"
    private static class LazyHolderFIFO {
        public static final CacheImplFIFO<?, ?> INSTANCE = new CacheImplFIFO<>();
    }

    public static CacheImplFIFO<?, ?> getInstance() {
        return CacheImplFIFO.LazyHolderFIFO.INSTANCE;
    }

}
