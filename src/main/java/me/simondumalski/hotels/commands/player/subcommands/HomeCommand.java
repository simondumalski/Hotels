package me.simondumalski.hotels.commands.player.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.utils.Hotel;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HomeCommand extends SubCommand {

    private Core plugin;

    public HomeCommand(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "home";
    }

    @Override
    public String getDescription() {
        return "Teleports you to your hotel.";
    }

    @Override
    public String getUsage() {
        return "/hotels home";
    }

    @Override
    public String getPermission() {
        return "hotels.rent";
    }

    @Override
    public boolean perform(Player p, String[] args) {

        //Check if the player is renting a hotel
        if (!plugin.getHotelsMap().containsKey(p.getUniqueId())) {
            plugin.sendPlayerMessage(p, "messages.errors.not-renting", null);
            return true;
        }

        //Get the player's hotel and the hotel's chunk
        Hotel hotel = plugin.getHotelsMap().get(p.getUniqueId());

        //Teleport the player to the center of the hotel's chunk and send them a confirmation message
        hotel.teleportToHotel(p);
        plugin.sendPlayerMessage(p, "messages.commands.home", new String[]{hotel.timeToString()});

        return true;
    }

}
