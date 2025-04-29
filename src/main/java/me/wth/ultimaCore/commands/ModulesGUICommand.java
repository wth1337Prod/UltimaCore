package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.gui.ModulesGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;


public class ModulesGUICommand extends BaseCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
            return;
        }
        
        Player player = (Player) sender;
        
                new ModulesGUI(plugin).open(player);
    }
    
    @Override
    public String getName() {
        return "modulesgui";
    }
    
    @Override
    public String getDescription() {
        return "Отображает графический интерфейс с информацией о модулях плагина";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore modulesgui";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.modulesgui";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"mgui", "modules"};
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