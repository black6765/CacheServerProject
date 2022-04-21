package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.message.ErrorMessage;
import com.blue.cacheserver.task.ConnectTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerService {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;
    private Cache cache;

    private int connectionNum = 0;

    protected void startServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            cache = Cache.getInstance();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(44001));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.getMessage();
            e.printStackTrace();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
            Thread thread = new Thread() {
                @Override
                public void run() {

                    String threadName = Thread.currentThread().getName();

                    while (true) {

                        try {
                            int keyCount = selector.select();

                            if (keyCount == 0) {
                                continue;
                            }

                            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                            while (iterator.hasNext()) {
                                SelectionKey selectionKey = iterator.next();

                                // Todo : 각 경우 메소드로 만들기
                                if (selectionKey.isAcceptable()) {
                                    try {
                                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                                        SocketChannel clientSocketChannel = serverSocketChannel.accept();

                                        ByteBuffer buf = ByteBuffer.allocate(512);

                                        int byteCount = clientSocketChannel.read(buf);
                                        if (byteCount == -1) {
                                            throw new IOException();
                                        }

                                        buf.flip();
                                        String cmd = Charset.forName("UTF-8").decode(buf).toString();
                                        System.out.println("클라이언트 입력값: " + cmd +
                                                "\n처리 스레드: " + threadName + "\n처리 포트: " + clientSocketChannel.getRemoteAddress());

                                        /* 여기부터 get, put, remove에 대한 각 작업에 대한 if else문
                                         * 각 작업을 정의하고, 마지막에 executorService.submit(작업)으로 스레드 풀에 던짐!!!
                                         *
                                         */

                                        if (!"1".equals(cmd)) {
                                            Runnable runnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (int i = 0; i < 1000; i++) {
                                                        try {
                                                            System.out.println("i = " + i + "\n처리 스레드: " + Thread.currentThread().getName() + "\n처리 포트: " + clientSocketChannel.getRemoteAddress());
                                                        } catch (Exception e) {

                                                        }
                                                    }
                                                }
                                            };

                                            // 현재 코드 상태에서 각 작업이 다른 스레드로 처리되고 있는 것을 확인 가능
                                            executorService.submit(runnable);
                                        }
                                    } catch (Exception e) {
                                        e.getMessage();
                                        e.printStackTrace();
                                    }
//                            } else if (selectionKey.isReadable()) {
//                                ByteBuffer buf = ByteBuffer.allocate(512);
//
//                                SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
//
//                                try {
//                                    clientSocketChannel.read(buf);
//                                    buf.flip();
//                                    System.out.println("클라이언트 입력값: " + Charset.forName("UTF-8").decode(buf));
//                                } catch (Exception e) {
//                                    selectionKey.cancel();
//                                }
                                } else {
                                    System.out.println("원하는 것이 아님");
                                }

                                iterator.remove();
                            }
                        } catch (IOException e) {

                        }
                    }
                }
            };

            thread.start();
    }
}
