package com.blue.cacheserver.cache;

import java.util.Arrays;

public final class BytesKey {
    private final byte[] array;

    public BytesKey(byte[] array) {
        this.array = array;
    }

    public byte[] getArray() {
        return array.clone();
    }

    public boolean equals(Object obj) {
        if (obj instanceof BytesKey) {
            BytesKey other = (BytesKey) obj;
            return Arrays.equals(array, other.array);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(array);
    }

}