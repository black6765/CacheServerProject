package com.blue.cacheserver.start;

public class ClientMain {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImplTest();

        for (int i = 0; i < 1000; i++)
            clientConnection.StartClient();

    }
}
