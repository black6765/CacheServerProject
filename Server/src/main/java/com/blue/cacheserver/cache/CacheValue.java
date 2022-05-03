package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.Arrays;


public class CacheValue {
    private final byte[] value;
    private final int byteSize;
    private Instant timeStamp;
    private boolean expired;


    public CacheValue(byte[] value, Instant instant, int byteSize) {
        this.value = Arrays.copyOf(value, value.length);
        this.timeStamp = instant;
        this.byteSize = byteSize;
        this.expired = false;
    }

    public byte[] getValue() {
        return value;
    }

    public int getByteSize() {
        return byteSize;
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

}
