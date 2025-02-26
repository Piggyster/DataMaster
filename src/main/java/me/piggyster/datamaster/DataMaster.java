package me.piggyster.datamaster;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DataMaster {

    private static DataMaster master;

    static {
        master = new DataMaster(
                new MongoDBStorage("mongodb://localhost:27017", "datamaster")
        );
    }

    public static DataMaster get() {
        return master;
    }

    protected final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final AbstractStorage storage;

    private final Map<String, Object> defaultValueRegistry = new ConcurrentHashMap<>();
    private final Set<String> syncValues = new HashSet<>();

    public DataMaster(AbstractStorage storage) {
        this.storage = storage;
    }

    public void setSyncField(String path) {
        syncValues.add(path);
    }

    public Set<String> getSyncFields() {
        return ImmutableSet.copyOf(syncValues);
    }

    public boolean isSyncField(String path) {
        if(syncValues.contains(path)) {
            return true;
        }
        for(String string : syncValues) {
            if(string.endsWith(".*")) {
                string = string.replaceAll("\\.\\*", "..+");
                if(path.matches(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void registerDefaultValue(String key, Object value) {
        defaultValueRegistry.put(key, value);
    }

    public <T> T getDefaultValue(String key, TypeToken<T> type) {
        Object value = defaultValueRegistry.get(key);
        if(value == null) return null;
        return gson.fromJson(gson.toJsonTree(value), type);
    }

    public Map<String, Object> getDefaultValues() {
        return ImmutableMap.copyOf(defaultValueRegistry);
    }

    public <T> T getDefaultValue(String key, Class<T> clazz) {
        return getDefaultValue(key, TypeToken.get(clazz));
    }

    public <T> CompletableFuture<T> getAsyncData(UUID uuid, String key, TypeToken<T> type) {
        return storage.getASyncData(uuid, key, type).thenApply(data -> {
            if(data == null) {
                return getDefaultValue(key, type);
            }
            return data;
        });
    }

    public <T> CompletableFuture<T> getASyncData(UUID uuid, String key, Class<T> clazz) {
        return getAsyncData(uuid, key, TypeToken.get(clazz));
    }

    public <T> T getSyncData(UUID uuid, String key, TypeToken<T> type) {
        return storage.getSyncData(uuid + "." + key, type);
    }

    public <T> T getSyncData(UUID uuid, String key, Class<T> clazz) {
        return getSyncData(uuid, key, TypeToken.get(clazz));
    }

    public void setData(UUID uuid, String key, Object value) {
        storage.setData(uuid, key, value);
    }

    public CompletableFuture<Set<String>> getKeys(UUID uuid, String path) {
        return storage.getKeys(uuid + "." + path);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return players.get(uuid);
    }

    public CompletableFuture<Void> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            boolean exists = storage.loadPlayer(uuid).join();
            PlayerData data = new PlayerData(uuid, this);
            if(!exists) {
                System.out.println("Creating new player data...");
                getDefaultValues().forEach(data::set);
            } else {
                System.out.println("Loading existing data...");
                data.loadSyncData(syncValues).join();
            }
            players.put(uuid, data);
            return null;
        });
    }

    public void unloadPlayer(UUID uuid) {
        players.remove(uuid);
    }

    public void shutdown() {
        storage.close();
    }
}
