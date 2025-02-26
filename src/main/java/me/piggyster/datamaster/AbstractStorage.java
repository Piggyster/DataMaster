package me.piggyster.datamaster;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import java.util.*;
import java.util.concurrent.*;

public abstract class AbstractStorage {
    protected final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    public abstract <T> CompletableFuture<T> getAsyncData(UUID uuid, String key, TypeToken<T> token);
    public abstract CompletableFuture<Void> setData(UUID uuid, String key, Object value);
    public abstract CompletableFuture<Set<String>> getKeys(String prefix);
    public abstract CompletableFuture<Boolean> loadPlayer(UUID uuid);
    public abstract void close();

}
