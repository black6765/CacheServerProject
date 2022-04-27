package server.cacheserver.message;

public class Message {
    public static final String SERVER_START_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully started" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_STOP_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully stop" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_PUT_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully process put operation" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_GET_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully process get operation" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_REMOVE_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully process remove operation" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_CACHE_EVICTION_MSG = MessageColorCode.GREEN_COLOR + "[INFO] Server successfully process eviction" + MessageColorCode.COLOR_RESET;
}
