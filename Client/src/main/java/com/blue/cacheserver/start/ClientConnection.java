package com.blue.cacheserver.start;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.blue.cacheserver.message.ClientErrorMessage.CLIENT_REQUEST_UNDEFINED_OPERATION_MSG;
import static com.blue.cacheserver.message.ClientErrorMessage.CLIENT_START_FAILED_MSG;
import static com.blue.cacheserver.message.ClientMessage.CLIENT_CONNECTION_MSG;


public class ClientConnection {
    ByteBuffer buf = ByteBuffer.allocate(512);
    Charset charset = StandardCharsets.UTF_8;
    SocketChannel socketChannel;
    final String DELIMITER = "<=";

    public void StartClient() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            System.out.println("\n[연결 요청 작업]");

            socketChannel.connect(new InetSocketAddress("localhost", 44001));
            System.out.println(CLIENT_CONNECTION_MSG);

            while (true) {
                System.out.println("Select operation: put, get, remove, exit");
                String[] cmd = br.readLine().split(" ");

                if ("put".equals(cmd[0])) {
                    requestPut(cmd);
                } else if ("get".equals(cmd[0])) {
                    requestGet(cmd);
                } else if ("remove".equals(cmd[0])) {
                    requestRemove(cmd);
                } else if ("exit".equals(cmd[0])) {
                    System.out.println("Exited");
                    socketChannel.close();
                    br.close();
                    break;
                } else {
                    System.out.println(CLIENT_REQUEST_UNDEFINED_OPERATION_MSG);
                    socketChannel.write(charset.encode(CLIENT_REQUEST_UNDEFINED_OPERATION_MSG));
                }
            }
        } catch (IOException e) {
            System.out.println(CLIENT_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void requestPut(String[] cmd) throws IOException {
        byte[] concatBytes = getConcatBytes(cmd);

        socketChannel.write(ByteBuffer.wrap(concatBytes));
        buf.clear();

        int byteCount = socketChannel.read(buf);
        buf.flip();
        byte[] bytes = new byte[byteCount];
        buf.get(bytes);

        String returnVal;
        if ("null".equals((new String(bytes))))
            returnVal = "null";
        else
            returnVal = (String) deserialize(bytes);

        System.out.println("Server return: " + returnVal);
    }

    public void requestGet(String[] cmd) throws IOException {
        System.out.println("\n[Get operation]");

        // 서버로 입력 정보 송신
//        String output = cmd + "\n" + key;
//        socketChannel.write(charset.encode(output));
        buf.clear();
//        System.out.println("\nSend to the server " + GREEN_COLOR + "get(" + key + ")" + COLOR_RESET + " operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println(input);
    }

    public void requestRemove(String[] cmd) throws IOException {
        System.out.println("\n[Remove operation]");

        // 서버로 입력 정보 송신
//        String output = cmd + "\n" + key;
//        socketChannel.write(charset.encode(output));
        buf.clear();
//        System.out.println("\nSend to the server " + GREEN_COLOR + "remove(" + key + ")" + COLOR_RESET + " operation");

        socketChannel.read(buf);
        buf.flip();

        String input = StandardCharsets.UTF_8.decode(buf).toString();
        System.out.println(input);
    }

    private byte[] getConcatBytes(String[] cmd) throws IOException {
        byte[] serializedOperation = serialize(cmd[0]);
        byte[] serializedKey = serialize(cmd[1]);
        byte[] serializedValue = serialize(cmd[2]);

        byte[] concatBytes = new byte[serializedOperation.length +
                serializedKey.length + serializedValue.length + 2 * (DELIMITER.length())];

        int idx = 0;
        for (byte b : serializedOperation)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '<';
        concatBytes[idx++] = '=';

        for (byte b : serializedKey)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '<';
        concatBytes[idx++] = '=';

        for (byte b : serializedValue)
            concatBytes[idx++] = b;

        return concatBytes;
    }


    private byte[] serialize(Object obj) throws IOException {
        byte[] serializedObj;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        serializedObj = baos.toByteArray();

        return serializedObj;
    }

    private Object deserialize(byte[] bytes) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object bytesObj = ois.readObject();

                return bytesObj;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "null";
    }
}