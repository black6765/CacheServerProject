package com.blue.cacheserver.cache;

import java.time.Instant;
import java.util.Arrays;

public class CacheValue {
    private final byte[] value;
    private final int byteSize;
    // timeStamp는 LRU(Eviction)에 사용되며, get 연산 시 갱신됨
    private Instant timeStamp;
    // expireTimeStamp는 처음 put 되었을 때 정해져 갱신되지 않으며, expire 시에 사용됨
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
