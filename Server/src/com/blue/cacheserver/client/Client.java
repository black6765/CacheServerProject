package com.blue.cacheserver.client;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static com.blue.cacheserver.message.ErrorMessage.SERVER_CLIENT_OBJECT_CONSTRUCT_FAILED_MSG;

public class Client {
    public Client(SocketChannel socketChannel) {
        try {
            InetSocketAddress inetSocketAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
            this.ip = inetSocketAddress.getHostName();
            this.port = inetSocketAddress.getPort();

            // IP:Port String의 해시 값으로 id를 생성
            String raw = id+port;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(raw.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest();
            this.id = String.format("%064x", new BigInteger(1, md.digest()));

        } catch (Exception e) {
            System.out.println(SERVER_CLIENT_OBJECT_CONSTRUCT_FAILED_MSG);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    private String id;
    private String ip;
    private int port;




}
