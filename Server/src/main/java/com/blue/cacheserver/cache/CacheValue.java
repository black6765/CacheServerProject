package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.Arrays;

public class CacheValue {
    private final byte[] value;
    private final int byteSize;
    private Instant timeStamp;
    private final Instant expireTimeStamp;

    public CacheValue(byte[] value, Instant instant, int byteSize) {
        this.value = Arrays.copyOf(value, value.length);
        this.byteSize = byteSize;
        this.timeStamp = instant;
        this.expireTimeStamp = instant;
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

    public Instant getExpireTimeStamp() {
        return expireTimeStamp;
    }
}
