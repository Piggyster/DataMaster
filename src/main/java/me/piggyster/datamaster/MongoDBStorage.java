package me.piggyster.datamaster;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.concurrent.*;

public class MongoDBStorage extends AbstractStorage {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>(); //add custom cache object for deletion after 3-5 minutes
    private final Gson gson = new GsonBuilder().create();
    private MongoCollection<Document> collection;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService cacheCleaner = Executors.newSingleThreadScheduledExecutor();

    public MongoDBStorage(String connectionString, String dbName) {
        MongoClientSettings clientSettings = MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).applyConnectionString(new ConnectionString(connectionString)).build();

        MongoClient client = MongoClients.create(clientSettings);
        collection = client.getDatabase(dbName).getCollection("playerdata");
        //index later

        cacheCleaner.scheduleAtFixedRate(() -> cache.entrySet().removeIf(entry -> entry.getValue().isExpired()),
                1, 1, TimeUnit.MINUTES);
    }


    /*

    Later I can possibly remove sync data getting methods, because you simply cannot practical access the database
    sync without stopping other operations. Even loading a player's initial data when they spawn is done async
    sync getting methods below are only different from the async methods in the fact that they do not cache
    any values they retrieve because another class handles that.

     */

    @Override
    public <T> T getSyncData(String key, TypeToken<T> type) {
        String[] keys = key.split("\\.", 2);
        Document playerDocument = collection.find(Filters.eq("_id", UUID.fromString(keys[0]))).first();
        return extractValue(playerDocument, keys[1], type);
    }

    private <T> T extractValue(Document document, String key, TypeToken<T> type) {
        String[] keys = key.split("\\.");
        if(document == null) return null;
        for(int i = 0; i < keys.length - 1; i++) {
            document = document.get(keys[i], Document.class);
            if(document == null) return null;
        }
        Object object = document.get(keys[keys.length - 1]);
        return gson.fromJson(gson.toJsonTree(object), type);
    }

    private Document extractDocument(Document document, String key) {
        String[] keys = key.split("\\.");
        for(int i = 0; i < keys.length; i++) {
            document = document.get(keys[i], Document.class);
            if(document == null) return null;
        }
        return document;
    }

    @Override
    public <T> CompletableFuture<T> getASyncData(UUID uuid, String key, TypeToken<T> type) {
        return CompletableFuture.supplyAsync(() -> {
            if(cache.containsKey(key)) {
                return gson.fromJson(gson.toJsonTree(cache.get(key).getValue()), type);
            }
            Document playerDocument = collection.find(Filters.eq("_id", uuid)).first();
            T value = extractValue(playerDocument, key, type);
            if(value != null) {
                cache.put(uuid + "." + key, new CacheEntry(value, 3));
            } else {
                cache.remove(uuid + "." + key);
            }
            return value;
        }, executor);
    }

    @Override
    public CompletableFuture<Void> setData(UUID uuid, String key, Object value) {
        return CompletableFuture.supplyAsync(() -> {
            Bson update = Updates.set(key, value);

            UpdateResult result = collection.updateOne(
                    Filters.eq("_id", uuid),
                    update,
                    new UpdateOptions().upsert(true)
            );

            if(result.getModifiedCount() > 0) {
                cache.put(key, new CacheEntry(value, 3));
            }
            //update cache for async values
            return null;
        }, executor);
    }

    @Override
    public CompletableFuture<Set<String>> getKeys(String prefix) {
        return CompletableFuture.supplyAsync(() -> {
            String[] keys = prefix.split("\\.", 2);
            Document playerDocument = collection.find(Filters.eq("_id", UUID.fromString(keys[0]))).first();
            Document document = extractDocument(playerDocument, keys[1]);
            if(document == null) {
                return Collections.emptySet();
            }
            Set<String> result = extractDocument(playerDocument, keys[1]).keySet();
            return new HashSet<>(result);
        }, executor);
    }

    @Override
    public CompletableFuture<Boolean> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if(collection.find(Filters.eq("_id", uuid)).first() != null) {
                return true;
            }
            Document player = new Document("_id", uuid);
            collection.insertOne(player);
            return false;
        }, executor);
    }

    @Override
    public void close() {
        executor.shutdown();
        cacheCleaner.shutdown();
    }
}
