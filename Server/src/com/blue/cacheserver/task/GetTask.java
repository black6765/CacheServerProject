package com.blue.cacheserver.task;

import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.message.ErrorMessage;
import com.blue.cacheserver.message.Message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GetTask implements Runnable {

    SocketChannel clientSocketChannel;
    Cache<String, String> cache;

    public GetTask(Cache<String, String> cache, SocketChannel socketChannel) {
        this.cache = cache;
        this.clientSocketChannel = socketChannel;
    }
    @Override
    public void run() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(512);
            Charset charset = StandardCharsets.UTF_8;


            int byteCount = clientSocketChannel.read(buf);
            if (byteCount == -1) {
                throw new IOException();
            }

            buf.flip();
            String key = charset.decode(buf).toString();

            clientSocketChannel.write(charset.encode("[Server] Success receive Key: (" + key + ")"));

            buf.clear();

            clientSocketChannel.write(charset.encode("[Server] Get operation return value: (" + cache.get(key) + ")"));

            System.out.println(Message.SERVER_GET_MSG);
        } catch (Exception e) {
            System.out.println(ErrorMessage.SERVER_GET_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
