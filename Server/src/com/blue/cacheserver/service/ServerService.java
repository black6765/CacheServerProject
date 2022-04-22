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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerService {

    private Selector selector;

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

                        System.out.println("클라이언트 연결 신청");

                        Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();

                            // Todo : 각 경우 메소드로 만들기(셀렉터는 accept 요청만 받으므로 필요 없을 수도)
                            if (selectionKey.isAcceptable()) {
                                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                                SocketChannel clientSocketChannel = serverSocketChannel.accept();

                                System.out.println("클라이언트 연결 완료");
                                Client client = new Client(clientSocketChannel);
                                clients.add(client);

                                System.out.println(client);
                                ByteBuffer buf = ByteBuffer.allocate(512);

                                int byteCount = clientSocketChannel.read(buf);
                                if (byteCount == -1) {
                                    throw new IOException();
                                }

                                buf.flip();
                                String cmd = StandardCharsets.UTF_8.decode(buf).toString();

                                if ("1".equals(cmd)) {
                                    Runnable putTask = new PutTask(cache, clientSocketChannel);

                                    // 현재 코드 상태에서 각 작업이 다른 스레드로 처리되고 있는 것을 확인 가능
                                    executorService.submit(putTask);
                                } else if ("2".equals(cmd)) {
                                    Runnable getTask = new GetTask(cache, clientSocketChannel);
                                    executorService.submit(getTask);
                                }
                                System.out.println(Message.SERVER_SUBMIT_MSG);
                                buf.clear();
                            } else {
                                System.out.println("원하는 것이 아님");
                            }

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
            clients = new HashSet<>();
            connectionNum = 0;
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();
            cache = Cache.getInstance();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(44001));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(Message.SERVER_START_MSG);
            runServer();
        } catch (IOException e) {
            System.out.println(ErrorMessage.SERVER_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
