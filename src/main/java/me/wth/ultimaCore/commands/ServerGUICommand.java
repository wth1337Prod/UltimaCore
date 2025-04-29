package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.gui.ServerStatusGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class ServerGUICommand extends BaseCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return;
        }
        
        Player player = (Player) sender;
        
                new ServerStatusGUI(plugin).open(player);
    }
    
    @Override
    public String getName() {
        return "servergui";
    }
    
    @Override
    public String getDescription() {
        return "Отображает графический интерфейс с информацией о состоянии сервера";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore servergui";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.servergui";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"sgui", "status", "gui"};
    }
    
    @Override
    public boolean isConsoleAllowed() {
        return false;
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return null;
    }
} 