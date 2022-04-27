package com.blue.cacheserver.start;

import com.blue.cacheserver.test.ClientConnectionImplTest;

public class ClientMain {
    public static void main(String[] args) {

        ClientConnection clientConnection = new ClientConnectionImpl();
        clientConnection.StartClient();
    }
}
