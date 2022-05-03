package com.blue.cacheserver.start;

public class ClientMain {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.StartClient();
    }
}
