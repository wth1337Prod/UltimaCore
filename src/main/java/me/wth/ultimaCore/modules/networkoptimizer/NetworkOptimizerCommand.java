package me.wth.ultimaCore.modules.networkoptimizer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class NetworkOptimizerCommand implements CommandExecutor, TabCompleter {
    private final NetworkOptimizerModule module;
    private final String[] subCommands = {"status", "optimize", "settings", "help", "reload"};
    private final String[] settingsCommands = {"enable", "disable", "set", "list"};
    
    
    public NetworkOptimizerCommand(NetworkOptimizerModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.networkoptimizer.admin")) {
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
                showStatus(sender, args);
                break;
            case "optimize":
                optimizePlayer(sender, args);
                break;
            case "settings":
                handleSettings(sender, args);
                break;
            case "help":
                showHelp(sender);
                break;
            case "reload":
                reloadModule(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /netoptimizer help для справки.");
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.networkoptimizer.admin")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            return filterStartingWith(args[0], subCommands);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("settings")) {
                return filterStartingWith(args[1], settingsCommands);
            } else if (args[0].equalsIgnoreCase("optimize")) {
                return getOnlinePlayerNames(args[1]);
            } else if (args[0].equalsIgnoreCase("status")) {
                return getOnlinePlayerNames(args[1]);
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settings")) {
                if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable")) {
                    return filterStartingWith(args[2], getSettingsOptions());
                } else if (args[1].equalsIgnoreCase("set")) {
                    return filterStartingWith(args[2], getNumericalSettingsOptions());
                }
            }
        }
        
        return Collections.emptyList();
    }
    
    
    private List<String> getOnlinePlayerNames(String prefix) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    
    private List<String> filterStartingWith(String prefix, String[] options) {
        return filterStartingWith(prefix, Arrays.asList(options));
    }
    
    
    private List<String> filterStartingWith(String prefix, List<String> options) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    
    private void showStatus(CommandSender sender, String[] args) {
        if (args.length > 1) {
                        String playerName = args[1];
            Player target = Bukkit.getPlayer(playerName);
            
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок " + playerName + " не найден.");
                return;
            }
            
            showPlayerStatus(sender, target);
        } else {
                        showServerStatus(sender);
        }
    }
    
    
    private void showPlayerStatus(CommandSender sender, Player player) {
        NetworkOptimizerSettings settings = module.getSettings();
        int ping = player.getPing();
        
        sender.sendMessage(ChatColor.GREEN + "=== Сетевой статус игрока " + player.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Текущий пинг: " + ping + " мс");
        
                String pingLevel;
        if (ping < settings.getMediumLatencyThreshold()) {
            pingLevel = ChatColor.GREEN + "Низкий" + ChatColor.YELLOW;
        } else if (ping < settings.getHighLatencyThreshold()) {
            pingLevel = ChatColor.GOLD + "Средний" + ChatColor.YELLOW;
        } else {
            pingLevel = ChatColor.RED + "Высокий" + ChatColor.YELLOW;
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Уровень задержки: " + pingLevel);
        
                boolean hasOptimizations = player.hasPermission("ultimacore.networkoptimizer.bypass");
        
        if (hasOptimizations) {
            sender.sendMessage(ChatColor.YELLOW + "Оптимизации: " + ChatColor.RED + "Отключены (bypass)");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Оптимизации: " + ChatColor.GREEN + "Включены");
            
                        if (ping > settings.getHighLatencyThreshold()) {
                sender.sendMessage(ChatColor.RED + "Рекомендуется снизить дистанцию прорисовки или графические настройки.");
            }
        }
    }
    
    
    private void showServerStatus(CommandSender sender) {
        NetworkOptimizerSettings settings = module.getSettings();
        int playerCount = Bukkit.getOnlinePlayers().size();
        
        sender.sendMessage(ChatColor.GREEN + "=== Статус сети сервера ===");
        sender.sendMessage(ChatColor.YELLOW + "Всего игроков: " + playerCount);
        
                int highLatencyPlayers = 0;
        int mediumLatencyPlayers = 0;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            int ping = player.getPing();
            
            if (ping > settings.getHighLatencyThreshold()) {
                highLatencyPlayers++;
            } else if (ping > settings.getMediumLatencyThreshold()) {
                mediumLatencyPlayers++;
            }
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Игроков с высоким пингом: " + ChatColor.RED + highLatencyPlayers);
        sender.sendMessage(ChatColor.YELLOW + "Игроков со средним пингом: " + ChatColor.GOLD + mediumLatencyPlayers);
        
                sender.sendMessage(ChatColor.YELLOW + "Оптимизация видимости сущностей: " + 
                formatBoolean(settings.isEnabledEntityVisibilityOptimization()));
        sender.sendMessage(ChatColor.YELLOW + "Оптимизация обновления чанков: " + 
                formatBoolean(settings.isEnabledChunkUpdateOptimization()));
        sender.sendMessage(ChatColor.YELLOW + "Сжатие пакетов: " + 
                formatBoolean(settings.isEnablePacketCompression()));
    }
    
    
    private void optimizePlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Укажите имя игрока.");
            return;
        }
        
        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);
        
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок " + playerName + " не найден.");
            return;
        }
        
                sender.sendMessage(ChatColor.YELLOW + "Применение оптимизаций для игрока " + target.getName() + "...");
        
                        PlayerNetworkStats stats = new PlayerNetworkStats();
        stats.update(target);
        
                try {
            java.lang.reflect.Method method = module.getClass().getDeclaredMethod("optimizePlayerNetwork", Player.class, PlayerNetworkStats.class);
            method.setAccessible(true);
            method.invoke(module, target, stats);
            
            sender.sendMessage(ChatColor.GREEN + "Оптимизации успешно применены!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при применении оптимизаций: " + e.getMessage());
        }
    }
    
    
    private void handleSettings(CommandSender sender, String[] args) {
        NetworkOptimizerSettings settings = module.getSettings();
        
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
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная команда настроек. Используйте /netoptimizer settings list для справки.");
                break;
        }
    }
    
    
    private void showSettingsList(CommandSender sender) {
        NetworkOptimizerSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Настройки NetworkOptimizer ===");
        sender.sendMessage(ChatColor.YELLOW + "Пороги задержки:");
        sender.sendMessage(ChatColor.YELLOW + "  Средний пинг: " + settings.getMediumLatencyThreshold() + " мс");
        sender.sendMessage(ChatColor.YELLOW + "  Высокий пинг: " + settings.getHighLatencyThreshold() + " мс");
        
        sender.sendMessage(ChatColor.YELLOW + "Настройки мониторинга:");
        sender.sendMessage(ChatColor.YELLOW + "  Интервал проверки: " + settings.getMonitoringInterval() + " сек");
        
        sender.sendMessage(ChatColor.YELLOW + "Настройки оптимизации сущностей:");
        sender.sendMessage(ChatColor.YELLOW + "  Включено: " + formatBoolean(settings.isEnabledEntityVisibilityOptimization()));
        sender.sendMessage(ChatColor.YELLOW + "  Нормальная дистанция: " + settings.getNormalEntityViewDistance() + " блоков");
        sender.sendMessage(ChatColor.YELLOW + "  Сниженная дистанция: " + settings.getReducedEntityViewDistance() + " блоков");
        
        sender.sendMessage(ChatColor.YELLOW + "Настройки оптимизации чанков:");
        sender.sendMessage(ChatColor.YELLOW + "  Включено: " + formatBoolean(settings.isEnabledChunkUpdateOptimization()));
        sender.sendMessage(ChatColor.YELLOW + "  Нормальная частота: каждые " + settings.getNormalChunkUpdateRate() + " тиков");
        sender.sendMessage(ChatColor.YELLOW + "  Сниженная частота: каждые " + settings.getReducedChunkUpdateRate() + " тиков");
        
        sender.sendMessage(ChatColor.YELLOW + "Настройки компрессии пакетов:");
        sender.sendMessage(ChatColor.YELLOW + "  Включено: " + formatBoolean(settings.isEnablePacketCompression()));
        sender.sendMessage(ChatColor.YELLOW + "  Порог сжатия: " + settings.getCompressionThreshold() + " байт");
        
        sender.sendMessage(ChatColor.YELLOW + "Прочие настройки:");
        sender.sendMessage(ChatColor.YELLOW + "  Подробное логирование: " + formatBoolean(settings.isEnableDetailedLogging()));
        sender.sendMessage(ChatColor.YELLOW + "  Оптимизация при входе: " + formatBoolean(settings.isEnableOptimizationOnJoin()));
    }
    
    
    private void enableSetting(CommandSender sender, String settingName) {
        NetworkOptimizerSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "entityvisibility":
                settings.setEnabledEntityVisibilityOptimization(true);
                break;
            case "chunkupdate":
                settings.setEnabledChunkUpdateOptimization(true);
                break;
            case "packetcompression":
                settings.setEnablePacketCompression(true);
                break;
            case "detailedlogging":
                settings.setEnableDetailedLogging(true);
                break;
            case "optimizeonjoin":
                settings.setEnableOptimizationOnJoin(true);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " включен.");
    }
    
    
    private void disableSetting(CommandSender sender, String settingName) {
        NetworkOptimizerSettings settings = module.getSettings();
        
        switch (settingName.toLowerCase()) {
            case "entityvisibility":
                settings.setEnabledEntityVisibilityOptimization(false);
                break;
            case "chunkupdate":
                settings.setEnabledChunkUpdateOptimization(false);
                break;
            case "packetcompression":
                settings.setEnablePacketCompression(false);
                break;
            case "detailedlogging":
                settings.setEnableDetailedLogging(false);
                break;
            case "optimizeonjoin":
                settings.setEnableOptimizationOnJoin(false);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + settingName);
                return;
        }
        
        module.saveConfig();
        sender.sendMessage(ChatColor.GREEN + "Параметр " + settingName + " выключен.");
    }
    
    
    private void setSetting(CommandSender sender, String settingName, String value) {
        NetworkOptimizerSettings settings = module.getSettings();
        
        try {
            switch (settingName.toLowerCase()) {
                case "mediumlatencythreshold":
                    int mediumThreshold = Integer.parseInt(value);
                    if (mediumThreshold < 0) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть больше 0.");
                        return;
                    }
                    settings.setMediumLatencyThreshold(mediumThreshold);
                    break;
                case "highlatencythreshold":
                    int highThreshold = Integer.parseInt(value);
                    if (highThreshold < 0) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть больше 0.");
                        return;
                    }
                    settings.setHighLatencyThreshold(highThreshold);
                    break;
                case "monitoringinterval":
                    int interval = Integer.parseInt(value);
                    if (interval < 1) {
                        sender.sendMessage(ChatColor.RED + "Интервал должен быть больше 0.");
                        return;
                    }
                    settings.setMonitoringInterval(interval);
                    break;
                case "normalentityviewdistance":
                    int normalDist = Integer.parseInt(value);
                    if (normalDist < 16) {
                        sender.sendMessage(ChatColor.RED + "Дистанция должна быть не менее 16 блоков.");
                        return;
                    }
                    settings.setNormalEntityViewDistance(normalDist);
                    break;
                case "reducedentityviewdistance":
                    int reducedDist = Integer.parseInt(value);
                    if (reducedDist < 16) {
                        sender.sendMessage(ChatColor.RED + "Дистанция должна быть не менее 16 блоков.");
                        return;
                    }
                    settings.setReducedEntityViewDistance(reducedDist);
                    break;
                case "normalchunkupdaterate":
                    int normalRate = Integer.parseInt(value);
                    if (normalRate < 1) {
                        sender.sendMessage(ChatColor.RED + "Частота должна быть не менее 1 тика.");
                        return;
                    }
                    settings.setNormalChunkUpdateRate(normalRate);
                    break;
                case "reducedchunkupdaterate":
                    int reducedRate = Integer.parseInt(value);
                    if (reducedRate < 1) {
                        sender.sendMessage(ChatColor.RED + "Частота должна быть не менее 1 тика.");
                        return;
                    }
                    settings.setReducedChunkUpdateRate(reducedRate);
                    break;
                case "compressionthreshold":
                    int threshold = Integer.parseInt(value);
                    if (threshold < 0) {
                        sender.sendMessage(ChatColor.RED + "Порог должен быть больше или равен 0.");
                        return;
                    }
                    settings.setCompressionThreshold(threshold);
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
    
    
    private void reloadModule(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Перезагрузка модуля NetworkOptimizer...");
        module.reload();
        sender.sendMessage(ChatColor.GREEN + "Модуль успешно перезагружен!");
    }
    
    
    private String formatBoolean(boolean value) {
        return value ? ChatColor.GREEN + "Вкл" : ChatColor.RED + "Выкл";
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Справка по NetworkOptimizer ===");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer status [игрок]" + ChatColor.WHITE + " - Показать статус сети [игрока]");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer optimize <игрок>" + ChatColor.WHITE + " - Оптимизировать сеть для игрока");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer settings list" + ChatColor.WHITE + " - Показать настройки");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer settings enable <параметр>" + ChatColor.WHITE + " - Включить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer settings disable <параметр>" + ChatColor.WHITE + " - Выключить параметр");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer settings set <параметр> <значение>" + ChatColor.WHITE + " - Установить значение");
        sender.sendMessage(ChatColor.YELLOW + "/netoptimizer reload" + ChatColor.WHITE + " - Перезагрузить модуль");
    }
    
    
    private String[] getSettingsOptions() {
        return new String[] {
            "entityVisibility", "chunkUpdate", "packetCompression", "detailedLogging", "optimizeOnJoin"
        };
    }
    
    
    private String[] getNumericalSettingsOptions() {
        return new String[] {
            "mediumLatencyThreshold", "highLatencyThreshold", "monitoringInterval",
            "normalEntityViewDistance", "reducedEntityViewDistance",
            "normalChunkUpdateRate", "reducedChunkUpdateRate", "compressionThreshold"
        };
    }
} 