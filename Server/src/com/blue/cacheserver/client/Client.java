//package com.blue.cacheserver.client;
//
//import java.net.InetSocketAddress;
//import java.nio.channels.SocketChannel;
//
//import static com.blue.cacheserver.message.ErrorMessage.SERVER_CLIENT_OBJECT_CONSTRUCT_FAILED_MSG;
//
//public class Client {
//
//    private String ip;
//    private int port;
//
//    public Client(SocketChannel socketChannel) {
//        try {
//            InetSocketAddress inetSocketAddress = ((InetSocketAddress) socketChannel.getRemoteAddress());
//            this.ip = inetSocketAddress.getHostName();
//            this.port = inetSocketAddress.getPort();
//        } catch (Exception e) {
//            System.out.println(SERVER_CLIENT_OBJECT_CONSTRUCT_FAILED_MSG);
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public String getIp() {
//        return ip;
//    }
//
//    public int getPort() {
//        return port;
//    }
//}
