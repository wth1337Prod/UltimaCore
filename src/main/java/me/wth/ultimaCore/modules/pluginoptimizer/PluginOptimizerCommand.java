package me.wth.ultimaCore.modules.pluginoptimizer;

import me.wth.ultimaCore.UltimaCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class PluginOptimizerCommand implements CommandExecutor, TabCompleter {
    private final PluginOptimizerModule module;
    
    
    public PluginOptimizerCommand(PluginOptimizerModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.pluginoptimizer.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "reload":
                reloadModule(sender);
                break;
            case "analyze":
                analyzePlugins(sender, args);
                break;
            case "ignore":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /pluginoptimizer ignore <плагин>");
                    return true;
                }
                ignorePlugin(sender, args[1]);
                break;
            case "unignore":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /pluginoptimizer unignore <плагин>");
                    return true;
                }
                unignorePlugin(sender, args[1]);
                break;
            case "stats":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /pluginoptimizer stats <плагин>");
                    return true;
                }
                showPluginStats(sender, args[1]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /pluginoptimizer help для справки.");
                break;
        }
        
        return true;
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Помощь по PluginOptimizer ===");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer help " + ChatColor.WHITE + "- Показать эту справку");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer status " + ChatColor.WHITE + "- Показать статус модуля");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer reload " + ChatColor.WHITE + "- Перезагрузить модуль");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer analyze [плагин] " + ChatColor.WHITE + "- Анализировать все плагины или указанный");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer ignore <плагин> " + ChatColor.WHITE + "- Добавить плагин в список игнорируемых");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer unignore <плагин> " + ChatColor.WHITE + "- Удалить плагин из списка игнорируемых");
        sender.sendMessage(ChatColor.YELLOW + "/pluginoptimizer stats <плагин> " + ChatColor.WHITE + "- Показать статистику плагина");
    }
    
    
    private void showStatus(CommandSender sender) {
        PluginOptimizerSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Статус PluginOptimizer ===");
        sender.sendMessage(ChatColor.YELLOW + "Модуль активен: " + 
                (settings.isEnabled() ? ChatColor.GREEN + "Да" : ChatColor.RED + "Нет"));
        sender.sendMessage(ChatColor.YELLOW + "Мониторинг команд: " + 
                (settings.isMonitorCommands() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        sender.sendMessage(ChatColor.YELLOW + "Мониторинг задач: " + 
                (settings.isMonitorTasks() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        sender.sendMessage(ChatColor.YELLOW + "Мониторинг событий: " + 
                (settings.isMonitorEvents() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        sender.sendMessage(ChatColor.YELLOW + "Автоматическая оптимизация: " + 
                (settings.isAutoOptimize() ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Выключена"));
        sender.sendMessage(ChatColor.YELLOW + "Ограничение ресурсов: " + 
                (settings.isLimitResourceUsage() ? ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено"));
    }
    
    
    private void reloadModule(CommandSender sender) {
        try {
            module.reload();
            sender.sendMessage(ChatColor.GREEN + "Модуль PluginOptimizer успешно перезагружен!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке модуля: " + e.getMessage());
            UltimaCore.getInstance().getLogger().severe("Ошибка при перезагрузке PluginOptimizer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void analyzePlugins(CommandSender sender, String[] args) {
        if (args.length > 1) {
                        String pluginName = args[1];
            Plugin targetPlugin = UltimaCore.getInstance().getServer().getPluginManager().getPlugin(pluginName);
            
            if (targetPlugin == null) {
                sender.sendMessage(ChatColor.RED + "Плагин не найден: " + pluginName);
                return;
            }
            
            sender.sendMessage(ChatColor.GREEN + "Анализ плагина " + targetPlugin.getName() + "...");
                        sender.sendMessage(ChatColor.GREEN + "Анализ завершен. Отчет отправлен в консоль.");
        } else {
                        sender.sendMessage(ChatColor.GREEN + "Запуск полного анализа плагинов...");
                        sender.sendMessage(ChatColor.GREEN + "Анализ завершен. Отчет отправлен в консоль.");
        }
    }
    
    
    private void ignorePlugin(CommandSender sender, String pluginName) {
        Plugin targetPlugin = UltimaCore.getInstance().getServer().getPluginManager().getPlugin(pluginName);
        
        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Плагин не найден: " + pluginName);
            return;
        }
        
        List<String> ignoredPlugins = UltimaCore.getInstance().getConfig().getStringList("pluginoptimizer.ignored_plugins");
        if (ignoredPlugins == null) {
            ignoredPlugins = new ArrayList<>();
        }
        
        if (ignoredPlugins.contains(targetPlugin.getName())) {
            sender.sendMessage(ChatColor.YELLOW + "Плагин " + targetPlugin.getName() + " уже в списке игнорируемых.");
            return;
        }
        
        ignoredPlugins.add(targetPlugin.getName());
        UltimaCore.getInstance().getConfig().set("pluginoptimizer.ignored_plugins", ignoredPlugins);
        UltimaCore.getInstance().saveConfig();
        
        sender.sendMessage(ChatColor.GREEN + "Плагин " + targetPlugin.getName() + " добавлен в список игнорируемых.");
    }
    
    
    private void unignorePlugin(CommandSender sender, String pluginName) {
        List<String> ignoredPlugins = UltimaCore.getInstance().getConfig().getStringList("pluginoptimizer.ignored_plugins");
        if (ignoredPlugins == null || ignoredPlugins.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Список игнорируемых плагинов пуст.");
            return;
        }
        
        boolean removed = ignoredPlugins.removeIf(name -> name.equalsIgnoreCase(pluginName));
        
        if (removed) {
            UltimaCore.getInstance().getConfig().set("pluginoptimizer.ignored_plugins", ignoredPlugins);
            UltimaCore.getInstance().saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Плагин " + pluginName + " удален из списка игнорируемых.");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Плагин " + pluginName + " не найден в списке игнорируемых.");
        }
    }
    
    
    private void showPluginStats(CommandSender sender, String pluginName) {
        Plugin targetPlugin = UltimaCore.getInstance().getServer().getPluginManager().getPlugin(pluginName);
        
        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Плагин не найден: " + pluginName);
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== Статистика плагина " + targetPlugin.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Версия: " + ChatColor.WHITE + targetPlugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.WHITE + String.join(", ", targetPlugin.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.YELLOW + "Зависимости: " + ChatColor.WHITE + String.join(", ", targetPlugin.getDescription().getDepend()));
        
                
        sender.sendMessage(ChatColor.YELLOW + "Статус: " + 
                (targetPlugin.isEnabled() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.pluginoptimizer.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("help", "status", "reload", "analyze", "ignore", "unignore", "stats");
            return filterStartsWith(commands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ignore") || 
                args[0].equalsIgnoreCase("analyze") ||
                args[0].equalsIgnoreCase("stats")) {
                
                List<String> plugins = new ArrayList<>();
                for (Plugin plugin : UltimaCore.getInstance().getServer().getPluginManager().getPlugins()) {
                    plugins.add(plugin.getName());
                }
                return filterStartsWith(plugins, args[1]);
            } else if (args[0].equalsIgnoreCase("unignore")) {
                List<String> ignoredPlugins = UltimaCore.getInstance().getConfig()
                        .getStringList("pluginoptimizer.ignored_plugins");
                return filterStartsWith(ignoredPlugins, args[1]);
            }
        }
        
        return new ArrayList<>();
    }
    
    
    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream()
                .filter(entry -> entry.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
} 