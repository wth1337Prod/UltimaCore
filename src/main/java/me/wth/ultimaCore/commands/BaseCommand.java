package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.UltimaCore;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseCommand {
    protected UltimaCore plugin;
    
    
    public abstract void execute(CommandSender sender, String[] args);
    
    
    public abstract String getName();
    
    
    public abstract String getDescription();
    
    
    public abstract String getSyntax();
    
    
    public abstract String getPermission();
    
    
    public String[] getAliases() {
        return new String[0];
    }
    
    
    public boolean isConsoleAllowed() {
        return true;
    }
    
    
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
    
    
    public void setPlugin(UltimaCore plugin) {
        this.plugin = plugin;
    }
} 