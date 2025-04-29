package me.wth.ultimaCore.modules.protocoloptimizer.commands;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.modules.protocoloptimizer.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ProtocolOptimizerCommand implements CommandExecutor, TabCompleter {

    private final ProtocolOptimizerModule module;
    
        private final List<String> subCommands = Arrays.asList(
            "help", "stats", "enable", "disable", "reload", 
            "latency", "filter", "optimize", "cleardata", 
            "entities", "blocks", "packets", "tps", "cache"
    );
    
    
    public ProtocolOptimizerCommand(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help":
                showHelp(sender);
                break;
                
            case "stats":
                showStats(sender);
                break;
                
            case "enable":
                if (hasAdminPermission(sender)) {
                    enableOptimization(sender, args);
                }
                break;
                
            case "disable":
                if (hasAdminPermission(sender)) {
                    disableOptimization(sender, args);
                }
                break;
                
            case "reload":
                if (hasAdminPermission(sender)) {
                    reloadSettings(sender);
                }
                break;
                
            case "latency":
                showLatencyStats(sender, args);
                break;
                
            case "filter":
                if (hasAdminPermission(sender)) {
                    manageFilter(sender, args);
                }
                break;
                
            case "optimize":
                if (hasAdminPermission(sender)) {
                    runOptimization(sender);
                }
                break;
                
            case "cleardata":
                if (hasAdminPermission(sender)) {
                    clearData(sender);
                }
                break;
                
            case "entities":
                showEntitiesStats(sender);
                break;
                
            case "blocks":
                showBlocksStats(sender);
                break;
                
            case "packets":
                showPacketsStats(sender);
                break;
                
            case "tps":
                showTps(sender);
                break;
                
            case "cache":
                showCacheStats(sender);
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Неизвестная подкоманда. Используйте /protocol help для справки.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(partialCommand)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String partial = args[1].toLowerCase();
            
            if (subCommand.equals("enable") || subCommand.equals("disable")) {
                List<String> options = Arrays.asList("all", "entities", "blocks", "movement", "filter");
                for (String option : options) {
                    if (option.startsWith(partial)) {
                        completions.add(option);
                    }
                }
            } else if (subCommand.equals("filter")) {
                List<String> options = Arrays.asList("add", "remove", "clear", "list");
                for (String option : options) {
                    if (option.startsWith(partial)) {
                        completions.add(option);
                    }
                }
            } else if (subCommand.equals("latency") && sender.hasPermission("ultimacore.protocol.admin")) {
                                for (Player player : sender.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(partial)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            
            if (subCommand.equals("filter") && action.equals("add")) {
                List<String> packetTypes = Arrays.asList(
                        "ABILITIES", "ARM_ANIMATION", "KEEP_ALIVE", "ENTITY_VELOCITY",
                        "POSITION", "LOOK", "FLYING", "TRANSACTION", "ENTITY_ACTION",
                        "PLAYER_DIGGING", "BLOCK_PLACE", "HELD_ITEM_SLOT"
                );
                
                String partial = args[2].toLowerCase();
                for (String type : packetTypes) {
                    if (type.toLowerCase().startsWith(partial)) {
                        completions.add(type);
                    }
                }
            } else if (subCommand.equals("filter") && action.equals("remove")) {
                                if (module.getSettings() != null) {
                    List<String> blockedTypes = module.getSettings().getBlockedPacketTypes();
                    String partial = args[2].toLowerCase();
                    
                    for (String type : blockedTypes) {
                        if (type.toLowerCase().startsWith(partial)) {
                            completions.add(type);
                        }
                    }
                }
            }
        }
        
        return completions;
    }
    
    
    private boolean hasAdminPermission(CommandSender sender) {
        if (!sender.hasPermission("ultimacore.protocol.admin")) {
            MessageUtils.sendErrorMessage(sender, "У вас нет прав для выполнения этой команды.");
            return false;
        }
        return true;
    }
    
    
    private void showHelp(CommandSender sender) {
        MessageUtils.sendHeader(sender, "Помощь по команде /protocol");
        MessageUtils.sendListItem(sender, "/protocol help - Показать эту справку");
        MessageUtils.sendListItem(sender, "/protocol stats - Показать общую статистику оптимизации протокола");
        MessageUtils.sendListItem(sender, "/protocol latency [игрок] - Показать статистику задержек");
        MessageUtils.sendListItem(sender, "/protocol tps - Показать текущий TPS сервера");
        
        if (sender.hasPermission("ultimacore.protocol.admin")) {
            MessageUtils.sendListItem(sender, "&bКоманды администратора:");
            MessageUtils.sendListItem(sender, "/protocol enable <all|entities|blocks|movement|filter> - Включить оптимизацию");
            MessageUtils.sendListItem(sender, "/protocol disable <all|entities|blocks|movement|filter> - Отключить оптимизацию");
            MessageUtils.sendListItem(sender, "/protocol reload - Перезагрузить настройки");
            MessageUtils.sendListItem(sender, "/protocol filter <add|remove|clear|list> [тип] - Управление фильтром пакетов");
            MessageUtils.sendListItem(sender, "/protocol optimize - Выполнить ручную оптимизацию");
            MessageUtils.sendListItem(sender, "/protocol cleardata - Очистить все данные оптимизатора");
            MessageUtils.sendListItem(sender, "/protocol entities - Показать статистику оптимизации сущностей");
            MessageUtils.sendListItem(sender, "/protocol blocks - Показать статистику оптимизации блоков");
            MessageUtils.sendListItem(sender, "/protocol packets - Показать статистику фильтрации пакетов");
            MessageUtils.sendListItem(sender, "/protocol cache - Показать статистику кэширования пакетов");
        }
    }
    
    
    private void showStats(CommandSender sender) {
        MessageUtils.sendHeader(sender, "Статистика оптимизации протокола");
        
        if (module.getSettings() == null || !module.getSettings().isEnabled()) {
            MessageUtils.sendErrorMessage(sender, "Модуль оптимизации протокола выключен.");
            return;
        }
        
        String stats = module.getStats();
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    
    private void enableOptimization(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Укажите что включить: all, entities, blocks, movement, filter");
            return;
        }
        
        String type = args[1].toLowerCase();
        
        switch (type) {
            case "all":
                module.getSettings().setOptimizeEntityPackets(true);
                module.getSettings().setOptimizeBlockPackets(true);
                module.getSettings().setOptimizeMovementPackets(true);
                module.getSettings().setFilterPackets(true);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Все оптимизации протокола включены.");
                break;
                
            case "entities":
                module.getSettings().setOptimizeEntityPackets(true);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация сущностей включена.");
                break;
                
            case "blocks":
                module.getSettings().setOptimizeBlockPackets(true);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация блоков включена.");
                break;
                
            case "movement":
                module.getSettings().setOptimizeMovementPackets(true);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация движения включена.");
                break;
                
            case "filter":
                module.getSettings().setFilterPackets(true);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Фильтрация пакетов включена.");
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Неизвестный тип оптимизации: " + type);
                break;
        }
    }
    
    
    private void disableOptimization(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Укажите что отключить: all, entities, blocks, movement, filter");
            return;
        }
        
        String type = args[1].toLowerCase();
        
        switch (type) {
            case "all":
                module.getSettings().setOptimizeEntityPackets(false);
                module.getSettings().setOptimizeBlockPackets(false);
                module.getSettings().setOptimizeMovementPackets(false);
                module.getSettings().setFilterPackets(false);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Все оптимизации протокола отключены.");
                break;
                
            case "entities":
                module.getSettings().setOptimizeEntityPackets(false);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация сущностей отключена.");
                break;
                
            case "blocks":
                module.getSettings().setOptimizeBlockPackets(false);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация блоков отключена.");
                break;
                
            case "movement":
                module.getSettings().setOptimizeMovementPackets(false);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Оптимизация движения отключена.");
                break;
                
            case "filter":
                module.getSettings().setFilterPackets(false);
                module.getSettings().saveConfig();
                MessageUtils.sendSuccessMessage(sender, "Фильтрация пакетов отключена.");
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Неизвестный тип оптимизации: " + type);
                break;
        }
    }
    
    
    private void reloadSettings(CommandSender sender) {
        module.getSettings().loadConfig();
        MessageUtils.sendSuccessMessage(sender, "Настройки модуля оптимизации протокола перезагружены.");
    }
    
    
    private void showLatencyStats(CommandSender sender, String[] args) {
        if (args.length > 1 && sender.hasPermission("ultimacore.protocol.admin")) {
                        String playerName = args[1];
            Player player = sender.getServer().getPlayer(playerName);
            
            if (player == null) {
                MessageUtils.sendErrorMessage(sender, "Игрок не найден или не в сети.");
                return;
            }
            
            UUID playerUUID = player.getUniqueId();
            String stats = module.getPlayerLatencyStats(playerUUID);
            for (String line : stats.split("\n")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        } else {
                        String stats = module.getLatencyStats();
            for (String line : stats.split("\n")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
        }
    }
    
    
    private void manageFilter(CommandSender sender, String[] args) {
        if (args.length < 2) {
            MessageUtils.sendErrorMessage(sender, "Укажите действие: add, remove, clear, list");
            return;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Укажите тип пакета для добавления в черный список.");
                    return;
                }
                
                String packetType = args[2].toUpperCase();
                module.addBlockedPacketType(packetType);
                MessageUtils.sendSuccessMessage(sender, "Тип пакета " + packetType + " добавлен в черный список.");
                break;
                
            case "remove":
                if (args.length < 3) {
                    MessageUtils.sendErrorMessage(sender, "Укажите тип пакета для удаления из черного списка.");
                    return;
                }
                
                String packetTypeToRemove = args[2].toUpperCase();
                module.removeBlockedPacketType(packetTypeToRemove);
                MessageUtils.sendSuccessMessage(sender, "Тип пакета " + packetTypeToRemove + " удален из черного списка.");
                break;
                
            case "clear":
                module.clearBlockedPacketTypes();
                MessageUtils.sendSuccessMessage(sender, "Черный список пакетов очищен.");
                break;
                
            case "list":
                List<String> blockedTypes = module.getSettings().getBlockedPacketTypes();
                
                if (blockedTypes.isEmpty()) {
                    MessageUtils.sendInfoMessage(sender, "Черный список пакетов пуст.");
                } else {
                    MessageUtils.sendHeader(sender, "Черный список пакетов");
                    for (String type : blockedTypes) {
                        MessageUtils.sendListItem(sender, type);
                    }
                }
                break;
                
            default:
                MessageUtils.sendErrorMessage(sender, "Неизвестное действие: " + action);
                break;
        }
    }
    
    
    private void runOptimization(CommandSender sender) {
        module.runManualOptimization();
        MessageUtils.sendSuccessMessage(sender, "Ручная оптимизация протокола выполнена.");
    }
    
    
    private void clearData(CommandSender sender) {
        module.clearAllData();
        MessageUtils.sendSuccessMessage(sender, "Все данные модуля оптимизации протокола очищены.");
    }
    
    
    private void showEntitiesStats(CommandSender sender) {
        if (module.entityPacketOptimizer == null) {
            MessageUtils.sendErrorMessage(sender, "Оптимизатор пакетов сущностей не инициализирован.");
            return;
        }
        
        String stats = module.entityPacketOptimizer.getStats();
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    
    private void showBlocksStats(CommandSender sender) {
        if (module.blockPacketOptimizer == null) {
            MessageUtils.sendErrorMessage(sender, "Оптимизатор пакетов блоков не инициализирован.");
            return;
        }
        
        String stats = module.blockPacketOptimizer.getStats();
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    
    private void showPacketsStats(CommandSender sender) {
        String stats = module.getFilterStats();
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
    
    
    private void showTps(CommandSender sender) {
        double tps = module.getTPS();
        String color;
        
        if (tps >= 19.0) {
            color = "&a";         } else if (tps >= 16.0) {
            color = "&e";         } else {
            color = "&c";         }
        
        MessageUtils.sendInfoMessage(sender, "Текущий TPS сервера: " + color + String.format("%.2f", tps));
    }
    
    
    private void showCacheStats(CommandSender sender) {
        if (module.getCacheManager() == null) {
            MessageUtils.sendErrorMessage(sender, "Менеджер кэширования пакетов не инициализирован.");
            return;
        }
        
        String stats = module.getCacheManager().getStats();
        for (String line : stats.split("\n")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }
} 