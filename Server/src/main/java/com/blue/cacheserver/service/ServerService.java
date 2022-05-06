package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.BytesKey;
import com.blue.cacheserver.cache.Cache;
import com.blue.cacheserver.cache.CacheValue;
import com.blue.cacheserver.message.DisconnectException;
import com.blue.cacheserver.message.ServerException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
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

    private final ScheduledExecutorService scheduleService = Executors.newSingleThreadScheduledExecutor();

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
                Map<BytesKey, CacheValue> cacheMemory = cache.getCacheMemory();
                for (ConcurrentHashMap.Entry<BytesKey, CacheValue> entry : cache.getCacheMemory().entrySet()) {
                    BytesKey entryKey = entry.getKey();
                    CacheValue entryValue = entry.getValue();

                    long elapsedTime = Instant.now().toEpochMilli() - entryValue.getExpireTimeStamp().toEpochMilli();

                    if (elapsedTime >= cache.getExpireTime()) {
                        cacheMemory.remove(entryKey);
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
                cache.getExpireCheckTime(),
                TimeUnit.MILLISECONDS);
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
                    .maxSize(1024)
                    .initSize(32)
                    .expireTime(6000)
                    .expireCheckTime(100)
                    .build();

            System.out.println(cache.initSettingToString());

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
            final int BUF_CAPACITY = 1024;
            ByteBuffer buf = ByteBuffer.allocate(BUF_CAPACITY);
            final int byteCount = socketChannel.read(buf);

            if (byteCount == -1) {
                throw new DisconnectException("Close the connection");
            }

            if (byteCount >= BUF_CAPACITY) {
                return;
            }

            buf.flip();

            byte[] bytesBuf = new byte[byteCount];
            buf.get(bytesBuf);

            int[] splitIdx = new int[3];
            int cntDelim = 0;

            for (int i = 0; i < bytesBuf.length; i++) {
                if (bytesBuf[i] == '\n' && bytesBuf[i + 1] == '\n') {
                    splitIdx[cntDelim++] = i;
                }
            }

            final byte[] operationBytes = Arrays.copyOfRange(bytesBuf, 0, splitIdx[0]);
            final String operation = (String) deserialize(operationBytes);
            selectOP(socketChannel, bytesBuf, splitIdx, operation);

        } catch (EOFException e) {
            System.out.println(SERVER_RECEIVE_TOO_LARGE_DATA_MSG);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                socketChannel.write(charset.encode("null"));
            } catch (IOException e1) {
                selectionKey.cancel();
            }
        } catch (StreamCorruptedException e) {
            System.out.println(SERVER_RECEIVE_TOO_LARGE_DATA_MSG);
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                socketChannel.write(charset.encode("null"));
            } catch (IOException e1) {
                selectionKey.cancel();
            }
        } catch (DisconnectException e) {
            selectionKey.cancel();
            System.out.println(SERVER_CLIENT_DISCONNECT_MSG);
        } catch (SocketException e) {
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
            byte[] timeStampBytes;
            final String DELIMITER = "\n\n";

            if ("put".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), splitIdx[1]);
                valueBytes = Arrays.copyOfRange(bytes, splitIdx[1] + DELIMITER.length(), splitIdx[2]);
                timeStampBytes = Arrays.copyOfRange(bytes, splitIdx[2] + DELIMITER.length(), bytes.length);
                putOperation(socketChannel, keyBytes, valueBytes, timeStampBytes);
            } else if ("get".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), splitIdx[1]);
                timeStampBytes = Arrays.copyOfRange(bytes, splitIdx[1] + DELIMITER.length(), bytes.length);
                getOperation(socketChannel, keyBytes, timeStampBytes);
            } else if ("remove".equals(op)) {
                keyBytes = Arrays.copyOfRange(bytes, splitIdx[0] + DELIMITER.length(), splitIdx[1]);
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

    public void putOperation(SocketChannel socketChannel, byte[] keyBytes, byte[] valueBytes, byte[] timeStampBytes) {
        try {
            byte[] returnVal = cache.put(keyBytes, valueBytes, (Instant) deserialize(timeStampBytes));

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

    private void getOperation(SocketChannel socketChannel, byte[] keyBytes, byte[] timeStampBytes) {
        try {
            byte[] returnVal = cache.get(keyBytes, (Instant) deserialize(timeStampBytes));

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
