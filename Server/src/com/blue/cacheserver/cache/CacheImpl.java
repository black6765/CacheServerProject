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
    private TreeSet<String> timeStampKeySet = new TreeSet<>(new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            // "=<-->=" 문자열은 타임스탬프와 키를 구분하기 위한 delimiter
            String[] str1 = o1.split("=<-->=");
            String[] str2 = o2.split("=<-->=");

            if (str1.length >= 2 && str2.length >= 2 && str1[0].equals(str2[0])) {
                return str1[1].compareTo(str2[1]);
            }

            return Long.compare(Long.parseLong(str1[0]), Long.parseLong(str2[0]));
        }
    });

    // Todo : eviction 메소드 작성
    public void eviction() {
        System.out.println("\n[Eviction start]");
        List<K> removedKeyList = new LinkedList<>();

        // 타임스탬프가 가장 오래된 entry 중 최대 크기의 일정 비율을 삭제 처리
        for (int i = 0; i < (MAX_SIZE / 2); i++) {
            System.out.println(timeStampKeySet);
            String str = timeStampKeySet.first();
            String[] splitStr = str.split("=<-->=");
            timeStampKeySet.remove(str);
            K target = (K) splitStr[1];
            cacheMemory.remove(target);
            removedKeyList.add(target);
            System.out.println(timeStampKeySet);
        }

        System.out.println("- Removed key list -");
        System.out.println(removedKeyList);
    }

    public V put(K key, V value) {
        if (cacheMemory.size() >= MAX_SIZE) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction();
            System.out.println(SERVER_CACHE_EVICTION_MSG);
        }

        // "=<-->=" 문자열은 타임스탬프와 키를 구분하기 위한 delimiter
        timeStampKeySet.add(String.valueOf(System.nanoTime()) + "=<-->=" + key);
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
