package me.piggyster.datamaster.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.piggyster.datamaster.api.storage.AbstractStorage;
import me.piggyster.datamaster.api.storage.impl.MongoDBStorage;
import me.piggyster.datamaster.api.util.DataMasterSettings;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DataMaster {

    private static DataMaster master;

    public static DataMaster get() {
        if(master == null) {
            throw new NullPointerException("DataMaster has not yet been initialized.");
        }
        return master;
    }

    public static void initialize(DataMasterSettings settings) {
        if(master != null) {
            throw new IllegalStateException("API is already initialized.");
        }
        AbstractStorage storage;
        if(settings.getDatabaseType().equalsIgnoreCase("mongo")) {
            storage = new MongoDBStorage(settings.getAddress(), settings.getDatabaseName());
        } else {
            System.out.println("An error has occured");
            storage = null;
        }
        master = new DataMaster(storage);
    }

    protected final Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .create();
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final AbstractStorage storage;

    private final Map<String, Object> defaultValueRegistry = new ConcurrentHashMap<>();
    private final Set<String> syncValues = new HashSet<>();

    private DataMaster(AbstractStorage storage) {
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
        return gson.fromJson(gson.toJsonTree(value), type.getType());
    }

    public Map<String, Object> getDefaultValues() {
        return ImmutableMap.copyOf(defaultValueRegistry);
    }

    public <T> T getDefaultValue(String key, Class<T> clazz) {
        return getDefaultValue(key, TypeToken.of(clazz));
    }

    public <T> CompletableFuture<T> getAsyncData(UUID uuid, String key, TypeToken<T> type) {
        return storage.getAsyncData(uuid, key, type).thenApply(data -> {
            if(data == null) {
                return getDefaultValue(key, type);
            }
            return data;
        });
    }

    public <T> CompletableFuture<T> getAsyncData(UUID uuid, String key, Class<T> clazz) {
        return getAsyncData(uuid, key, TypeToken.of(clazz));
    }

    public <K, V> CompletableFuture<Map<K, V>> getMapAsync(UUID uuid, String path, TypeToken<K> keyToken, TypeToken<V> valueToken) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> keys = getKeys(uuid, path).join();
            Map<K, V> map = new HashMap<>();
            keys.forEach(key -> {
                K formattedKey = gson.fromJson(gson.toJsonTree(key), keyToken.getType());
                V value = getAsyncData(uuid, path + "." + key, valueToken).join();
                map.put(formattedKey, value);
            });
            return map;
        });
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
