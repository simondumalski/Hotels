package me.simondumalski.hotels.commands.player.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.utils.Hotel;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InfoCommand extends SubCommand {

    private Core plugin;

    public InfoCommand(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Displays information on your hotel booking.";
    }

    @Override
    public String getUsage() {
        return "/hotels info";
    }

    @Override
    public String getPermission() {
        return "hotels.rent";
    }

    @Override
    public boolean perform(Player p, String[] args) {

        //Check if the player is renting a hotel and send them an error message if they aren't
        if (!plugin.getHotelsMap().containsKey(p.getUniqueId())) {
            plugin.sendPlayerMessage(p, "messages.errors.not-renting", null);
            return true;
        }

        //Get the player's hotel booking
        Hotel hotel = plugin.getHotelsMap().get(p.getUniqueId());

        //Send the plugin footer as a header
        plugin.sendMessageFooter(p);

        //Get the info message from the config.yml
        String message = plugin.getConfig().getString("messages.commands.info");

        //Replace the time-left placeholder with the hotel time left
        if (message.contains("%time-left%"))
            message = message.replace("%time-left%", hotel.timeToString());

        //Send the player the info message
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        //Send the plugin footer
        plugin.sendMessageFooter(p);

        return true;
    }

}
