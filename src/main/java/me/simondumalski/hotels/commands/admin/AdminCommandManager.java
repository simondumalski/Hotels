package me.simondumalski.hotels.commands.admin;

import me.simondumalski.hotels.Core;
import me.simondumalski.hotels.commands.SubCommand;
import me.simondumalski.hotels.commands.admin.subcommands.HelpCommand;
import me.simondumalski.hotels.commands.admin.subcommands.HomeCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AdminCommandManager implements TabExecutor {

    private Core plugin;
    private List<SubCommand> subCommands = new ArrayList<>();
    private List<SubCommand> doubleSubCommands = new ArrayList<>();

    public AdminCommandManager(Core plugin) {

        this.plugin = plugin;

        //Add the subcommands to the list
        subCommands.add(new HelpCommand(plugin, this));
        subCommands.add(new HomeCommand(plugin));

    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        //Check if the sender is a player
        if (sender instanceof Player) {

            Player p = (Player) sender;

            //Check if the player is sending a subcommand
            if (args.length > 0) {

                //Loop through the subcommands to find a matching one
                for (SubCommand subCommand : subCommands) {

                    //Check if the player is sending a valid subcommand and if they have permission for that command
                    if (args[0].equalsIgnoreCase(subCommand.getCommand()) && p.hasPermission(subCommand.getPermission())) {
                        return subCommand.perform(p, args);
                    }

                }

                //If no matching commands are found send the unknown command message
                plugin.sendPlayerMessage(p, "messages.errors.unknown-command", null);

            } else {

                //Find the help command and run it if the player has permission to
                for (SubCommand subCommand : subCommands) {
                    if (subCommand instanceof HelpCommand && p.hasPermission(subCommand.getPermission())) {
                        return subCommand.perform(p, args);
                    }
                }

                //Send the insufficient permissions message if the player does not have permission
                plugin.sendPlayerMessage(p, "messages.errors.insufficient-permissions", null);

            }

        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        //Make a list to hold the tab-complete arguments
        List<String> arguments = new ArrayList<>();

        //If the player is on the first argument, send the sub commands as tab-complete arguments
        if (args.length == 1) {
            for (SubCommand subCommand : subCommands) {
                arguments.add(subCommand.getCommand());
            }
            return arguments;
        }

        //If the player is on the second argument, send the list of online players as tab-complete arguments
        if (args.length == 2) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                arguments.add(p.getName());
            }
            return arguments;
        }

        return arguments;
    }

}
