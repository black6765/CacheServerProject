package com.blue.cacheserver.cache;

import java.util.Arrays;

public final class BytesKey {
    private final byte[] array;

    public BytesKey(byte[] array) {
        this.array = array;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BytesKey) {
            BytesKey other = (BytesKey) obj;
            return Arrays.equals(array, other.array);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }
}