package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CACHE_FULL_MSG;
import static com.blue.cacheserver.message.Message.SERVER_CACHE_EVICTION_MSG;

public class Cache {

    final int MAX_SIZE = 512;
    final int INIT_SIZE = MAX_SIZE / 2;

    Map<BytesKey, CacheValue> cacheMemory = new ConcurrentHashMap<>(INIT_SIZE);

    LinkedList<BytesKey> evictionQueue = new LinkedList<>();

    public void eviction() {
//        System.out.println("\n[Eviction start]");
//
//        for (int i = 0; i < (MAX_SIZE / 2); i++) {
//            K target = evictionQueue.removeFirst();
//            cacheMemory.remove(target);
//            removedKeyList.add(target);
//        }
//
//        System.out.println("- Removed key list -");
//        System.out.println(removedKeyList);
//        removedKeyList.clear();
//        System.out.println(SERVER_CACHE_EVICTION_MSG);
    }

    public byte[] put(byte[] key, byte[] value) {
        final int thisSize = key.length + value.length;

        if (cacheMemory.size() + thisSize >= MAX_SIZE) {
            System.out.println(SERVER_CACHE_FULL_MSG);
            eviction();
        }

        CacheValue returnVal = cacheMemory.put(new BytesKey(key), new CacheValue(value, Instant.now()));

        if (returnVal == null)
            return null;

        System.out.println(returnVal.getValue());
        return returnVal.getValue();
    }

    public byte[] get(BytesKey key) {
//        int idx = evictionQueue.indexOf(key);
//
//        if (idx != -1) {
//            K refreshKey = evictionQueue.remove(idx);
//            evictionQueue.addLast(refreshKey);
//        }
//
        return cacheMemory.get(key).getValue();
    }

    public byte[] remove(BytesKey key) {
        int idx = evictionQueue.indexOf(key);
        if (idx != -1) {
            evictionQueue.remove(idx);
        }

        return cacheMemory.remove(key).getValue();
    }

    public Cache() {

    }
}