package me.piggyster.datamaster;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractStorage {
    protected final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();

    public abstract <T> T getSyncData(String key, TypeToken<T> type);
    public abstract <T> CompletableFuture<T> getASyncData(UUID uuid, String key, TypeToken<T> type);
    public abstract CompletableFuture<Void> setData(UUID uuid, String key, Object value);
    public abstract CompletableFuture<Set<String>> getKeys(String prefix);

    public abstract CompletableFuture<Boolean> loadPlayer(UUID uuid);

    public abstract void close();

    //if I'm going to split values to extract UUID, I might as well just have a UUID parameter in each method instead of one long key
    //get rid of Class methods as it's redunent. The datamaster handles it the same way to avoid needing them here

}
