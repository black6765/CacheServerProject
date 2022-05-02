package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.Arrays;


public class CacheValue {
    private byte[] value;
    private Instant timeStamp;
    private boolean expired;

    public CacheValue(byte[] value, Instant instant) {
        this.value = Arrays.copyOf(value, value.length);
        this.timeStamp = instant;
        this.expired = false;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
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
