package com.blue.cacheserver.cache;

public interface Cache<K, V> {
    void eviction();

    public V put(K key, V value);
    public V get(K key);
    public V remove(K key);
}
