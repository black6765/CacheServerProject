package com.blue.cacheserver.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Client {

    SocketChannel socketChannel;
    private String ip;

    Client (SocketChannel socketChannel) throws IOException {
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
    }

}
