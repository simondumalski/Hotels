package me.simondumalski.hotels.listeners;

import me.simondumalski.hotels.Core;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    private Core plugin;

    public PlayerInteract(Core plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent e) {

        //Get the player who interacted with a block
        Player p = e.getPlayer();

        //Check if the player has the specified permission and cancel the event if they don't
        if (!p.hasPermission("hotels.admin")) {
            e.setCancelled(true);
            plugin.sendPlayerMessage(p, "messages.events.player-interact", null);
        }

    }
}
