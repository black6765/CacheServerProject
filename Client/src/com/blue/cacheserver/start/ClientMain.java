package com.blue.cacheserver.start;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.spec.RSAOtherPrimeInfo;

public class ClientMain {
    public static void main(String[] args) {
        StartClient startClient = new StartClient();
        startClient.StartClient();

    }
}
