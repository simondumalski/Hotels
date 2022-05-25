package me.simondumalski.hotels.commands.player.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.utils.Hotel;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class RentCommand extends SubCommand {

    private Core plugin;

    public RentCommand(Core plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getCommand() {
        return "rent";
    }

    @Override
    public String getDescription() {
        return "Rent a hotel for <days> days.";
    }

    @Override
    public String getUsage() {
        return "/hotels rent <days>";
    }

    @Override
    public String getPermission() {
        return "hotels.rent";
    }

    @Override
    public boolean perform(Player p, String[] args) {

        //Check if the player is already renting a hotel
        if (plugin.getHotelsMap().containsKey(p.getUniqueId())) {
            plugin.sendPlayerMessage(p, "messages.errors.already-renting", null);
            return true;
        }
        //Check if the player has specified a rental period
        if (args.length < 2) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Initialize the rental period variable
        int rentalPeriod = 0;

        //Try to parse the rental period from the arguments
        try {
            rentalPeriod = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Check if the specified rental period is less than 1
        if (rentalPeriod < 1) {
            plugin.sendPlayerMessage(p, "messages.errors.invalid-period", null);
            return true;
        }

        //Get the player's allowed rental period
        int allowedRentalPeriod = plugin.getAllowedRentalPeriod(p);

        //Check if the player is trying to rent for longer than they are allowed to
        if (allowedRentalPeriod < rentalPeriod) {
            plugin.sendPlayerMessage(p, "messages.errors.too-long", new String[]{Integer.toString(plugin.getAllowedRentalPeriod(p))});
            return true;
        }

        //Calculate the cost based on the hotel price-per-day
        double cost = rentalPeriod * plugin.getConfig().getInt("hotels.price-per-day");

        //Check if the player has enough money to rent the hotel
        if (plugin.getEconomy().getBalance((OfflinePlayer) p) < cost) {
            plugin.sendPlayerMessage(p, "messages.errors.insufficient-funds", null);
            return true;
        }

        //Take the money from the player's balance
        plugin.getEconomy().withdrawPlayer((OfflinePlayer) p, cost);

        //Create a new hotel for the rental period in the next available chunk
        Chunk chunk = plugin.getNextAvailableChunk();
        Hotel hotel = new Hotel(86400 * rentalPeriod, chunk, p);

        //Add the hotel to the hotels map and paste the hotel schematic on its chunk
        plugin.getHotelsMap().put(p.getUniqueId(), hotel);
        plugin.pasteHotelSchematic(chunk);

        //Log the hotel creation to the log file
        hotel.logHotelCreation(plugin.getLogfile());

        //Send the player a confirmation message
        plugin.sendPlayerMessage(p, "messages.commands.rent", new String[]{Integer.toString(rentalPeriod)});

        return true;
    }

}
