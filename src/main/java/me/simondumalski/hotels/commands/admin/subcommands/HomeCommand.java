package me.simondumalski.hotels.commands.admin.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.utils.Hotel;
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
        return "Teleport to <player>'s hotel.";
    }

    @Override
    public String getUsage() {
        return "/hotelsadmin home <player>";
    }

    @Override
    public String getPermission() {
        return "hotels.admin";
    }

    @Override
    public boolean perform(Player p, String[] args) {

        //Check if the player has specified a target player
        if (args.length < 2) {
            plugin.sendPlayerMessage(p, "messages.errors.incorrect-usage", new String[]{getUsage()});
            return true;
        }

        //Get the target player from the command arguments
        Player target = plugin.getServer().getPlayer(args[1]);

        //Check if the target player exists
        if (target == null) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-player", null);
            return true;
        }

        //Check if the target player is renting a hotel
        if (!plugin.getHotelsMap().containsKey(target.getUniqueId())) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-player", null);
            return true;
        }

        //Get the target player's hotel
        Hotel hotel = plugin.getHotelsMap().get(target.getUniqueId());

        //Teleport the player to the center of the hotel's chunk and send them a confirmation message
        hotel.teleportToHotel(p);
        plugin.sendPlayerMessage(p, "messages.admin.home", new String[]{target.getName()});

        return true;
    }

}
