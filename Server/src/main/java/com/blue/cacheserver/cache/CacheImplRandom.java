//package com.blue.cacheserver.cache;
//
//import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
//import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;
//
//// 구현중
//public class CacheImplRandom<K, V> extends CacheImpl<K, V> {
//    @Override
//    public void eviction() {
//        System.out.println("\n[Eviction start]");
//
//        // LRU 알고리즘으로 eviction 대상을 삭제 처리
//        for (int i = 0; i < (MAX_SIZE / 2); i++) {
//            K target = (int) (Math.random() * cacheMemory.size());
//            cacheMemory.remove(target);
//            removedKeyList.add(target);
//        }
//
//        System.out.println("- Removed key list -");
//        System.out.println(removedKeyList);
//        removedKeyList.clear();
//        System.out.println(SERVER_CACHE_EVICTION_MSG);
//    }
//
//    @Override
//    public V put(K key, V value) {
//        if (cacheMemory.size() >= MAX_SIZE) {
//            System.out.println(SERVER_CACHE_FULL_MSG);
//            eviction();
//        }
//
//        return cacheMemory.put(key, value);
//    }
//
//    @Override
//    public V get(K key) {
//        return cacheMemory.get(key);
//    }
//
//    @Override
//    public V remove(K key) {
//        return cacheMemory.remove(key);
//    }
//
//    private CacheImplRandom() {
//        // Forbidden for Singleton Pattern
//    }
//
//    // Singleton pattern using "LazyHolder"
//
//    private static class LazyHolder {
//        public static final CacheImplRandom<?, ?> INSTANCE = new CacheImplRandom<>();
//    }
//
//    public static CacheImplRandom<?, ?> getInstance() {
//        return CacheImplRandom.LazyHolder.INSTANCE;
//    }
//
//}
