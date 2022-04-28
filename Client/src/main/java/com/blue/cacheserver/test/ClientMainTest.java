package com.blue.cacheserver.test;

import com.blue.cacheserver.start.ClientConnection;
import com.blue.cacheserver.start.ClientConnectionImpl;

public class ClientMainTest {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImplTest();

        clientConnection.StartClient();
    }
}
