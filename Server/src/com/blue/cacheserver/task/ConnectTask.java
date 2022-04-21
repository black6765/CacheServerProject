package com.blue.cacheserver.task;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class ConnectTask extends Thread {

    Selector selector;

    public void ConnectTask(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int keyCount = selector.select();

                if (keyCount == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeySet.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();

                    // Todo : 각 경우 메소드로 만들기
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel clientSocketChannel =  serverSocketChannel.accept();
                        clientSocketChannel.configureBlocking(false);

                        clientSocketChannel.write(ByteBuffer.wrap("Input your work: ".getBytes()));
                        clientSocketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (selectionKey.isReadable()) {
                        ByteBuffer buf = ByteBuffer.allocate(512);

                        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();

                        try {
                            clientSocketChannel.read(buf);
                            buf.flip();
                            System.out.println("클라이언트 입력값: " + Charset.forName("UTF-8").decode(buf));
                        } catch (Exception e) {
                            selectionKey.cancel();
                        }
                    } else {
                        System.out.println("원하는 것이 아님");
                    }
                }
            } catch (IOException e) {

            }
        }
    }
}
