package client.cacheserver.test;

import client.cacheserver.start.ClientConnection;

public class ClientMainTest {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImplTest();

        for (int i = 0; i < 10000; i++) {
            clientConnection.StartClient();
        }
    }
}
