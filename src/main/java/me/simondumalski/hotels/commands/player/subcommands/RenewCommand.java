package me.simondumalski.hotels.commands.player.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.utils.Hotel;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class RenewCommand extends SubCommand {

    private Core plugin;

    public RenewCommand(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "renew";
    }

    @Override
    public String getDescription() {
        return "Renew your hotel booking for <days> additional days.";
    }

    @Override
    public String getUsage() {
        return "/hotels renew <days>";
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
        //Check if the player has specified a renewal period
        if (args.length < 2) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Initialize the renewal period variable
        int renewalPeriod = 0;

        try {
            renewalPeriod = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Check if the specified renewal period is less than 1
        if (renewalPeriod < 1) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Get the hotel the player owns
        Hotel hotel = plugin.getHotelsMap().get(p.getUniqueId());

        //Get the current remaining time the player has on their hotel booking
        int currentPeriod = hotel.getTimeLeft();

        //Get the player's allowed rental period
        int allowedRentalPeriod = plugin.getAllowedRentalPeriod(p);

        //Check if the player is trying to rent for longer than they are allowed to
        if (allowedRentalPeriod < currentPeriod + renewalPeriod) {
            plugin.sendPlayerMessage(p, "messages.errors.too-long", new String[]{Integer.toString(plugin.getAllowedRentalPeriod(p))});
            return true;
        }

        //Calculate the cost based on the hotel price-per-day
        int cost = renewalPeriod * plugin.getConfig().getInt("hotels.price-per-day");

        //Check if the player has enough money to rent the hotel
        if (plugin.getEconomy().getBalance((OfflinePlayer) p) < cost) {
            plugin.sendPlayerMessage(p, "messages.errors.insufficient-funds", null);
            return true;
        }

        //Take the money from the player's balance
        plugin.getEconomy().withdrawPlayer((OfflinePlayer) p, cost);

        //Add time to the hotel's rental period
        hotel.addTimeLeft(renewalPeriod);

        //Log the hotel renewal to the log file
        hotel.logHotelRenewal(plugin.getLogfile());

        //Send the player a confirmation message
        plugin.sendPlayerMessage(p, "messages.commands.renew", new String[]{Integer.toString(renewalPeriod)});

        return true;
    }

}
