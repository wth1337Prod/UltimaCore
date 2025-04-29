package me.wth.ultimaCore.modules.lagshield;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class LagShieldCommand implements CommandExecutor, TabCompleter {
    private final LagShieldModule module;
    private final String[] subCommands = {"status", "cleanup", "memory", "chunks", "entities", "settings", "help"};
    private final String[] settingsCommands = {"enable", "disable", "set", "list", "add", "remove"};
    
    
    public LagShieldCommand(LagShieldModule module) {
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
            case "status":
                showStatus(sender);
                break;
            case "cleanup":
                performCleanup(sender);
                break;
            case "memory":
                showMemoryInfo(sender);
                break;
            case "chunks":
                showChunksInfo(sender, args);
                break;
            case "entities":
                showEntitiesInfo(sender, args);
                break;
            case "settings":
                handleSettings(sender, args);
                break;
            case "help":
                showHelp(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /lagshield help для справки.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterStartingWith(args[0], subCommands);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("settings")) {
                return filterStartingWith(args[1], settingsCommands);
            } else if (args[0].equalsIgnoreCase("chunks") || args[0].equalsIgnoreCase("entities")) {
                return filterStartingWith(args[1], Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settings")) {
                if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable")) {
                    return filterStartingWith(args[2], getSettingsOptions());
                } else if (args[1].equalsIgnoreCase("set")) {
                    return filterStartingWith(args[2], getNumericalSettingsOptions());
                } else if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                    if (args[2].equalsIgnoreCase("protectedEntity")) {
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
        TPSMonitor tpsMonitor = module.getTpsMonitor();
        MemoryMonitor memoryMonitor = module.getMemoryMonitor();
        
        sender.sendMessage(ChatColor.GREEN + "=== Статус сервера ===");
        sender.sendMessage(ChatColor.YELLOW + "TPS: " + tpsMonitor.getFormattedTPS());
        sender.sendMessage(ChatColor.YELLOW + "Использование памяти: " + memoryMonitor.getFormattedMemoryUsage());
        sender.sendMessage(ChatColor.YELLOW + "Загружено чанков: " + module.getChunkMonitor().getTotalLoadedChunkCount());
        sender.sendMessage(ChatColor.YELLOW + "Игроков онлайн: " + Bukkit.getOnlinePlayers().size());
        
        int totalEntities = 0;
        for (World world : Bukkit.getWorlds()) {
            totalEntities += world.getEntities().size();
        }
        sender.sendMessage(ChatColor.YELLOW + "Всего сущностей: " + totalEntities);
    }
    
    
    private void performCleanup(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Выполняется очистка...");
        module.performCleanup();
        sender.sendMessage(ChatColor.GREEN + "Очистка завершена!");
    }
    
    
    private void showMemoryInfo(CommandSender sender) {
        MemoryMonitor memoryMonitor = module.getMemoryMonitor();
        
        sender.sendMessage(ChatColor.GREEN + "=== Информация о памяти ===");
        sender.sendMessage(ChatColor.YELLOW + "Использование: " + memoryMonitor.getFormattedMemoryUsage());
        sender.sendMessage(ChatColor.YELLOW + "Выделено: " + (memoryMonitor.getTotalMemory() / (1024 * 1024)) + " МБ");
        sender.sendMessage(ChatColor.YELLOW + "Максимум: " + (memoryMonitor.getMaxMemory() / (1024 * 1024)) + " МБ");
        sender.sendMessage(ChatColor.YELLOW + "Свободно: " + (memoryMonitor.getFreeMemory() / (1024 * 1024)) + " МБ");
        sender.sendMessage(ChatColor.YELLOW + "Порог защиты: " + module.getSettings().getMemoryThreshold() + "%");
    }
    
    
    private void showChunksInfo(CommandSender sender, String[] args) {
        ChunkMonitor chunkMonitor = module.getChunkMonitor();
        
        sender.sendMessage(ChatColor.GREEN + "=== Информация о чанках ===");
        
        if (args.length > 1 && args[1].equalsIgnoreCase("player") && sender instanceof Player) {
            Player player = (Player) sender;
            Set<ChunkMonitor.ChunkCoord> chunks = chunkMonitor.getChunksAroundPlayer(player, 5);
            
            sender.sendMessage(ChatColor.YELLOW + "Вы находитесь в чанке: (" + 
                    player.getLocation().getChunk().getX() + ", " + 
                    player.getLocation().getChunk().getZ() + ")");
            sender.sendMessage(ChatColor.YELLOW + "Чанки вокруг вас: " + chunks.size());
        } else {
            String worldName = args.length > 1 ? args[1] : null;
            World world = worldName != null ? Bukkit.getWorld(worldName) : null;
            
            if (worldName != null && world == null) {
                sender.sendMessage(ChatColor.RED + "Мир с названием '" + worldName + "' не найден.");
                return;
            }
            
            if (world != null) {
                sender.sendMessage(ChatColor.YELLOW + "Загружено чанков в мире " + world.getName() + ": " + 
                        chunkMonitor.getLoadedChunkCount(world));
            } else {
                for (World w : Bukkit.getWorlds()) {
                    sender.sendMessage(ChatColor.YELLOW + "Мир " + w.getName() + ": " + 
                            chunkMonitor.getLoadedChunkCount(w) + " чанков");
                }
            }
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Всего загружено чанков: " + chunkMonitor.getTotalLoadedChunkCount());
    }
    
    
    private void showEntitiesInfo(CommandSender sender, String[] args) {
        EntityLimiter entityLimiter = module.getEntityLimiter();
        
        sender.sendMessage(ChatColor.GREEN + "=== Информация о сущностях ===");
        
        String worldName = args.length > 1 ? args[1] : null;
        World world = worldName != null ? Bukkit.getWorld(worldName) : null;
        
        if (worldName != null && world == null) {
            sender.sendMessage(ChatColor.RED + "Мир с названием '" + worldName + "' не найден.");
            return;
        }
        
        if (world != null) {
            Map<EntityType, Integer> counts = entityLimiter.countEntitiesByType(world);
            sender.sendMessage(ChatColor.YELLOW + "Сущности в мире " + world.getName() + ":");
            
            counts.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> sender.sendMessage(ChatColor.YELLOW + " - " + entry.getKey() + ": " + entry.getValue()));
            
            sender.sendMessage(ChatColor.YELLOW + "Всего: " + entityLimiter.countTotalEntities(world));
        } else {
            int total = 0;
            for (World w : Bukkit.getWorlds()) {
                int count = entityLimiter.countTotalEntities(w);
                sender.sendMessage(ChatColor.YELLOW + "Мир " + w.getName() + ": " + count + " сущностей");
                total += count;
            }
            sender.sendMessage(ChatColor.YELLOW + "Всего: " + total);
        }
    }
    
    
    private void handleSettings(CommandSender sender, String[] args) {
        LagShieldSettings settings = module.getSettings();
        
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
                sender.sendMessage(ChatColor.RED + "Неизвестная команда настроек. Используйте /lagshield settings list для справки.");
                break;
        }
    }
    
    
    private void showSettingsList(CommandSender sender) {
        LagShieldSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Настройки LagShield ===");
        sender.sendMessage(ChatColor.YELLOW + "Автоматическая очистка: " + formatBoolean(settings.isAutoCleanupEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Интервал очистки: " + settings.getCleanupInterval() + " мин.");
        sender.sendMessage(ChatColor.YELLOW + "Порог TPS для очистки: " + settings.getCleanupTpsThreshold());
        sender.sendMessage(ChatColor.YELLOW + "Очистка сущностей: " + formatBoolean(settings.isCleanEntitiesEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Выгрузка чанков: " + formatBoolean(settings.isUnloadChunksEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Время неактивности чанков: " + settings.getChunkUnloadTime() + " мин.");
        sender.sendMessage(ChatColor.YELLOW + "Максимум сущностей на чанк: " + settings.getMaxEntitiesPerChunk());
        sender.sendMessage(ChatColor.YELLOW + "Максимум тайл-сущностей на чанк: " + settings.getMaxTileEntitiesPerChunk());
        sender.sendMessage(ChatColor.YELLOW + "Лимит сущностей по типу: " + settings.getEntityLimitPerType());
        sender.sendMessage(ChatColor.YELLOW + "Защита памяти: " + formatBoolean(settings.isMemoryProtectionEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "Порог использования памяти: " + settings.getMemoryThreshold() + "%");
        sender.sendMessage(ChatColor.YELLOW + "Защита от автоферм: " + formatBoolean(settings.isAntifarmProtection()));
        sender.sendMessage(ChatColor.YELLOW + "Максимум падающих блоков: " + settings.getMaxFallingBlocks());
        sender.sendMessage(ChatColor.YELLOW + "Максимум взрывов TNT: " + settings.getMaxTntDetonations());
        
        sender.sendMessage(ChatColor.YELLOW + "Защищенные типы сущностей: " + 
                settings.getProtectedEntityTypes().stream()
                        .map(EntityType::name)
                        .collect(Collectors.joining(", ")));
    }
    
    
    private void enableSetting(CommandSender sender, String settingName) {
        LagShieldSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "autocleanup":
                settings.setAutoCleanupEnabled(true);
                break;
            case "cleanentities":
                settings.setCleanEntitiesEnabled(true);
                break;
            case "unloadchunks":
                settings.setUnloadChunksEnabled(true);
                break;
            case "memoryprotection":
                settings.setMemoryProtectionEnabled(true);
                break;
            case "antifarm":
                settings.setAntifarmProtection(true);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " включен.");
    }
    
    
    private void disableSetting(CommandSender sender, String settingName) {
        LagShieldSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "autocleanup":
                settings.setAutoCleanupEnabled(false);
                break;
            case "cleanentities":
                settings.setCleanEntitiesEnabled(false);
                break;
            case "unloadchunks":
                settings.setUnloadChunksEnabled(false);
                break;
            case "memoryprotection":
                settings.setMemoryProtectionEnabled(false);
                break;
            case "antifarm":
                settings.setAntifarmProtection(false);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " выключен.");
    }
    
    
    private void setSetting(CommandSender sender, String settingName, String value) {
        LagShieldSettings settings = module.getSettings();
        
        try {
            switch (settingName.toLowerCase()) {
                case "cleanupinterval":
                    int interval = Integer.parseInt(value);
                    if (interval < 1) {
                        sender.sendMessage(ChatColor.RED + "Интервал должен быть больше 0.");
                        return;
                    }
                    settings.setCleanupInterval(interval);
                    break;
                case "cleanuptpsthreshold":
                    double threshold = Double.parseDouble(value);
                    if (threshold < 0 || threshold > 20) {
                        sender.sendMessage(ChatColor.RED + "Порог TPS должен быть от 0 до 20.");
                        return;
                    }
                    settings.setCleanupTpsThreshold(threshold);
                    break;
                case "chunkunloadtime":
                    int time = Integer.parseInt(value);
                    if (time < 1) {
                        sender.sendMessage(ChatColor.RED + "Время должно быть больше 0.");
                        return;
                    }
                    settings.setChunkUnloadTime(time);
                    break;
                case "maxentitiesperchunk":
                    int maxEntities = Integer.parseInt(value);
                    if (maxEntities < 1) {
                        sender.sendMessage(ChatColor.RED + "Максимум должен быть больше 0.");
                        return;
                    }
                    settings.setMaxEntitiesPerChunk(maxEntities);
                    break;
                case "maxtileentitiesperchunk":
                    int maxTileEntities = Integer.parseInt(value);
                    if (maxTileEntities < 1) {
                        sender.sendMessage(ChatColor.RED + "Максимум должен быть больше 0.");
                        return;
                    }
                    settings.setMaxTileEntitiesPerChunk(maxTileEntities);
                    break;
                case "entitylimitpertype":
                    int limit = Integer.parseInt(value);
                    if (limit < 1) {
                        sender.sendMessage(ChatColor.RED + "Лимит должен быть больше 0.");
                        return;
                    }
                    settings.setEntityLimitPerType(limit);
                    break;
                case "memorythreshold":
                    int memThreshold = Integer.parseInt(value);
                    if (memThreshold < 50 || memThreshold > 99) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть от 50 до 99.");
                        return;
                    }
                    settings.setMemoryThreshold(memThreshold);
                    break;
                case "maxfallingblocks":
                    int maxFalling = Integer.parseInt(value);
                    if (maxFalling < 1) {
                        sender.sendMessage(ChatColor.RED + "Максимум должен быть больше 0.");
                        return;
                    }
                    settings.setMaxFallingBlocks(maxFalling);
                    break;
                case "maxtntdetonations":
                    int maxTnt = Integer.parseInt(value);
                    if (maxTnt < 1) {
                        sender.sendMessage(ChatColor.RED + "Максимум должен быть больше 0.");
                        return;
                    }
                    settings.setMaxTntDetonations(maxTnt);
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
        LagShieldSettings settings = module.getSettings();
        
        if (settingName.equalsIgnoreCase("protectedentity")) {
            try {
                EntityType entityType = EntityType.valueOf(value.toUpperCase());
                settings.addProtectedEntityType(entityType);
                module.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Тип сущности " + entityType + " добавлен в список защищенных.");
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Неизвестный тип сущности: " + value);
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
        }
    }
    
    
    private void removeSetting(CommandSender sender, String settingName, String value) {
        LagShieldSettings settings = module.getSettings();
        
        if (settingName.equalsIgnoreCase("protectedentity")) {
            try {
                EntityType entityType = EntityType.valueOf(value.toUpperCase());
                settings.removeProtectedEntityType(entityType);
                module.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Тип сущности " + entityType + " удален из списка защищенных.");
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
        sender.sendMessage(ChatColor.GREEN + "=== Справка по LagShield ===");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield status" + ChatColor.WHITE + " - Показать текущий статус сервера");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield cleanup" + ChatColor.WHITE + " - Выполнить очистку");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield memory" + ChatColor.WHITE + " - Показать информацию о памяти");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield chunks [мир|player]" + ChatColor.WHITE + " - Показать информацию о чанках");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield entities [мир]" + ChatColor.WHITE + " - Показать информацию о сущностях");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield settings list" + ChatColor.WHITE + " - Показать настройки");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield settings enable <параметр>" + ChatColor.WHITE + " - Включить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield settings disable <параметр>" + ChatColor.WHITE + " - Выключить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield settings set <параметр> <значение>" + ChatColor.WHITE + " - Установить значение");
        sender.sendMessage(ChatColor.YELLOW + "/lagshield settings add/remove protectedEntity <тип>" + ChatColor.WHITE + " - Управление защищенными типами");
    }
    
    
    private String[] getSettingsOptions() {
        return new String[] {
            "autoCleanup", "cleanEntities", "unloadChunks", "memoryProtection", "antifarm"
        };
    }
    
    
    private String[] getNumericalSettingsOptions() {
        return new String[] {
            "cleanupInterval", "cleanupTpsThreshold", "chunkUnloadTime", "maxEntitiesPerChunk",
            "maxTileEntitiesPerChunk", "entityLimitPerType", "memoryThreshold",
            "maxFallingBlocks", "maxTntDetonations"
        };
    }
} 