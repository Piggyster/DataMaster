package me.piggyster.datamaster;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) {

        DataMaster master = DataMaster.get();

        master.registerDefaultValue("coins", 15);
        master.setSyncField("coins"); //sync value

        master.registerDefaultValue("inventory", new HashMap<>());
        master.setSyncField("inventory.*"); //sync map


        master.registerDefaultValue("settings.volume", 100); //not sync
        master.registerDefaultValue("settings.opacity", 100);

        UUID uuid = UUID.fromString("6b9c1f73-c42e-4f50-ac88-fa8934e9dd32"); //6b9c1f73-c42e-4f50-ac88-fa8934e9dd32
        //be46ab08-8670-4061-a6f5-c1b75d1a8fa2
        master.loadPlayer(uuid).join();

        PlayerData data = master.getPlayerData(uuid);

        int coins = data.getSync("coins", Integer.class);

        System.out.println(data.getPlayer() + " Player Info:");
        data.set("coins", ++coins); //
        System.out.println("Coins: " + coins);

        data.getAsync("settings.volume", Integer.class).thenAccept(volume -> {
            System.out.println("Volume: " + volume);
        });

        data.set("settings.opacity", 50);

        Set<String> keys = master.getKeys(uuid, "settings").join();

        keys.forEach(key -> {
            data.getAsync("settings." + key, Integer.class).thenAccept(value -> {
                System.out.println(value);
            });
        });

        Map<String, Integer> map = data.getMapAsync("settings", new TypeToken<String>() {}, new TypeToken<Integer>() {}).join();
        System.out.println(map);


        data.set("inventory.1", "dirt");
        data.set("inventory.3", "diamond");


        Map<Integer, String> inventory = data.getMapSync("inventory", new TypeToken<Integer>() {}, new TypeToken<String>() {});

        System.out.println(inventory);


        boolean bool = master.isSyncField("inventory.");
        System.out.println(bool);



        master.shutdown();
    }

}
