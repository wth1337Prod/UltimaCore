package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CommandManager implements CommandExecutor, TabCompleter {
    private final UltimaCore plugin;
    private final Map<String, BaseCommand> commands = new HashMap<>();
    
    public CommandManager(UltimaCore plugin) {
        this.plugin = plugin;
        registerCommands();
        
                plugin.getCommand("ultimacore").setExecutor(this);
        plugin.getCommand("ultimacore").setTabCompleter(this);
    }
    
    
    private void registerCommands() {
                registerCommand(new StatusCommand());
        registerCommand(new AnalyzeCommand());
        registerCommand(new ProfileCommand());
        registerCommand(new ConfigCommand());
        registerCommand(new ReloadCommand());
        registerCommand(new HelpCommand());
        
                registerCommand(new ServerGUICommand());
        registerCommand(new ModulesGUICommand());
        
        LoggerUtil.info("Зарегистрировано " + commands.size() + " команд");
    }
    
    
    private void registerCommand(BaseCommand command) {
        command.setPlugin(plugin);
        commands.put(command.getName().toLowerCase(), command);
        
                for (String alias : command.getAliases()) {
            commands.put(alias.toLowerCase(), command);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
                        commands.get("help").execute(sender, args);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        BaseCommand command = commands.get(subCommand);
        
        if (command == null) {
            sender.sendMessage("§cНеизвестная команда. Используйте /ultimacore help для просмотра списка команд.");
            return true;
        }
        
                if (!sender.hasPermission(command.getPermission())) {
            sender.sendMessage("§cУ вас нет прав для выполнения этой команды.");
            return true;
        }
        
                if (!command.isConsoleAllowed() && !(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только для игроков.");
            return true;
        }
        
                String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        
        try {
                        command.execute(sender, newArgs);
        } catch (Exception e) {
            LoggerUtil.severe("Ошибка при выполнении команды " + command.getName(), e);
            sender.sendMessage("§cПроизошла ошибка при выполнении команды. Подробности в консоли.");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> result = new ArrayList<>();
        
        if (args.length == 1) {
                        String arg = args[0].toLowerCase();
            for (BaseCommand command : getUniqueCommands()) {
                if (command.getName().toLowerCase().startsWith(arg) && sender.hasPermission(command.getPermission())) {
                    result.add(command.getName());
                }
            }
        } else if (args.length > 1) {
                        String subCommand = args[0].toLowerCase();
            BaseCommand command = commands.get(subCommand);
            
            if (command != null && sender.hasPermission(command.getPermission())) {
                                String[] newArgs = new String[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                
                                List<String> suggestions = command.tabComplete(sender, newArgs);
                if (suggestions != null) {
                    result.addAll(suggestions);
                }
            }
        }
        
        return result;
    }
    
    
    private List<BaseCommand> getUniqueCommands() {
        List<BaseCommand> result = new ArrayList<>();
        for (BaseCommand command : commands.values()) {
            if (!result.contains(command)) {
                result.add(command);
            }
        }
        return result;
    }
    
    
    public BaseCommand getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
    
    
    public List<BaseCommand> getAllCommands() {
        return getUniqueCommands();
    }
} 