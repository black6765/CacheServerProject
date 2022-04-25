package com.blue.cacheserver.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class StartClient {

    protected void StartClient() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            System.out.println("[연결 요청 작업]\n");

            socketChannel.connect(new InetSocketAddress("localhost", 44001));
            System.out.println("[서버 연결 완료]\n");

            ByteBuffer buf = ByteBuffer.allocate(512);
            Charset charset = StandardCharsets.UTF_8;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("[Select Operation]");
            System.out.println("1. put\n2. get\n3. remove");
            System.out.print("Enter the operation number: ");
            String cmd = br.readLine();

            // Todo 각 연산마다 분기점 설정. 각 분기 메소드로 리팩토링
            if ("1".equals(cmd)) {
                System.out.println("\n[Put operation]\n");
                System.out.print("Enter the key: ");
                String key = br.readLine();
                System.out.print("Enter the value: ");
                String value = br.readLine();

                // 서버로 입력 정보 송신
                String output = cmd + "\n" + key + "\n" + value;
                socketChannel.write(charset.encode(output));
                buf.clear();
                System.out.println("\nSend to the server [ put(" + key + ", " + value + ") ]");

                socketChannel.read(buf);
                buf.flip();

                String input = StandardCharsets.UTF_8.decode(buf).toString();
                System.out.println("\nServer return [" + input + "]");

//                int byteCount;
//                byteCount = socketChannel.read(buf);
//
//                if (byteCount == -1) {
//                    throw new IOException();
//                }
//                buf.flip();
//                System.out.println(charset.decode(buf));
//                ;
//
//                // 서버로 value를 송신
//                socketChannel.write(charset.encode(value));
//
//                buf.clear();
//                byteCount = socketChannel.read(buf);
//
//                if (byteCount == -1) {
//                    throw new IOException();
//                }
//                buf.flip();
//                System.out.println(charset.decode(buf));
//                ;
            } else if ("2".equals(cmd)) {

                System.out.println("\n[Get operation]\n");
                System.out.print("Enter the key: ");
                String key = br.readLine();

                // 서버로 입력 정보 송신
                String output = cmd + "\n" + key;
                socketChannel.write(charset.encode(output));
                buf.clear();
                System.out.println("\nSend to the server [ get(" + key + ") ]");

                socketChannel.read(buf);
                buf.flip();

                String input = StandardCharsets.UTF_8.decode(buf).toString();
                System.out.println("\nServer return [" + input + "]");


//                System.out.println("\n[Get operation]\n");
//                System.out.print("Enter the key: ");
//                String key = br.readLine();
//
//                // 서버로 key를 송신
//                socketChannel.write(charset.encode(key));
//
//                int byteCount;
//                byteCount = socketChannel.read(buf);
//
//                if (byteCount == -1) {
//                    throw new IOException();
//                }
//                buf.flip();
//                System.out.println(charset.decode(buf));
//
//                buf.clear();
//                byteCount = socketChannel.read(buf);
//
//                if (byteCount == -1) {
//                    throw new IOException();
//                }
//                buf.flip();
//                System.out.println(charset.decode(buf));
            }





        } catch (IOException e) {

        }
    }
}