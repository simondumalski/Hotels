package me.simondumalski.hotels.listeners;

import me.simondumalski.hotels.Core;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlace implements Listener {

    private Core plugin;

    public BlockPlace(Core plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {

        //Get the player who placed a block
        Player p = e.getPlayer();

        //Check if the player has the specified permission and cancel the event if they don't
        if (!p.hasPermission("hotels.admin")) {
            e.setCancelled(true);
            plugin.sendPlayerMessage(p, "messages.events.block-place", null);
        }

    }

}
