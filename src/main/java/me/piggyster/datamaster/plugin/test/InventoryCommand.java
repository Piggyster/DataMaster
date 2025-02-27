package me.piggyster.datamaster.plugin.test;

import com.google.common.reflect.TypeToken;
import me.piggyster.datamaster.api.DataMaster;
import me.piggyster.datamaster.api.PlayerData;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class InventoryCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return false;
        }
        PlayerData data = DataMaster.get().getPlayerData(player.getUniqueId());
        if(args.length == 0) {
            long start = System.currentTimeMillis();
            player.sendMessage("Loading inventory...");
            data.getMapAsync("inventory",
                    new TypeToken<Integer>() {},
                    new TypeToken<Map<String, Object>>() {}
            ).thenAccept(inventory -> {
                player.getInventory().clear();
                inventory.forEach((slot, serializedItem) -> {
                    ItemStack item = ItemStack.deserialize(serializedItem);
                    player.getInventory().setItem(slot, item);
                });
                long end = System.currentTimeMillis();
                player.sendMessage("Your inventory has been repopulated after " + (end - start) + "ms!");
            });
            return true;
        } else {
            data.set("inventory", new HashMap<>());
            player.sendMessage("Saving inventory...");
            ItemStack[] contents = player.getInventory().getContents();
            for(int i = 0; i < contents.length; i++) {
                if(contents[i] == null || contents[i].getType() == Material.AIR) continue;
                data.set("inventory." + i, contents[i].serialize());
            }
            player.sendMessage("Your inventory has been saved!");
            return true;
        }
    }
}
