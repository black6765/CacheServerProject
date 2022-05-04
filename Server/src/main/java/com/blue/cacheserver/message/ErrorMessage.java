package com.blue.cacheserver.message;

import static com.blue.cacheserver.message.MessageColorCode.*;

public class ErrorMessage {
    public static final String SERVER_START_FAILED_MSG = YELLOW_COLOR + "[INFO] Server start failed" + COLOR_RESET;
    public static final String SERVER_STOP_FAILED_MSG = YELLOW_COLOR + "[INFO] Server stop failed" + COLOR_RESET;
    public static final String SERVER_RUN_FAILED_MSG = YELLOW_COLOR + "[INFO] Server run failed" + COLOR_RESET;
    public static final String SERVER_PUT_FAILED_MSG = YELLOW_COLOR + "[INFO] Server put operation failed" + COLOR_RESET;
    public static final String SERVER_GET_FAILED_MSG = YELLOW_COLOR + "[INFO] Server get operation failed" + COLOR_RESET;
    public static final String SERVER_REMOVE_FAILED_MSG = YELLOW_COLOR + "[INFO] Server remove operation failed" + COLOR_RESET;
    public static final String SERVER_ACCEPT_FAILED_MSG = YELLOW_COLOR + "[INFO] Server accept method failed" + COLOR_RESET;
    public static final String SERVER_RECEIVE_FAILED_MSG = YELLOW_COLOR + "[INFO] Server receive method failed" + COLOR_RESET;
    public static final String SERVER_CACHE_FULL_MSG = YELLOW_COLOR + "[INFO] Client request put operation. but cache memory is full" + COLOR_RESET;
    public static final String SERVER_CLIENT_DISCONNECT_MSG = YELLOW_COLOR + "[INFO] Client disconnected" + COLOR_RESET;
    public static final String SERVER_EOFEXCEPTION_MSG = YELLOW_COLOR + "[INFO] Request was ignored because client sent too large size data at once." + COLOR_RESET;
}
