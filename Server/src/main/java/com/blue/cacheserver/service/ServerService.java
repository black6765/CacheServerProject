package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.BytesKey;
import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.cache.CacheValue;
import com.blue.cacheserver.message.DisconnectException;
import com.blue.cacheserver.message.ServerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.blue.cacheserver.message.ErrorMessage.*;
import static com.blue.cacheserver.message.Message.*;

public class ServerService {
    private final Charset charset = StandardCharsets.UTF_8;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Cache cache;

    private boolean runThread = true;

    private final ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(2);

    public Cache getCache() {
        return cache;
    }

    private void runServer() {
        Thread serverThread = new Thread(() -> {
            while (runThread) {
                try {
                    int keyCount = selector.select();

                    if (keyCount == 0) {
                        continue;
                    }

                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();

                        if (selectionKey.isAcceptable()) {
                            accept();
                        } else if (selectionKey.isReadable()) {
                            receive(selectionKey);
                        } else {
                            throw new IOException();
                        }

                        iterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println(SERVER_RUN_FAILED_MSG);
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        serverThread.setName("runServerThread");
        serverThread.start();

        Runnable expireRunnable = () -> {
            try {
                for (ConcurrentHashMap.Entry<BytesKey, CacheValue> entry : cache.getCacheMemory().entrySet()) {
                    BytesKey entryKey = entry.getKey();
                    CacheValue entryValue = entry.getValue();

                    long elapsedTime = Instant.now().toEpochMilli() - entryValue.getTimeStamp().toEpochMilli();

                    if (!entryValue.isExpired() && (elapsedTime >= cache.getExpireMilliSecTime())) {
                        entryValue.setExpired(true);
                        entryValue.setExpireTimeStamp(Instant.now());

                        Deque<BytesKey> expiredQueue = cache.getExpireQueue();
                        if (expiredQueue.size() >= cache.getExpireQueueSize()) {
                            expiredQueue.pollFirst();
                        }

                        cache.getExpireQueue().offerLast(entryKey);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        };

        scheduleService.scheduleAtFixedRate(
                expireRunnable,
                0,
                cache.getExpireCheckSecTime(),
                TimeUnit.MILLISECONDS);

        Runnable removeExpiredEntryRunnable = () -> {
            try {
                cache.removeAllExpiredEntry();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        };

        if (cache.getRemoveAllExpiredEntryTime() != 0) {
            scheduleService.scheduleAtFixedRate(
                    removeExpiredEntryRunnable,
                    5000,
                    cache.getRemoveAllExpiredEntryTime(),
                    TimeUnit.MILLISECONDS);
        }
    }

    public void stopServer() {
        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }

            if (selector != null && selector.isOpen()) {
                selector.close();
            }

            runThread = false;
            scheduleService.shutdown();

            System.out.println(SERVER_STOP_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_STOP_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public ServerService() {
        try {
            cache = new Cache.Builder()
                    .maxSize(81920)
                    .initSize(64)
                    .expireMilliSecTime(6000)
                    .expireCheckMilliSecTime(1)
                    .removeAllExpiredEntryTime(30000)
                    .expireQueueSize(30)
                    .build();

            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(44001));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(SERVER_START_MSG);

            runServer();
        } catch (IOException e) {
            System.out.println(SERVER_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void accept() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("\n[Client Info: " + socketChannel.getRemoteAddress() + "]");

            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);

        } catch (Exception e) {
            System.out.println(SERVER_ACCEPT_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();

            if (serverSocketChannel.isOpen()) {
                stopServer();
            }
        }
    }

    public void receive(SelectionKey selectionKey) {
        try {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            socketChannel.configureBlocking(false);
            ByteBuffer buf = ByteBuffer.allocate(512);
            final int byteCount = socketChannel.read(buf);

            if (byteCount == -1) {
                throw new DisconnectException("Close the connection");
            }

            buf.flip();

            byte[] bytesBuf = new byte[byteCount];
            buf.get(bytesBuf);

            int[] splitIdx = new int[2];
            int cntDelim = 0;

            for (int i = 0; i < bytesBuf.length; i++) {
                if (bytesBuf[i] == '\n' && bytesBuf[i + 1] == '\n') {
                    splitIdx[cntDelim++] = i;
                }
            }

            final byte[] operationBytes = Arrays.copyOfRange(bytesBuf, 0, splitIdx[0]);
            final String operation = (String) deserialize(operationBytes);
            selectOP(socketChannel, bytesBuf, splitIdx, operation);

        } catch (DisconnectException e) {
            selectionKey.cancel();
            System.out.println(SERVER_CLIENT_DISCONNECT_MSG);
        } catch (IOException e) {
            selectionKey.cancel();
            System.out.println(SERVER_RECEIVE_FAILED_MSG);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            stopServer();
        }
    }

    private void selectOP(SocketChannel socketChannel, byte[] bytes, int[] splitIdx, String op) {
        try {
            byte[] keyBytes;
            byte[] valueBytes;
            final String DELIMITER = "\n\n";

            if ("put".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), splitIdx[1]);
                valueBytes = Arrays.copyOfRange(bytes, splitIdx[1] + DELIMITER.length(), bytes.length);
                putOperation(socketChannel, keyBytes, valueBytes);
            } else if ("get".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), bytes.length);
                getOperation(socketChannel, keyBytes);
            } else if ("remove".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), bytes.length);
                removeOperation(socketChannel, keyBytes);
            } else if ("exit".equals(op)) {
                throw new DisconnectException("Close the connection");
            } else {
                throw new ServerException("Client request not supported operation");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void putOperation(SocketChannel socketChannel, byte[] keyBytes, byte[] valueBytes) {
        try {
            byte[] returnVal = cache.put(keyBytes, valueBytes, Instant.now());

            if (returnVal == null) {
                socketChannel.write(charset.encode("null"));
            } else {
                socketChannel.write(ByteBuffer.wrap(returnVal));
            }

            System.out.println(SERVER_PUT_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_PUT_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void getOperation(SocketChannel socketChannel, byte[] keyBytes) {
        try {
            byte[] returnVal = cache.get(keyBytes, Instant.now());

            if (returnVal == null) {
                socketChannel.write(charset.encode("null"));
            } else {
                socketChannel.write(ByteBuffer.wrap(returnVal));
            }

            System.out.println(SERVER_GET_MSG);

        } catch (Exception e) {
            System.out.println(SERVER_GET_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void removeOperation(SocketChannel socketChannel, byte[] keyBytes) {
        try {
            byte[] returnVal = cache.remove(keyBytes);

            if (returnVal == null) {
                socketChannel.write(charset.encode("null"));
            } else {
                socketChannel.write(ByteBuffer.wrap(returnVal));
            }

            System.out.println(SERVER_REMOVE_MSG);

        } catch (Exception e) {
            System.out.println(SERVER_REMOVE_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }
}
