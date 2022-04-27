package com.blue.cacheserver.start;

import com.blue.cacheserver.test.ClientConnectionImplTest;

public class ClientMain {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImpl();

//        for (int i = 0; i < 10000; i++)
        clientConnection.StartClient();

    }
}
