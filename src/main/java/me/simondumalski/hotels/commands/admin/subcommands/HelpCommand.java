package me.simondumalski.hotels.commands.admin.subcommands;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.admin.AdminCommandManager;
import me.simondumalski.hotels.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HelpCommand extends SubCommand {

    private Core plugin;
    private AdminCommandManager commandManager;

    public HelpCommand(Core plugin, AdminCommandManager commandManager) {
        this.plugin = plugin;
        this.commandManager = commandManager;
    }

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays the admin help menu.";
    }

    @Override
    public String getUsage() {
        return "/hotelsadmin help";
    }

    @Override
    public String getPermission() {
        return "hotels.admin";
    }

    @Override
    public boolean perform(Player p, String[] args) {

        //Send the plugin header
        plugin.sendMessageHeader(p);

        //Loop through the subcommands list
        for (SubCommand subCommand : commandManager.getSubCommands()) {

            //Get the help message from the config.yml
            String message = plugin.getConfig().getString("messages.admin.help");

            //Replace the command placeholder with the command usage
            if (message.contains("%command%")) {
                message = message.replace("%command%", subCommand.getUsage());
            }

            //Replace the description placeholder with the command description
            if (message.contains("%description%")) {
                message = message.replace("%description", subCommand.getDescription());
            }

            //Send the command help message to the player
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        }

        //Send the plugin footer
        plugin.sendMessageHeader(p);

        return true;
    }

}
