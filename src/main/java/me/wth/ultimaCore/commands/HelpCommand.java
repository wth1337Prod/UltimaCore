package me.wth.ultimaCore.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class HelpCommand extends BaseCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        int page = 1;
        
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                                showCommandHelp(sender, args[0]);
                return;
            }
        }
        
        showHelpPage(sender, page);
    }
    
    
    private void showCommandHelp(CommandSender sender, String commandName) {
        CommandManager commandManager = plugin.getCommandManager();
        BaseCommand command = commandManager.getCommand(commandName.toLowerCase());
        
        if (command == null) {
            sender.sendMessage(ChatColor.RED + "Команда " + commandName + " не найдена");
            return;
        }
        
        if (!sender.hasPermission(command.getPermission())) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для просмотра помощи по этой команде");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Помощь по команде: " + command.getName() + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.GOLD + "Синтаксис: " + ChatColor.YELLOW + command.getSyntax());
        sender.sendMessage(ChatColor.GOLD + "Описание: " + ChatColor.YELLOW + command.getDescription());
        sender.sendMessage(ChatColor.GOLD + "Разрешение: " + ChatColor.YELLOW + command.getPermission());
        
                String[] aliases = command.getAliases();
        if (aliases.length > 0) {
            sender.sendMessage(ChatColor.GOLD + "Алиасы: " + ChatColor.YELLOW + String.join(", ", aliases));
        }
        
                sender.sendMessage(ChatColor.GOLD + "Доступна из консоли: " + 
                (command.isConsoleAllowed() ? ChatColor.GREEN + "Да" : ChatColor.RED + "Нет"));
    }
    
    
    private void showHelpPage(CommandSender sender, int page) {
        CommandManager commandManager = plugin.getCommandManager();
        List<BaseCommand> allCommands = commandManager.getAllCommands();
        
                List<BaseCommand> availableCommands = new ArrayList<>();
        for (BaseCommand command : allCommands) {
            if (sender.hasPermission(command.getPermission())) {
                availableCommands.add(command);
            }
        }
        
                availableCommands.sort(Comparator.comparing(BaseCommand::getName));
        
                int commandsPerPage = 8;
        int totalPages = (int) Math.ceil((double) availableCommands.size() / commandsPerPage);
        
                if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }
        
                sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "UltimaCore - Справка (Страница " + 
                page + " из " + totalPages + ")" + ChatColor.GOLD + " ===");
        
                if (availableCommands.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Нет доступных команд");
            return;
        }
        
                int startIndex = (page - 1) * commandsPerPage;
        int endIndex = Math.min(startIndex + commandsPerPage, availableCommands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            BaseCommand command = availableCommands.get(i);
            sender.sendMessage(ChatColor.YELLOW + command.getSyntax() + ChatColor.GRAY + " - " + 
                    ChatColor.WHITE + command.getDescription());
        }
        
                if (totalPages > 1) {
            sender.sendMessage(ChatColor.GOLD + "Используйте " + ChatColor.YELLOW + 
                    "/ultimacore help [страница]" + ChatColor.GOLD + " для переключения страниц");
        }
        
                sender.sendMessage(ChatColor.GOLD + "Используйте " + ChatColor.YELLOW + 
                "/ultimacore help [команда]" + ChatColor.GOLD + " для подробной справки по команде");
    }
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "Показывает справку по командам плагина";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore help [страница|команда]";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.command.help";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"?", "h"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        
        if (args.length == 1) {
                        CommandManager commandManager = plugin.getCommandManager();
            List<BaseCommand> allCommands = commandManager.getAllCommands();
            
                        List<BaseCommand> availableCommands = new ArrayList<>();
            for (BaseCommand command : allCommands) {
                if (sender.hasPermission(command.getPermission())) {
                    availableCommands.add(command);
                }
            }
            
                        for (BaseCommand command : availableCommands) {
                if (command.getName().startsWith(args[0].toLowerCase())) {
                    result.add(command.getName());
                }
            }
            
                        int commandsPerPage = 8;
            int totalPages = (int) Math.ceil((double) availableCommands.size() / commandsPerPage);
            for (int i = 1; i <= totalPages; i++) {
                String pageNum = String.valueOf(i);
                if (pageNum.startsWith(args[0])) {
                    result.add(pageNum);
                }
            }
        }
        
        return result;
    }
} 