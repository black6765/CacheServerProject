//package com.blue.cacheserver.task;
//
//import com.blue.cacheserver.cache.Cache;
//import com.blue.cacheserver.message.ErrorMessage;
//import com.blue.cacheserver.message.SuccessMessage;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.SocketChannel;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//
//public class PutTask implements Runnable {
//    SocketChannel clientSocketChannel;
//    Cache<String, String> cache;
//    public PutTask(Cache<String, String> cache, SocketChannel socketChannel) {
//        this.cache = cache;
//        this.clientSocketChannel = socketChannel;
//    }
//
//    @Override
//    public void run() {
//        try {
//            ByteBuffer buf = ByteBuffer.allocate(512);
//            buf.clear();
//            Charset charset = StandardCharsets.UTF_8;
//
//
//            int byteCount = clientSocketChannel.read(buf);
//            if (byteCount == -1) {
//                throw new IOException();
//            }
//
//            buf.flip();
//            String key = charset.decode(buf).toString();
//
//            clientSocketChannel.write(charset.encode("[Server] Success receive Key: (" + key + ")"));
//
//            buf.clear();
//
//            byteCount = clientSocketChannel.read(buf);
//            if (byteCount == -1) {
//                throw new IOException();
//            }
//            buf.flip();
//            String value = charset.decode(buf).toString();
//            clientSocketChannel.write(charset.encode("[Server] Success receive value: (" + value + ")"));
//            System.out.println(key);
//            System.out.println(value);
//
//            cache.put(key, value);
//            buf.clear();
//
//            System.out.println(SuccessMessage.SERVER_PUT_MSG);
//        } catch (Exception e) {
//            System.out.println(ErrorMessage.SERVER_PUT_FAILED_MSG);
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}
