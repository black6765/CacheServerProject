package client.cacheserver.start;

import java.io.BufferedReader;
import java.io.IOException;

public interface ClientConnection {
    void StartClient();

    void requestRemove(BufferedReader br, String cmd) throws IOException;
    void requestGet(BufferedReader br, String cmd) throws IOException;
    void requestPut(BufferedReader br, String cmd) throws IOException;
}
