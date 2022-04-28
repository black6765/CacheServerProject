package com.blue.cacheserver.message;

public class DisconnectException extends Exception {
    public DisconnectException(String message) {
        super(message);
    }

    public DisconnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisconnectException(Throwable cause) {
        super(cause);
    }
}
