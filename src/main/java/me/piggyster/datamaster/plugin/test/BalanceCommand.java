package me.piggyster.datamaster.plugin.test;

import me.piggyster.datamaster.api.DataMaster;
import me.piggyster.datamaster.api.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("You must be a player to use this command.");
            return false;
        }
        PlayerData data = DataMaster.get().getPlayerData(player.getUniqueId());
        if(args.length == 0) {
            player.sendMessage("Usage: /balance <currency>");
            int coins = data.getSync("economy.coins", Integer.class);
            data.set("economy.coins", ++coins);
            player.sendMessage("However you received one coin!");
            return false;
        }

        String currency = args[0];
        int balance = data.getSync("economy." + currency, Integer.class);
        player.sendMessage("Your current " + currency.toLowerCase() + " balance is " + balance + "!");
        return false;
    }
}
