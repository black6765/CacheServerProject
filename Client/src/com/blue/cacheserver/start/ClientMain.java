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

        SocketChannel socketChannel = null;
        for (int i=0;i<10;i++) {
            try {
                socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(true);
                System.out.println("연결 요청 작업");

                socketChannel.connect(new InetSocketAddress("localhost", 44001));
                System.out.println("서버와 연결 완료");

                ByteBuffer buf = ByteBuffer.allocate(512);
                Charset charset = Charset.forName("UTF-8");

                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                String cmd = "br.readLine()";
                socketChannel.write(charset.encode(cmd));
                System.out.println("동작 보내기 성공. 보낸 동작 = " + cmd);
                buf.clear();

            } catch (IOException e) {

            }
        }

    }
}
