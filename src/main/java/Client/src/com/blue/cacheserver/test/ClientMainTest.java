package Client.src.com.blue.cacheserver.test;

import Client.src.com.blue.cacheserver.start.ClientConnection;
import Client.src.com.blue.cacheserver.start.ClientConnectionImpl;

public class ClientMainTest {
    public static void main(String[] args) {
        ClientConnection clientConnection = new ClientConnectionImplTest();

        for (int i = 0; i < 10000; i++) {
            clientConnection.StartClient();
        }
    }
}
