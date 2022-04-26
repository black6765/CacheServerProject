package com.blue.cacheserver.service;

import com.blue.cacheserver.cache.Cache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.blue.cacheserver.message.ErrorMessage.*;
import static com.blue.cacheserver.message.SuccessMessage.*;

public class ServerService {

    private Selector selector;
    private final Charset charset = StandardCharsets.UTF_8;
    private ServerSocketChannel serverSocketChannel;
    private Cache<String, String> cache;

    /**
     * 별도의 스레드로 Server를 동작시킴
     */
    protected void runServer() {
        Thread thread = new Thread(() -> {
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

                        if (selectionKey.isAcceptable()) {
                            accept();
                        } else if (selectionKey.isReadable()) {
                            receive(selectionKey);
                        } else {
                            System.out.println("Undefined Behavior. Shutdown Server");
                            throw new Exception();
                        }

                        iterator.remove();
                    }
                } catch (IOException e) {
                    System.out.println(SERVER_RUN_FAILED_MSG);
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println(SERVER_RUN_FAILED_MSG);
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    stopServer();
                    break;
                }
            }
        });
        thread.start();
    }

    /**
     * Server를 정지시킴. 심각한 오류가 발생했을 때 실행
     */
    protected void stopServer() {
        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                serverSocketChannel.close();
            }

            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            System.out.println(SERVER_STOP_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_STOP_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Server를 시작하기 위해 여러 환경을 세팅하고, runServer() 메소드 호출
     */
    protected void startServer() {
        try {
            // 싱글톤 패턴으로 cache 인스턴스를 가져옴
            cache = (Cache<String, String>) Cache.getInstance();

            // Selector를 open
            selector = Selector.open();

            // serverSocketChannel과 관련된 세팅
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(44001));
            // serverSocketChannel을 non-blocking 모드로 설정하고, selector에 OP_ACCEPT로 등록함
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println(SERVER_START_MSG);

            // 서버 동작
            runServer();
        } catch (IOException e) {
            System.out.println(SERVER_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * selectionKey.isAcceptable() 일 때 실행되어 서버와 클라이언트를 연결시킴
     */
    private void accept() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("\n[Client Info: " + socketChannel.getRemoteAddress() + " # " + Thread.currentThread().getName() + "]");
            if (socketChannel != null) {
                // 정상적으로 소켓 채널이 설정되었다면, non-blocking 모드로 하여 셀렉터에 OP_READ로 등록시킴
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            } else {
                // 일반적으로 실행될 수 없는 분기로, IOException을 발생시킴
                throw new IOException();
            }

        } catch (IOException e) {
            System.out.println(SERVER_ACCEPT_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();

            if (serverSocketChannel.isOpen()) {
                stopServer();
            }
        }
    }

    /**
     * selectionKey.isReadable() 일 때 실행되어 클라이언트의 요청을 받고, 연산을 처리함
     * param : SelectionKey
     */
    private void receive(SelectionKey selectionKey) {
        try (SocketChannel socketChannel = (SocketChannel) selectionKey.channel()) {

            socketChannel.configureBlocking(false);
            ByteBuffer buf = ByteBuffer.allocate(512);

            int byteCount = socketChannel.read(buf);
            if (byteCount == -1) {
                System.out.println("Client is disconnected");
                throw new IOException();
            }

            buf.flip();
            // 클라이어트 측에서 연산의 종류와 그 연산에 대한 패러미터를 "\n"으로 구분하여 보내게 됨
            // 이를 split 메소드로 String[]에 저장
            String[] input = StandardCharsets.UTF_8.decode(buf).toString().split("\n");


            if ("1".equals(input[0])) {
                putOperation(socketChannel, input);
            }
            else if ("2".equals(input[0])) {
                getOperation(socketChannel, input);
            }
            else if ("3".equals((input[0]))) {
                removeOperation(socketChannel, input);
            } else {
                System.out.println("Client request not supported operation");
            }

            selectionKey.cancel();

        } catch (IOException e) {
            selectionKey.cancel();
            System.out.println(SERVER_RECEIVE_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            selectionKey.cancel();
            System.out.println(SERVER_RECEIVE_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
            stopServer();
        }
    }


    /**
     * Remove operation
     * cache에 주어진 key로 remove 연산 실행
     * param 1. : 클라이언트와의 소켓채널
     * param 2. : 클라이언트로부터 받은 String[] input
     * return case 1. : get 연산 시에 캐시에 해당 key가 없다면 "null"이 리턴
     * return case 2. : 기존의 key 값이 있다면 이를 삭제하고, 이에 대한 value를 리턴
     */
    private void removeOperation(SocketChannel socketChannel, String[] input) {
        try {
            String str = cache.remove(input[1]);

            // str이 null일 때 "null"을 반환하고 그 외에는 str을 반환
            socketChannel.write(charset.encode(Objects.requireNonNullElse(str, "null")));

            System.out.println("\n[Remove operation success]");
            System.out.println("Remove key = [" + input[1] + "]");
            System.out.println(SERVER_REMOVE_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_REMOVE_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get operation
     * cache에 주어진 key로 get 연산 실행
     * param 1. : 클라이언트와의 소켓채널
     * param 2. : 클라이언트로부터 받은 String[] input
     * return case 1. get 연산 시에 캐시에 해당 key가 없다면 "null"이 리턴
     * return case 2. 기존의 key 값이 있다면 이에 대한 value를 리턴
     */
    private void getOperation(SocketChannel socketChannel, String[] input) {
        try {
            // Request get operation
            // cache에 주어진 key로 get 연산 실행
            String str = cache.get(input[1]);

            // str이 null일 때 "null"을 반환하고 그 외에는 str을 반환
            socketChannel.write(charset.encode(Objects.requireNonNullElse(str, "null")));

            System.out.println("\n[Get operation success]");
            System.out.println("Get key = [" + input[1] + "]");
            System.out.println(SERVER_GET_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_GET_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Put operation
     * cache에 주어진 key와 value로 put 연산 실행
     * param 1. : 클라이언트와의 소켓채널
     * param 2. : 클라이언트로부터 받은 String[] input
     * return case 1. put 연산 시에 캐시에 해당 key-value 쌍이 없다면 "null"이 리턴
     * return case 2. 기존의 key 값이 있다면 이를 새로운 value로 갱신하고 기존의 key를 리턴
     */
    private void putOperation(SocketChannel socketChannel, String[] input) {
        try {

            String str = cache.put(input[1], input[2]);

            // str이 null일 때 "null"을 반환하고 그 외에는 str을 반환
            socketChannel.write(charset.encode(Objects.requireNonNullElse(str, "null")));

            System.out.println("\n[Put operation success]");
            System.out.println("Put key = [" + input[1] + "]");
            System.out.println("Put value = [" + input[2] + "]");
            System.out.println(SERVER_PUT_MSG);
        } catch (Exception e) {
            System.out.println(SERVER_PUT_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
