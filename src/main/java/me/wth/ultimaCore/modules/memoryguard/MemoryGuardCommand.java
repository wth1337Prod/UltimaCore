package me.wth.ultimaCore.modules.memoryguard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class MemoryGuardCommand implements CommandExecutor, TabCompleter {
    private final MemoryGuardModule module;
    private final String[] subCommands = {"status", "cleanup", "settings", "help"};
    private final String[] settingsCommands = {"enable", "disable", "set", "list", "add", "remove"};
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
    
    public MemoryGuardCommand(MemoryGuardModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.memoryguard.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет доступа к этой команде.");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                showStatus(sender);
                break;
            case "cleanup":
                performCleanup(sender, args);
                break;
            case "settings":
                handleSettings(sender, args);
                break;
            case "help":
                showHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /memoryguard help для справки.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.memoryguard.admin")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            return filterStartingWith(args[0], subCommands);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("settings")) {
                return filterStartingWith(args[1], settingsCommands);
            } else if (args[0].equalsIgnoreCase("cleanup")) {
                return filterStartingWith(args[1], new String[]{"items", "entities", "chunks", "gc", "all"});
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settings")) {
                if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable")) {
                    return filterStartingWith(args[2], getSettingsOptions());
                } else if (args[1].equalsIgnoreCase("set")) {
                    return filterStartingWith(args[2], getNumericalSettingsOptions());
                } else if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    if (args[2].equalsIgnoreCase("world")) {
                        return filterStartingWith(args[3], Bukkit.getWorlds().stream()
                                .map(World::getName)
                                .collect(Collectors.toList()));
                    } else if (args[2].equalsIgnoreCase("entityType")) {
                        return filterStartingWith(args[3], Arrays.stream(EntityType.values())
                                .map(EntityType::name)
                                .collect(Collectors.toList()));
                    }
                }
            }
        }
        
        return Collections.emptyList();
    }
    
    
    private List<String> filterStartingWith(String prefix, String[] options) {
        return filterStartingWith(prefix, Arrays.asList(options));
    }
    
    
    private List<String> filterStartingWith(String prefix, List<String> options) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    
    private void showStatus(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usedPercentage = (double) usedMemory / maxMemory * 100;
        double allocatedPercentage = (double) totalMemory / maxMemory * 100;
        
        sender.sendMessage(ChatColor.GREEN + "=== Статус памяти сервера ===");
        sender.sendMessage(ChatColor.YELLOW + "Используется: " + formatBytes(usedMemory) + " (" + 
                decimalFormat.format(usedPercentage) + "%)");
        sender.sendMessage(ChatColor.YELLOW + "Выделено: " + formatBytes(totalMemory) + " (" + 
                decimalFormat.format(allocatedPercentage) + "%)");
        sender.sendMessage(ChatColor.YELLOW + "Максимум: " + formatBytes(maxMemory));
        sender.sendMessage(ChatColor.YELLOW + "Свободно: " + formatBytes(freeMemory));
        
                MemoryGuardSettings settings = module.getSettings();
        sender.sendMessage(ChatColor.YELLOW + "Порог уведомления: " + decimalFormat.format(settings.getNoticeThreshold()) + "%");
        sender.sendMessage(ChatColor.YELLOW + "Порог предупреждения: " + decimalFormat.format(settings.getWarningThreshold()) + "%");
        sender.sendMessage(ChatColor.YELLOW + "Критический порог: " + decimalFormat.format(settings.getCriticalThreshold()) + "%");
    }
    
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return decimalFormat.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return decimalFormat.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return decimalFormat.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }
    
    
    private void performCleanup(CommandSender sender, String[] args) {
        String type = args.length > 1 ? args[1].toLowerCase() : "all";
        
        switch (type) {
            case "items":
                sender.sendMessage(ChatColor.YELLOW + "Выполняется очистка предметов...");
                Bukkit.getScheduler().runTask(module.getPlugin(), () -> {
                    int removed = clearItems();
                    sender.sendMessage(ChatColor.GREEN + "Очистка завершена! Удалено " + removed + " предметов.");
                });
                break;
            case "entities":
                sender.sendMessage(ChatColor.YELLOW + "Выполняется очистка сущностей...");
                Bukkit.getScheduler().runTask(module.getPlugin(), () -> {
                    int removed = clearEntities();
                    sender.sendMessage(ChatColor.GREEN + "Очистка завершена! Удалено " + removed + " сущностей.");
                });
                break;
            case "chunks":
                sender.sendMessage(ChatColor.YELLOW + "Выполняется выгрузка неиспользуемых чанков...");
                Bukkit.getScheduler().runTask(module.getPlugin(), () -> {
                    int removed = unloadChunks();
                    sender.sendMessage(ChatColor.GREEN + "Выгрузка завершена! Выгружено " + removed + " чанков.");
                });
                break;
            case "gc":
                sender.sendMessage(ChatColor.YELLOW + "Выполняется сборка мусора...");
                System.gc();
                sender.sendMessage(ChatColor.GREEN + "Сборка мусора завершена!");
                break;
            case "all":
                sender.sendMessage(ChatColor.YELLOW + "Выполняется полная очистка...");
                Bukkit.getScheduler().runTask(module.getPlugin(), () -> {
                    int removedItems = clearItems();
                    int removedEntities = clearEntities();
                    int unloadedChunks = unloadChunks();
                    System.gc();
                    
                    sender.sendMessage(ChatColor.GREEN + "Очистка завершена!");
                    sender.sendMessage(ChatColor.YELLOW + "Удалено предметов: " + removedItems);
                    sender.sendMessage(ChatColor.YELLOW + "Удалено сущностей: " + removedEntities);
                    sender.sendMessage(ChatColor.YELLOW + "Выгружено чанков: " + unloadedChunks);
                });
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный тип очистки: " + type);
                sender.sendMessage(ChatColor.RED + "Доступные типы: items, entities, chunks, gc, all");
                break;
        }
    }
    
    
    private int clearItems() {
        MemoryGuardSettings settings = module.getSettings();
        int removed = 0;
        
        for (World world : Bukkit.getWorlds()) {
            if (settings.isItemsRemovalEnabled(world.getName())) {
                removed += world.getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                        .filter(item -> !item.hasMetadata("protected"))
                        .peek(org.bukkit.entity.Entity::remove)
                        .count();
            }
        }
        
        return removed;
    }
    
    
    private int clearEntities() {
        MemoryGuardSettings settings = module.getSettings();
        int removed = 0;
        
        for (World world : Bukkit.getWorlds()) {
            if (settings.isEntityRemovalEnabled(world.getName())) {
                for (org.bukkit.entity.Entity entity : world.getEntities()) {
                    if (entity instanceof org.bukkit.entity.Player || entity.hasMetadata("protected")) {
                        continue;
                    }
                    
                    if (settings.isEntityTypeRemovable(entity.getType())) {
                        entity.remove();
                        removed++;
                    }
                }
            }
        }
        
        return removed;
    }
    
    
    private int unloadChunks() {
        MemoryGuardSettings settings = module.getSettings();
        int unloaded = 0;
        
        for (World world : Bukkit.getWorlds()) {
            if (settings.isChunkUnloadEnabled(world.getName())) {
                                List<org.bukkit.Chunk> playerChunks = new ArrayList<>();
                for (org.bukkit.entity.Player player : world.getPlayers()) {
                    org.bukkit.Chunk chunk = player.getLocation().getChunk();
                    playerChunks.add(chunk);
                    
                                        int viewDistance = Bukkit.getViewDistance();
                    for (int x = -viewDistance; x <= viewDistance; x++) {
                        for (int z = -viewDistance; z <= viewDistance; z++) {
                            playerChunks.add(world.getChunkAt(chunk.getX() + x, chunk.getZ() + z));
                        }
                    }
                }
                
                                for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                    if (!playerChunks.contains(chunk)) {
                        chunk.unload(true);
                        unloaded++;
                    }
                }
            }
        }
        
        return unloaded;
    }
    
    
    private void handleSettings(CommandSender sender, String[] args) {
        MemoryGuardSettings settings = module.getSettings();
        
        if (args.length < 2) {
            showSettingsList(sender);
            return;
        }
        
        String settingCommand = args[1].toLowerCase();
        
        switch (settingCommand) {
            case "list":
                showSettingsList(sender);
                break;
            case "enable":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Не указан параметр для включения.");
                    return;
                }
                enableSetting(sender, args[2]);
                break;
            case "disable":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Не указан параметр для выключения.");
                    return;
                }
                disableSetting(sender, args[2]);
                break;
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Не указан параметр или значение.");
                    return;
                }
                setSetting(sender, args[2], args[3]);
                break;
            case "add":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Не указан параметр или значение для добавления.");
                    return;
                }
                addSetting(sender, args[2], args[3]);
                break;
            case "remove":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Не указан параметр или значение для удаления.");
                    return;
                }
                removeSetting(sender, args[2], args[3]);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная команда настроек. Используйте /memoryguard settings list для справки.");
                break;
        }
    }
    
    
    private void showSettingsList(CommandSender sender) {
        MemoryGuardSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Настройки MemoryGuard ===");
        sender.sendMessage(ChatColor.YELLOW + "Порог уведомления: " + settings.getNoticeThreshold() + "%");
        sender.sendMessage(ChatColor.YELLOW + "Порог предупреждения: " + settings.getWarningThreshold() + "%");
        sender.sendMessage(ChatColor.YELLOW + "Критический порог: " + settings.getCriticalThreshold() + "%");
        sender.sendMessage(ChatColor.YELLOW + "Интервал проверки: " + settings.getCheckInterval() + " сек.");
        sender.sendMessage(ChatColor.YELLOW + "Макс. число кикаемых игроков: " + settings.getKickPlayersLimit());
        sender.sendMessage(ChatColor.YELLOW + "Подробное логирование: " + formatBoolean(settings.isEnableDetailedLogging()));
        sender.sendMessage(ChatColor.YELLOW + "Предупреждения игрокам: " + formatBoolean(settings.isEnablePlayerWarnings()));
        sender.sendMessage(ChatColor.YELLOW + "Кик игроков при нехватке памяти: " + formatBoolean(settings.isEnableKickOnCritical()));
        
                List<String> disabledItems = Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !settings.isItemsRemovalEnabled(name))
                .collect(Collectors.toList());
        
        List<String> disabledEntities = Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !settings.isEntityRemovalEnabled(name))
                .collect(Collectors.toList());
        
        List<String> disabledChunks = Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> !settings.isChunkUnloadEnabled(name))
                .collect(Collectors.toList());
        
        sender.sendMessage(ChatColor.YELLOW + "Миры с отключенной очисткой предметов: " + String.join(", ", disabledItems));
        sender.sendMessage(ChatColor.YELLOW + "Миры с отключенной очисткой сущностей: " + String.join(", ", disabledEntities));
        sender.sendMessage(ChatColor.YELLOW + "Миры с отключенной выгрузкой чанков: " + String.join(", ", disabledChunks));
        
                sender.sendMessage(ChatColor.YELLOW + "Удаляемые типы сущностей: " + 
                settings.getRemovableEntityTypes().stream()
                        .map(EntityType::name)
                        .collect(Collectors.joining(", ")));
    }
    
    
    private void enableSetting(CommandSender sender, String settingName) {
        MemoryGuardSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "detailedlogging":
                settings.setEnableDetailedLogging(true);
                break;
            case "playerwarnings":
                settings.setEnablePlayerWarnings(true);
                break;
            case "kickoncritical":
                settings.setEnableKickOnCritical(true);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " включен.");
    }
    
    
    private void disableSetting(CommandSender sender, String settingName) {
        MemoryGuardSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "detailedlogging":
                settings.setEnableDetailedLogging(false);
                break;
            case "playerwarnings":
                settings.setEnablePlayerWarnings(false);
                break;
            case "kickoncritical":
                settings.setEnableKickOnCritical(false);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " выключен.");
    }
    
    
    private void setSetting(CommandSender sender, String settingName, String value) {
        MemoryGuardSettings settings = module.getSettings();
        
        try {
            switch (settingName.toLowerCase()) {
                case "noticethreshold":
                    double noticeThreshold = Double.parseDouble(value);
                    if (noticeThreshold < 0 || noticeThreshold > 100) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть от 0 до 100.");
                        return;
                    }
                    settings.setNoticeThreshold(noticeThreshold);
                    break;
                case "warningthreshold":
                    double warningThreshold = Double.parseDouble(value);
                    if (warningThreshold < 0 || warningThreshold > 100) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть от 0 до 100.");
                        return;
                    }
                    settings.setWarningThreshold(warningThreshold);
                    break;
                case "criticalthreshold":
                    double criticalThreshold = Double.parseDouble(value);
                    if (criticalThreshold < 0 || criticalThreshold > 100) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть от 0 до 100.");
                        return;
                    }
                    settings.setCriticalThreshold(criticalThreshold);
                    break;
                case "checkinterval":
                    int checkInterval = Integer.parseInt(value);
                    if (checkInterval < 1) {
                        sender.sendMessage(ChatColor.RED + "Интервал должен быть больше 0.");
                        return;
                    }
                    settings.setCheckInterval(checkInterval);
                    break;
                case "kickplayerslimit":
                    int kickPlayersLimit = Integer.parseInt(value);
                    if (kickPlayersLimit < 0) {
                        sender.sendMessage(ChatColor.RED + "Лимит должен быть больше или равен 0.");
                        return;
                    }
                    settings.setKickPlayersLimit(kickPlayersLimit);
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                    return;
            }
            
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " установлен в " + value + ".");
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверное значение. Ожидается число.");
        }
    }
    
    
    private void addSetting(CommandSender sender, String settingName, String value) {
        MemoryGuardSettings settings = module.getSettings();
        
        if (settingName.equalsIgnoreCase("disabledworlditems")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setItemsRemovalEnabled(world.getName(), false);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " добавлен в список миров с отключенной очисткой предметов.");
        } else if (settingName.equalsIgnoreCase("disabledworldentities")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setEntityRemovalEnabled(world.getName(), false);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " добавлен в список миров с отключенной очисткой сущностей.");
        } else if (settingName.equalsIgnoreCase("disabledworldchunks")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setChunkUnloadEnabled(world.getName(), false);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " добавлен в список миров с отключенной выгрузкой чанков.");
        } else if (settingName.equalsIgnoreCase("removableentitytype")) {
            try {
                EntityType entityType = EntityType.valueOf(value.toUpperCase());
                settings.addRemovableEntityType(entityType);
                module.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Тип сущности " + entityType + " добавлен в список удаляемых.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Неизвестный тип сущности: " + value);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
        }
    }
    
    
    private void removeSetting(CommandSender sender, String settingName, String value) {
        MemoryGuardSettings settings = module.getSettings();
        
        if (settingName.equalsIgnoreCase("disabledworlditems")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setItemsRemovalEnabled(world.getName(), true);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " удален из списка миров с отключенной очисткой предметов.");
        } else if (settingName.equalsIgnoreCase("disabledworldentities")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setEntityRemovalEnabled(world.getName(), true);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " удален из списка миров с отключенной очисткой сущностей.");
        } else if (settingName.equalsIgnoreCase("disabledworldchunks")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + value + "' не найден.");
                return;
            }
            settings.setChunkUnloadEnabled(world.getName(), true);
            module.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Мир " + world.getName() + " удален из списка миров с отключенной выгрузкой чанков.");
        } else if (settingName.equalsIgnoreCase("removableentitytype")) {
            try {
                EntityType entityType = EntityType.valueOf(value.toUpperCase());
                settings.removeRemovableEntityType(entityType);
                module.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Тип сущности " + entityType + " удален из списка удаляемых.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Неизвестный тип сущности: " + value);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
        }
    }
    
    
    private String formatBoolean(boolean value) {
        return value ? ChatColor.GREEN + "Вкл" : ChatColor.RED + "Выкл";
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Справка по MemoryGuard ===");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard status" + ChatColor.WHITE + " - Показать текущий статус памяти");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard cleanup [тип]" + ChatColor.WHITE + " - Выполнить очистку (items, entities, chunks, gc, all)");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings list" + ChatColor.WHITE + " - Показать настройки");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings enable <параметр>" + ChatColor.WHITE + " - Включить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings disable <параметр>" + ChatColor.WHITE + " - Выключить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings set <параметр> <значение>" + ChatColor.WHITE + " - Установить значение");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings add <тип> <значение>" + ChatColor.WHITE + " - Добавить значение в список");
        sender.sendMessage(ChatColor.YELLOW + "/memoryguard settings remove <тип> <значение>" + ChatColor.WHITE + " - Удалить значение из списка");
    }
    
    
    private String[] getSettingsOptions() {
        return new String[] {
            "detailedLogging", "playerWarnings", "kickOnCritical"
        };
    }
    
    
    private String[] getNumericalSettingsOptions() {
        return new String[] {
            "noticeThreshold", "warningThreshold", "criticalThreshold", "checkInterval", "kickPlayersLimit"
        };
    }
} 