package me.piggyster.datamaster;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PlayerData {

    private final UUID uuid;
    private final Map<String, Object> syncData = new ConcurrentHashMap<>();

    private final DataMaster dataMaster;

    public PlayerData(UUID uuid, DataMaster dataMaster) {
        this.uuid = uuid;
        this.dataMaster = dataMaster;
    }

    public CompletableFuture<Void> loadSyncData(Set<String> keys) {
        return CompletableFuture.supplyAsync(() -> {
            for (String key : keys) {
                if(key.matches(".*\\.\\*")) {
                    //wildcard
                    String path = key.replaceAll("\\.\\*", "");
                    Set<String> result = dataMaster.getKeys(uuid, path).join();
                    System.out.println(result);
                    result = result.stream().map(k -> path + "." + k).collect(Collectors.toSet());
                    loadSyncData(result);
                } else {
                    //normal
                    Object value = dataMaster.getSyncData(uuid, key, Object.class);
                    if(value == null) continue;
                    syncData.put(key, value);
                }
            }
            return null;
        });
    }


    public <T> T getSync(String key, TypeToken<T> type) {
        Object value = syncData.get(key);
        if(value == null) {
            return dataMaster.getDefaultValue(key, type);
        }
        return dataMaster.gson.fromJson(dataMaster.gson.toJsonTree(value), type);
    }

    public <T> T getSync(String key, Class<T> clazz) {
        return getSync(key, TypeToken.get(clazz));
    }

    public <T> CompletableFuture<T> getASync(String key, TypeToken<T> type) {
        return dataMaster.getAsyncData(uuid, key, type);
    }

    public <T> CompletableFuture<T> getASync(String key, Class<T> clazz) {
        return getASync(key, TypeToken.get(clazz));
    }

    public <K, V> CompletableFuture<Map<K, V>> getMapASync(String path, TypeToken<K> keyType, TypeToken<V> valueType) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> keys = dataMaster.getKeys(uuid, path).join();
            Map<K, V> map = new HashMap<>();
            keys.forEach(key -> {
                K formattedKey = dataMaster.gson.fromJson(dataMaster.gson.toJsonTree(key), keyType);
                V value = getASync(path + "." + key, valueType).join();
                map.put(formattedKey, value);
            });
            return map;
        });
    }

    public <K, V> Map<K, V> getMapSync(String path, TypeToken<K> keyType, TypeToken<V> valueType) {
        Map<K, V> map = new HashMap<>();
        syncData.keySet().forEach(key -> {
            if(key.startsWith(path)) {
                int index = key.lastIndexOf(".");
                String substring = key.substring(index + 1);
                K formattedKey = dataMaster.gson.fromJson(dataMaster.gson.toJsonTree(substring), keyType);
                V value = getSync(key, valueType);
                map.put(formattedKey, value);
            }
        });
        return map;
    }

    public void set(String key, Object value) {
        dataMaster.setData(uuid, key, value);
        if(dataMaster.isSyncField(key)) {
            syncData.put(key, value);
        }

        //add a saving mechanism to do batch updates instead of constant ones
    }

    public UUID getPlayer() {
        return uuid;
    }
}