package me.kermx.horsestats;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final HorseStats plugin;
    public ReloadCommand(HorseStats plugin){this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (cmd.getName().equalsIgnoreCase("horsestats")){
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")){
                plugin.reloadConfig();
                plugin.loadConfig();
                sender.sendMessage(ChatColor.GREEN + "HorseStats configuration reloaded!");
                return true;
            }
        }
        sender.sendMessage(ChatColor.GREEN + "Incorrect Usage!");
        return false;
    }
}
