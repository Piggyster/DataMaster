package me.piggyster.datamaster.plugin.listener;

import me.piggyster.datamaster.api.DataMaster;
import me.piggyster.datamaster.api.event.AsyncPrePlayerDataLoadEvent;
import me.piggyster.datamaster.api.event.PostPlayerDataLoadEvent;
import me.piggyster.datamaster.plugin.DataMasterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private DataMasterPlugin plugin;

    public PlayerListener(DataMasterPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getOnlinePlayers().forEach(player -> {
            DataMaster.get().loadPlayer(player.getUniqueId());
        });
    }

    @EventHandler
    public void onEvent(PlayerJoinEvent event) {
        AsyncPrePlayerDataLoadEvent preLoadEvent = new AsyncPrePlayerDataLoadEvent(event.getPlayer());
        //Bukkit.getPluginManager().callEvent(preLoadEvent);
        DataMaster.get().loadPlayer(event.getPlayer().getUniqueId()).thenAccept(none -> {
            PostPlayerDataLoadEvent postLoadEvent = new PostPlayerDataLoadEvent(event.getPlayer());
            //Bukkit.getPluginManager().callEvent(postLoadEvent);
        });
    }

    @EventHandler
    public void onEvent(PlayerQuitEvent event) {
        DataMaster.get().unloadPlayer(event.getPlayer().getUniqueId());
    }
}
