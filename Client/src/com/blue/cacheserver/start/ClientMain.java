package com.blue.cacheserver.start;

public class ClientMain {
    public static void main(String[] args) {
        ClientConnectionImpl clientConnection = new ClientConnectionImpl();
        clientConnection.StartClient();

    }
}
