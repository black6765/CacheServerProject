package Server.src.com.blue.cacheserver.message;

import static Server.src.com.blue.cacheserver.message.MessageColorCode.*;

public class Message {
    public static final String SERVER_START_MSG = GREEN_COLOR + "[INFO] Server successfully started" + COLOR_RESET;
    public static final String SERVER_STOP_MSG = GREEN_COLOR + "[INFO] Server successfully stop" + COLOR_RESET;
    public static final String SERVER_PUT_MSG = GREEN_COLOR + "[INFO] Server successfully process put operation" + COLOR_RESET;
    public static final String SERVER_GET_MSG = GREEN_COLOR + "[INFO] Server successfully process get operation" + COLOR_RESET;
    public static final String SERVER_REMOVE_MSG = GREEN_COLOR + "[INFO] Server successfully process remove operation" + COLOR_RESET;
    public static final String SERVER_CACHE_EVICTION_MSG = GREEN_COLOR + "[INFO] Server successfully process eviction" + COLOR_RESET;
}
