package client.cacheserver.test;

import client.cacheserver.start.ClientConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static client.cacheserver.message.ClientErrorMessage.*;
import static client.cacheserver.message.ClientMessage.*;
import static server.cacheserver.message.MessageColorCode.*;

public class ClientConnectionImplTest implements ClientConnection {
    ByteBuffer buf = ByteBuffer.allocate(512);
    Charset charset = StandardCharsets.UTF_8;
    SocketChannel socketChannel;

    public void StartClient() {

        try {
            socketChannel = SocketChannel.open();

            socketChannel.configureBlocking(true);
            System.out.println("\n[연결 요청 작업]\n");

            socketChannel.connect(new InetSocketAddress("localhost", 44001));
            System.out.println(CLIENT_CONNECTION_MSG);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("[Select Operation]");
            System.out.println("1. put\n2. get\n3. remove");
            System.out.print("Enter the operation number: ");

            String cmd;

            double r = Math.random() * 100;
            if ( r >= 100000) {
                cmd = String.valueOf(4);
            } else if ( r >= 70){
                cmd = String.valueOf(3);
            } else if ( r >= 50){
                cmd = String.valueOf(2);
            } else {
                cmd = String.valueOf(1);
            }

            if ("1".equals(cmd)) {
                requestPut(br, cmd);
            } else if ("2".equals(cmd)) {
                requestGet(br, cmd);
            } else if ("3".equals(cmd)) {
                requestRemove(br, cmd);
            } else {
                System.out.println(CLIENT_REQUEST_UNDEFINED_OPERATION_MSG);
                socketChannel.write(charset.encode(CLIENT_REQUEST_UNDEFINED_OPERATION_MSG));
            }

            socketChannel.close();
            br.close();
        } catch (IOException e) {
            System.out.println(CLIENT_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void requestRemove(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Remove operation]");
        System.out.print("Enter the key: ");
        String key = String.valueOf((int) (Math.random() * 1000));

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server " + GREEN_COLOR + "remove(" + key + ")" + COLOR_RESET + " operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return " + "[" + GREEN_COLOR + input + COLOR_RESET + "]");
    }

    public void requestGet(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Get operation]");
        System.out.print("Enter the key: ");
        String key = String.valueOf((int) (Math.random() * 1000));

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server " + GREEN_COLOR + "get(" + key + ")" + COLOR_RESET + " operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return " + "[" + GREEN_COLOR + input + COLOR_RESET + "]");
    }

    public void requestPut(BufferedReader br, String cmd) throws IOException {
        System.out.println("\n[Put operation]");
        System.out.print("Enter the key: ");
        String key = String.valueOf((int) (Math.random() * 1000));
        System.out.print("Enter the value: ");
        String value = String.valueOf((int) (Math.random() * 1000));

        // 서버로 입력 정보 송신
        String output = cmd + "\n" + key + "\n" + value;
        socketChannel.write(charset.encode(output));
        buf.clear();
        System.out.println("\nSend to the server " + GREEN_COLOR + "put(" + key + ", " + value + ")" + COLOR_RESET + " operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println("\nServer return " + "[" + GREEN_COLOR + input + COLOR_RESET + "]");
    }
}
