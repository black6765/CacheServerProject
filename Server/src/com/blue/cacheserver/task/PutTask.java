package com.blue.cacheserver.task;

import com.blue.cacheserver.cache.Cache;

import java.nio.channels.SocketChannel;

public class PutTask implements Runnable {
    SocketChannel clientSocketChannel;
    Cache cache;
    public PutTask(Cache cache, SocketChannel socketChannel) {
        this.cache = cache;
        this.clientSocketChannel = socketChannel;
    }

    @Override
    public void run() {

    }
}
