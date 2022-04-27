package server.cacheserver.message;

public class ErrorMessage {
    public static final String SERVER_START_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server start failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_STOP_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server stop failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_RUN_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server run failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_PUT_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server put operation failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_GET_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server get operation failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_REMOVE_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server remove operation failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_ACCEPT_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server accept method failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_RECEIVE_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server receive method failed" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_CACHE_FULL_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Client request put operation. but cache memory is full" + MessageColorCode.COLOR_RESET;
    public static final String SERVER_CLIENT_OBJECT_CONSTRUCT_FAILED_MSG = MessageColorCode.YELLOW_COLOR + "[INFO] Server failed to construct Client object" + MessageColorCode.COLOR_RESET;


}
