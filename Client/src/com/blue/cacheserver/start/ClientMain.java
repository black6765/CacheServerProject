package com.blue.cacheserver.start;

public class ClientMain {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImpl();

            clientConnection.StartClient();

    }
}
