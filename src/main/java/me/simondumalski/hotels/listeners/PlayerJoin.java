package me.simondumalski.hotels.listeners;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.utils.Hotel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private Core plugin;

    public PlayerJoin(Core plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {

        //Get the player who joined
        Player p = e.getPlayer();

        //Check if the player has a hotel
        if (plugin.getHotelsMap().containsKey(p.getUniqueId())) {

            //Get the player's hotel
            Hotel hotel = plugin.getHotelsMap().get(p.getUniqueId());

            //Send a rent expiry alert if the player has less than 1 day left in their rental period, otherwise send a player-join message
            if (hotel.getTimeLeft() < 86400) {
                plugin.sendPlayerMessage(p, "messages.events.rent-expiry-alert", new String[]{hotel.timeToString()});
            } else {
                plugin.sendPlayerMessage(p, "messages.events.player-join", new String[]{hotel.timeToString()});
            }

        }

    }
}
