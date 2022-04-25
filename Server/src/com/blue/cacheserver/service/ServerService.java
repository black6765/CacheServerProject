package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.client.Client;
import com.blue.cacheserver.message.ErrorMessage;
import com.blue.cacheserver.message.Message;
import com.blue.cacheserver.task.GetTask;
import com.blue.cacheserver.task.PutTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerService {

    private Selector selector;

    private final Charset charset = StandardCharsets.UTF_8;

    private ServerSocketChannel serverSocketChannel;
    private Cache<String, String> cache;

    private int connectionNum;

    private Set<Client> clients;

    protected void runServer() {

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        int keyCount = selector.select();

                        // 클라이언트가 연결을 신청할 때 해당 조건문을 통과함(probably)
                        if (keyCount == 0) {
                            continue;
                        }

//                        System.out.println("클라이언트 연결 신청");

                        Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();

                            // Todo : 각 경우 메소드로 만들기(셀렉터는 accept 요청만 받으므로 필요 없을 수도)
                            if (selectionKey.isAcceptable()) {
                                System.out.println("Accept");
                                accept(selectionKey);
                            } else if (selectionKey.isReadable()) {
                                try {
                                    System.out.println("Read");
                                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                                    socketChannel.configureBlocking(false);
                                    ByteBuffer buf = ByteBuffer.allocate(512);


                                    int byteCount = socketChannel.read(buf);
                                    if (byteCount == -1) {
                                        System.out.println("클라이언트 연결 종료");
                                    }
                                    buf.flip();
                                    String[] input = StandardCharsets.UTF_8.decode(buf).toString().split("\n");

                                    if ("1".equals(input[0])) {

                                        String str = (String) cache.put(input[1], input[2]);
                                        if (str == null) {
                                            socketChannel.write(charset.encode("null"));
                                        } else {
                                            socketChannel.write(charset.encode(str));
                                        }

                                        System.out.println("Put success.");
                                        System.out.println("key = [" + input[1] + "]");
                                        System.out.println("value = [" + input[2] + "]");

                                    } else if ("2".equals(input[0])) {
                                        String str = (String) cache.get(input[1]);
                                        if (str == null) {
                                            socketChannel.write(charset.encode("null"));
                                        } else {
                                            socketChannel.write(charset.encode(str));
                                        }

                                        System.out.println("Get success.");
                                        System.out.println("key = [" + input[1] + "]");
                                    }
                                    selectionKey.cancel();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
//                            else if (selectionKey.isWritable()) {
//                                try {
//                                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//                                    socketChannel.configureBlocking(false);
//
//                                    socketChannel.write(charset.encode("data"));
//                                    selectionKey.interestOps(SelectionKey.OP_READ);
//                                } catch (Exception e) {
//
//                                }
//                            }

                            iterator.remove();
                        }
                    } catch (IOException e) {
                        System.out.println(ErrorMessage.SERVER_RUN_FAILED_MSG);
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        continue;
                    } catch (Exception e) {
                        System.out.println(ErrorMessage.SERVER_RUN_FAILED_MSG);
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                        stopServer();
                        break;
                    }
                }
            }
        };
        thread.start();
    }

    protected void stopServer() {

        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }

            if (selector != null && selector.isOpen()) {
                selector.close();
            }

            System.out.println(Message.SERVER_STOP_MSG);
        } catch (Exception e) {
            System.out.println(ErrorMessage.SERVER_STOP_FAILED_MSG);
            e.getMessage();
            e.printStackTrace();
        }
    }

    protected void startServer() {
        try {
            cache = Cache.getInstance();
            clients = new HashSet<>();
            connectionNum = 0;
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(44001));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(Message.SERVER_START_MSG);
            runServer();
        } catch (IOException e) {
            System.out.println(ErrorMessage.SERVER_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    void accept(SelectionKey selectionKey) {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("클라이언트 정보: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName());
            if (socketChannel != null) {
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
                System.out.println("등록 성공");
            }

        } catch (IOException e) {
            System.out.println("Accept failed");
            System.out.println(e.getMessage());
            e.printStackTrace();

            if (serverSocketChannel.isOpen()) {
                stopServer();
            }
        }
        System.out.println("Accept 메소드 종료");
    }

}
