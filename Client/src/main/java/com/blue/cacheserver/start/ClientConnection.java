package com.blue.cacheserver.start;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.blue.cacheserver.message.ClientErrorMessage.CLIENT_REQUEST_UNDEFINED_OPERATION_MSG;
import static com.blue.cacheserver.message.ClientErrorMessage.CLIENT_START_FAILED_MSG;

public class ClientConnection {
    ByteBuffer buf = ByteBuffer.allocate(512);
    SocketChannel socketChannel;
    final String DELIMITER = "\n\n";

    public void StartClient() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            socketChannel.connect(new InetSocketAddress("localhost", 44001));

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
                }
            }
        } catch (IOException e) {
            System.out.println(CLIENT_START_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void requestPut(String[] cmd) {
        if (cmd.length != 3) {
            System.out.println("Invalid input");
            return;
        }

        socketConn(cmd);
    }

    public void requestGet(String[] cmd) {
        if (cmd.length != 2) {
            System.out.println("Invalid input");
            return;
        }
        socketConn(cmd);
    }

    public void requestRemove(String[] cmd) {
        if (cmd.length != 2) {
            System.out.println("Invalid input");
            return;
        }
        socketConn(cmd);
    }

    private void socketConn(String[] cmd) {
        try {
            byte[] concatBytes;
            if (cmd.length == 3) {
                concatBytes = getConcatBytes(cmd[0], cmd[1], cmd[2]);
            } else {
                concatBytes = getConcatBytes(cmd[0], cmd[1]);
            }

            socketChannel.write(ByteBuffer.wrap(concatBytes));
            buf.clear();

            int byteCount = socketChannel.read(buf);
            buf.flip();
            byte[] bytes = new byte[byteCount];
            buf.get(bytes);

            String bytesString = new String(bytes);
            String serverReturnValue;

            if ("null".equals(bytesString)) {
                serverReturnValue = "null";
            } else {
                serverReturnValue = getServerReturnValue(bytes);
            }
            System.out.println("Server return: " + serverReturnValue);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String getServerReturnValue(byte[] bytes) throws Exception {
        String serverReturnValue;
        try {
            serverReturnValue = (String) deserialize(bytes);
        } catch (StreamCorruptedException e) {
            serverReturnValue = new String(bytes);
        }
        return serverReturnValue;
    }

    private byte[] getConcatBytes(String operaion, String key, String value) throws IOException {
        final byte[] serializedOperation = serialize(operaion);
        final byte[] serializedKey = serialize(key);
        final byte[] serializedValue = serialize(value);
        final byte[] serializedTimeStamp = serialize(Instant.now());

        byte[] concatBytes = new byte[serializedOperation.length
                + serializedKey.length
                + serializedValue.length
                + serializedTimeStamp.length
                + (3 * DELIMITER.length())];

        int idx = 0;
        for (byte b : serializedOperation)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '\n';
        concatBytes[idx++] = '\n';

        for (byte b : serializedKey)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '\n';
        concatBytes[idx++] = '\n';

        for (byte b : serializedValue)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '\n';
        concatBytes[idx++] = '\n';

        for (byte b : serializedTimeStamp)
            concatBytes[idx++] = b;

        return concatBytes;
    }

    private byte[] getConcatBytes(String operaion, String key) throws IOException {
        final byte[] serializedOperation = serialize(operaion);
        final byte[] serializedKey = serialize(key);
        final byte[] serializedTimeStamp = serialize(Instant.now());

        byte[] concatBytes = new byte[serializedOperation.length
                + serializedKey.length
                + serializedTimeStamp.length
                + (2 * DELIMITER.length())];

        int idx = 0;
        for (byte b : serializedOperation)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '\n';
        concatBytes[idx++] = '\n';

        for (byte b : serializedKey)
            concatBytes[idx++] = b;

        concatBytes[idx++] = '\n';
        concatBytes[idx++] = '\n';

        for (byte b : serializedTimeStamp)
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

    private Object deserialize(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return ois.readObject();
    }
}
