package Server.src.com.blue.cacheserver.service;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface ServerService {
    void startServer();
    void runServer();
    void stopServer();


    void accept();
    void receive(SelectionKey selectionKey);

    void removeOperation(SocketChannel socketChannel, String[] input);
    void getOperation(SocketChannel socketChannel, String[] input);
    void putOperation(SocketChannel socketChannel, String[] input);
}
