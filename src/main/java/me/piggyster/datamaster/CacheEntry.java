package me.piggyster.datamaster;

public class CacheEntry {
    private final Object value;
    private final long expiration;

    public CacheEntry(Object value, int minutes) {
        this.value = value;
        this.expiration = System.currentTimeMillis() + (minutes * 60_000);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiration;
    }

    public Object getValue() {
        return value;
    }
}
