package me.piggyster.datamaster.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PostPlayerDataLoadEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public PostPlayerDataLoadEvent(Player player) {
        super(player);
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
