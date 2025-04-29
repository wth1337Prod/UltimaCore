package me.wth.ultimaCore.modules.redstonegenius;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class RedstoneGeniusCommand implements CommandExecutor, TabCompleter {
    private final RedstoneGeniusModule module;
    
    
    public RedstoneGeniusCommand(RedstoneGeniusModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.redstone.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "info":
                showInfo(sender);
                break;
                
            case "limit":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /redstone limit <число>");
                    return true;
                }
                try {
                    int limit = Integer.parseInt(args[1]);
                    module.setMaxRedstoneUpdatesPerChunk(limit);
                    sender.sendMessage(ChatColor.GREEN + "Лимит редстоуновых обновлений на чанк установлен: " + limit);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Пожалуйста, укажите число.");
                }
                break;
                
            case "clock":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /redstone clock <enable|disable>");
                    return true;
                }
                
                boolean enableClocks = args[1].equalsIgnoreCase("enable");
                module.setClockDetectionEnabled(enableClocks);
                sender.sendMessage(ChatColor.GREEN + "Обнаружение редстоун-часов " + 
                        (enableClocks ? "включено" : "выключено"));
                break;
                
            case "optimize":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /redstone optimize <enable|disable>");
                    return true;
                }
                
                boolean enableOptimize = args[1].equalsIgnoreCase("enable");
                module.setRedstoneDustOptimizationEnabled(enableOptimize);
                sender.sendMessage(ChatColor.GREEN + "Оптимизация редстоуновой пыли " + 
                        (enableOptimize ? "включена" : "выключена"));
                break;
                
            case "enable":
                module.setEnabled(true);
                sender.sendMessage(ChatColor.GREEN + "Модуль RedstoneGenius включен!");
                break;
                
            case "disable":
                module.setEnabled(false);
                sender.sendMessage(ChatColor.GREEN + "Модуль RedstoneGenius выключен!");
                break;
                
            case "chains":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /redstone chains <limit>");
                    return true;
                }
                
                try {
                    int chainLimit = Integer.parseInt(args[1]);
                    module.setMaxRedstoneChainLength(chainLimit);
                    sender.sendMessage(ChatColor.GREEN + "Максимальная длина редстоун-цепи установлена: " + chainLimit);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Пожалуйста, укажите число.");
                }
                break;
                
            case "interval":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /redstone interval <тики>");
                    return true;
                }
                
                try {
                    int interval = Integer.parseInt(args[1]);
                    module.setRedstoneCheckInterval(interval);
                    sender.sendMessage(ChatColor.GREEN + "Интервал проверки редстоуна установлен: " + interval + " тиков");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Пожалуйста, укажите число.");
                }
                break;
                
            case "status":
                showStatus(sender);
                break;
                
            case "reload":
                module.reloadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "Конфигурация RedstoneGenius перезагружена!");
                break;
                
            default:
                showHelp(sender);
                break;
        }
        
        return true;
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Помощь по командам RedstoneGenius ===");
        sender.sendMessage(ChatColor.GOLD + "/redstone info" + ChatColor.WHITE + " - Показать информацию о модуле");
        sender.sendMessage(ChatColor.GOLD + "/redstone status" + ChatColor.WHITE + " - Показать текущий статус модуля");
        sender.sendMessage(ChatColor.GOLD + "/redstone limit <число>" + ChatColor.WHITE + " - Установить лимит редстоуновых обновлений на чанк");
        sender.sendMessage(ChatColor.GOLD + "/redstone clock <enable|disable>" + ChatColor.WHITE + " - Включить/выключить обнаружение редстоун-часов");
        sender.sendMessage(ChatColor.GOLD + "/redstone optimize <enable|disable>" + ChatColor.WHITE + " - Включить/выключить оптимизацию редстоуновой пыли");
        sender.sendMessage(ChatColor.GOLD + "/redstone chains <limit>" + ChatColor.WHITE + " - Установить максимальную длину редстоун-цепи");
        sender.sendMessage(ChatColor.GOLD + "/redstone interval <тики>" + ChatColor.WHITE + " - Установить интервал проверки редстоуна");
        sender.sendMessage(ChatColor.GOLD + "/redstone enable" + ChatColor.WHITE + " - Включить модуль");
        sender.sendMessage(ChatColor.GOLD + "/redstone disable" + ChatColor.WHITE + " - Выключить модуль");
        sender.sendMessage(ChatColor.GOLD + "/redstone reload" + ChatColor.WHITE + " - Перезагрузить конфигурацию");
    }
    
    
    private void showInfo(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== RedstoneGenius ===");
        sender.sendMessage(ChatColor.GOLD + "Описание: " + ChatColor.WHITE + "Модуль оптимизации редстоуновых механизмов и схем");
        sender.sendMessage(ChatColor.GOLD + "Автор: " + ChatColor.WHITE + "UltimaTeam");
        sender.sendMessage(ChatColor.GOLD + "Версия: " + ChatColor.WHITE + "1.0.0");
        sender.sendMessage(ChatColor.GOLD + "Статус: " + (module.isEnabled() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GREEN + "Основные функции:");
        sender.sendMessage(ChatColor.WHITE + "- Ограничение количества редстоуновых обновлений на чанк");
        sender.sendMessage(ChatColor.WHITE + "- Обнаружение и оптимизация редстоун-часов");
        sender.sendMessage(ChatColor.WHITE + "- Оптимизация редстоуновой пыли");
        sender.sendMessage(ChatColor.WHITE + "- Ограничение длины редстоун-цепей");
    }
    
    
    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Статус RedstoneGenius ===");
        sender.sendMessage(ChatColor.GOLD + "Модуль: " + (module.isEnabled() ? ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        sender.sendMessage(ChatColor.GOLD + "Лимит редстоуновых обновлений на чанк: " + ChatColor.WHITE + module.getMaxRedstoneUpdatesPerChunk());
        sender.sendMessage(ChatColor.GOLD + "Обнаружение редстоун-часов: " + 
                (module.isClockDetectionEnabled() ? ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено"));
        sender.sendMessage(ChatColor.GOLD + "Оптимизация редстоуновой пыли: " + 
                (module.isRedstoneDustOptimizationEnabled() ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Выключена"));
        sender.sendMessage(ChatColor.GOLD + "Максимальная длина редстоун-цепи: " + ChatColor.WHITE + module.getMaxRedstoneChainLength());
        sender.sendMessage(ChatColor.GOLD + "Интервал проверки редстоуна: " + ChatColor.WHITE + module.getRedstoneCheckInterval() + " тиков");
        
                int detectedClocks = module.getDetectedClocks().size();
        sender.sendMessage(ChatColor.GOLD + "Обнаружено редстоун-часов: " + ChatColor.WHITE + detectedClocks);
        
                int blockPhysicsEvents = module.getEventCount("block_physics");
        int redstoneEvents = module.getEventCount("redstone");
        sender.sendMessage(ChatColor.GOLD + "События физики блоков: " + ChatColor.WHITE + blockPhysicsEvents);
        sender.sendMessage(ChatColor.GOLD + "События редстоуна: " + ChatColor.WHITE + redstoneEvents);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.redstone.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
                        List<String> subcommands = Arrays.asList("info", "status", "limit", "clock", "optimize", "chains", "interval", "enable", "disable", "reload");
            return subcommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
                        String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("clock") || subcommand.equals("optimize")) {
                List<String> options = Arrays.asList("enable", "disable");
                return options.stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subcommand.equals("limit")) {
                List<String> options = Arrays.asList("50", "100", "150", "200");
                return options.stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (subcommand.equals("chains")) {
                List<String> options = Arrays.asList("50", "100", "150", "200");
                return options.stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            } else if (subcommand.equals("interval")) {
                List<String> options = Arrays.asList("10", "20", "40", "60");
                return options.stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 