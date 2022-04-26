package com.blue.cacheserver.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.blue.cacheserver.message.ClientErrorMessage.CLIENT_START_FAILED_MSG;
import static com.blue.cacheserver.message.ClientSuccessMessage.CLIENT_CONNECTION_MSG;

public class ClientConnection {
    ByteBuffer buf = ByteBuffer.allocate(512);
    Charset charset = StandardCharsets.UTF_8;
    SocketChannel socketChannel;

    protected void StartClient() {

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            System.out.println("[연결 요청 작업]\n");

            socketChannel.connect(new InetSocketAddress("localhost", 44001));
            System.out.println(CLIENT_CONNECTION_MSG);



            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("[Select Operation]");
            System.out.println("1. put\n2. get\n3. remove");
            System.out.print("Enter the operation number: ");
            String cmd = br.readLine();

            // Todo 각 연산마다 분기점 설정. 각 분기 메소드로 리팩토링
            if ("1".equals(cmd)) {
                requestPut(br, cmd);
            } else if ("2".equals(cmd)) {
                requestGet(br, cmd);
            } else if ("3".equals(cmd)) {
                requestRemove(br, cmd);
            } else {
                System.out.println("Client request not supported operation");
            }

            br.close();
        } catch (IOException e) {
            System.out.println(CLIENT_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void requestRemove(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Remove operation]");
        System.out.print("Enter the key: ");
        String key = br.readLine();

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server [ remove(" + key + ") ] operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return [" + input + "]");
    }

    private void requestGet(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Get operation]");
        System.out.print("Enter the key: ");
        String key = br.readLine();

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server [ get(" + key + ") ] operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return [" + input + "]");
    }

    private void requestPut(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Put operation]");
        System.out.print("Enter the key: ");
        String key = br.readLine();
        System.out.print("Enter the value: ");
        String value = br.readLine();

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key + "\n" + value;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server [ put(" + key + ", " + value + ") ] operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return [" + input + "]");
    }
}