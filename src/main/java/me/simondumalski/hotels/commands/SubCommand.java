package me.simondumalski.hotels.commands;

import me.simondumalski.hotels.Core;
import org.bukkit.entity.Player;

public abstract class SubCommand {

    public abstract String getCommand();
    public abstract String getDescription();
    public abstract String getUsage();
    public abstract String getPermission();
    public abstract boolean perform(Player p, String[] args);

}
