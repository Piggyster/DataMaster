package me.piggyster.datamaster.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class AsyncPrePlayerDataLoadEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public AsyncPrePlayerDataLoadEvent(Player player) {
        super(player, true);
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
