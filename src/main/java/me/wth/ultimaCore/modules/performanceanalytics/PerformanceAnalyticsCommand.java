package me.wth.ultimaCore.modules.performanceanalytics;

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


public class PerformanceAnalyticsCommand implements CommandExecutor, TabCompleter {
    private final PerformanceAnalyticsModule module;
    private final List<String> subCommands = Arrays.asList(
            "report", "history", "settings", "reset", "help"
    );
    
    
    public PerformanceAnalyticsCommand(PerformanceAnalyticsModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.performance")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
                        module.showPerformanceReport(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "report":
                                module.showPerformanceReport(sender);
                return true;
                
            case "history":
                                showPerformanceHistory(sender);
                return true;
                
            case "settings":
                                handleSettingsCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
                
            case "reset":
                                sender.sendMessage(ChatColor.GREEN + "Статистика производительности сброшена.");
                return true;
                
            case "help":
                                showHelp(sender);
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда: " + subCommand);
                sender.sendMessage(ChatColor.RED + "Используйте /performance help для получения справки.");
                return true;
        }
    }
    
    
    private void handleSettingsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimacore.performance.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для изменения настроек!");
            return;
        }
        
        PerformanceAnalyticsSettings settings = module.getSettings();
        
        if (args.length == 0) {
                        showSettings(sender, settings);
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Неверное количество аргументов!");
            sender.sendMessage(ChatColor.RED + "Использование: /performance settings <параметр> <значение>");
            return;
        }
        
        String param = args[0].toLowerCase();
        String value = args[1];
        
        try {
            switch (param) {
                case "memorycheckinterval":
                    settings.setMemoryCheckInterval(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Интервал проверки памяти установлен на " + value + " секунд.");
                    break;
                    
                case "cpucheckinterval":
                    settings.setCpuCheckInterval(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Интервал проверки CPU установлен на " + value + " секунд.");
                    break;
                    
                case "plugincheckinterval":
                    settings.setPluginCheckInterval(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Интервал проверки плагинов установлен на " + value + " секунд.");
                    break;
                    
                case "tpswarningthreshold":
                    settings.setTpsWarningThreshold(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Порог TPS для предупреждений установлен на " + value);
                    break;
                    
                case "memorywarningthreshold":
                    settings.setMemoryWarningThreshold(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Порог использования памяти для предупреждений установлен на " + value + "%");
                    break;
                    
                case "cpuwarningthreshold":
                    settings.setCpuWarningThreshold(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Порог использования CPU для предупреждений установлен на " + value + "%");
                    break;
                    
                case "enabletpswarnings":
                    settings.setEnableTpsWarnings(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Предупреждения о низком TPS " + 
                            (settings.isEnableTpsWarnings() ? "включены" : "выключены"));
                    break;
                    
                case "enablememorywarnings":
                    settings.setEnableMemoryWarnings(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Предупреждения о памяти " + 
                            (settings.isEnableMemoryWarnings() ? "включены" : "выключены"));
                    break;
                    
                case "enablecpuwarnings":
                    settings.setEnableCpuWarnings(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Предупреждения о CPU " + 
                            (settings.isEnableCpuWarnings() ? "включены" : "выключены"));
                    break;
                    
                case "broadcastwarnings":
                    settings.setBroadcastWarnings(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Отправка предупреждений админам " + 
                            (settings.isBroadcastWarnings() ? "включена" : "выключена"));
                    break;
                    
                case "enablepluginmonitoring":
                    settings.setEnablePluginMonitoring(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Мониторинг плагинов " + 
                            (settings.isEnablePluginMonitoring() ? "включен" : "выключен"));
                    break;
                    
                case "enablehistorytracking":
                    settings.setEnableHistoryTracking(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Отслеживание истории " + 
                            (settings.isEnableHistoryTracking() ? "включено" : "выключено"));
                    break;
                    
                case "savedataondisable":
                    settings.setSaveDataOnDisable(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Сохранение данных при выключении " + 
                            (settings.isSaveDataOnDisable() ? "включено" : "выключено"));
                    break;
                    
                case "historysize":
                    settings.setHistorySize(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Размер истории производительности установлен на " + value + " записей.");
                    break;
                    
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + param);
                    return;
            }
            
                        module.saveConfig();
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверный формат значения: " + value);
            LoggerUtil.warning("Ошибка при изменении настроек PerformanceAnalytics: " + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Произошла ошибка при изменении настроек: " + e.getMessage());
            LoggerUtil.warning("Ошибка при изменении настроек PerformanceAnalytics", e);
        }
    }
    
    
    private void showSettings(CommandSender sender, PerformanceAnalyticsSettings settings) {
        sender.sendMessage(ChatColor.GREEN + "===== Настройки PerformanceAnalytics =====");
        
        sender.sendMessage(ChatColor.GOLD + "Интервалы проверок:");
        sender.sendMessage(ChatColor.YELLOW + "  Память: " + ChatColor.WHITE + settings.getMemoryCheckInterval() + " сек");
        sender.sendMessage(ChatColor.YELLOW + "  CPU: " + ChatColor.WHITE + settings.getCpuCheckInterval() + " сек");
        sender.sendMessage(ChatColor.YELLOW + "  Плагины: " + ChatColor.WHITE + settings.getPluginCheckInterval() + " сек");
        
        sender.sendMessage(ChatColor.GOLD + "Пороговые значения:");
        sender.sendMessage(ChatColor.YELLOW + "  TPS: " + ChatColor.WHITE + settings.getTpsWarningThreshold());
        sender.sendMessage(ChatColor.YELLOW + "  Память: " + ChatColor.WHITE + settings.getMemoryWarningThreshold() + "%");
        sender.sendMessage(ChatColor.YELLOW + "  CPU: " + ChatColor.WHITE + settings.getCpuWarningThreshold() + "%");
        
        sender.sendMessage(ChatColor.GOLD + "Предупреждения:");
        sender.sendMessage(ChatColor.YELLOW + "  TPS: " + getBooleanColor(settings.isEnableTpsWarnings()));
        sender.sendMessage(ChatColor.YELLOW + "  Память: " + getBooleanColor(settings.isEnableMemoryWarnings()));
        sender.sendMessage(ChatColor.YELLOW + "  CPU: " + getBooleanColor(settings.isEnableCpuWarnings()));
        sender.sendMessage(ChatColor.YELLOW + "  Отправка админам: " + getBooleanColor(settings.isBroadcastWarnings()));
        
        sender.sendMessage(ChatColor.GOLD + "Общие настройки:");
        sender.sendMessage(ChatColor.YELLOW + "  Мониторинг плагинов: " + getBooleanColor(settings.isEnablePluginMonitoring()));
        sender.sendMessage(ChatColor.YELLOW + "  Отслеживание истории: " + getBooleanColor(settings.isEnableHistoryTracking()));
        sender.sendMessage(ChatColor.YELLOW + "  Сохранение данных: " + getBooleanColor(settings.isSaveDataOnDisable()));
        sender.sendMessage(ChatColor.YELLOW + "  Размер истории: " + ChatColor.WHITE + settings.getHistorySize() + " записей");
        
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    
    private String getBooleanColor(boolean value) {
        return (value ? ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено");
    }
    
    
    private void showPerformanceHistory(CommandSender sender) {
        List<PerformanceAnalyticsModule.PerformanceSnapshot> history = module.getHistory();
        
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "История производительности пуста!");
            return;
        }
        
        sender.sendMessage(ChatColor.GREEN + "===== История производительности =====");
        sender.sendMessage(ChatColor.GRAY + "Показаны последние " + Math.min(5, history.size()) + " записей:");
        
                List<PerformanceAnalyticsModule.PerformanceSnapshot> lastEntries = new ArrayList<>(history);
        int startIndex = Math.max(0, lastEntries.size() - 5);
        
        for (int i = startIndex; i < lastEntries.size(); i++) {
            PerformanceAnalyticsModule.PerformanceSnapshot snapshot = lastEntries.get(i);
            
                        String timeAgo = formatTimeAgo(System.currentTimeMillis() - snapshot.timestamp);
            
            sender.sendMessage(ChatColor.GOLD + "Запись " + (i + 1) + " (" + timeAgo + "):");
            sender.sendMessage(ChatColor.YELLOW + "  TPS: " + formatValue(snapshot.tps, 18.0, 15.0));
            sender.sendMessage(ChatColor.YELLOW + "  Память: " + formatPercentage(snapshot.memoryUsage, 70.0, 85.0));
            sender.sendMessage(ChatColor.YELLOW + "  CPU: " + formatPercentage(snapshot.cpuUsage, 50.0, 75.0));
            sender.sendMessage(ChatColor.YELLOW + "  Игроки: " + ChatColor.WHITE + snapshot.playersOnline);
            sender.sendMessage(ChatColor.YELLOW + "  Чанки: " + ChatColor.WHITE + snapshot.chunksLoaded);
            sender.sendMessage(ChatColor.YELLOW + "  Сущности: " + ChatColor.WHITE + snapshot.entitiesCount);
        }
        
        sender.sendMessage(ChatColor.GREEN + "=========================================");
    }
    
    
    private String formatTimeAgo(long millis) {
        long seconds = millis / 1000;
        
        if (seconds < 60) {
            return seconds + " сек назад";
        } else if (seconds < 3600) {
            return (seconds / 60) + " мин назад";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " час назад";
        } else {
            return (seconds / 86400) + " дн назад";
        }
    }
    
    
    private String formatValue(double value, double goodThreshold, double warningThreshold) {
        String color;
        
        if (value >= goodThreshold) {
            color = ChatColor.GREEN.toString();
        } else if (value >= warningThreshold) {
            color = ChatColor.YELLOW.toString();
        } else {
            color = ChatColor.RED.toString();
        }
        
        return color + String.format("%.2f", value);
    }
    
    
    private String formatPercentage(double value, double goodThreshold, double warningThreshold) {
        String color;
        
        if (value <= goodThreshold) {
            color = ChatColor.GREEN.toString();
        } else if (value <= warningThreshold) {
            color = ChatColor.YELLOW.toString();
        } else {
            color = ChatColor.RED.toString();
        }
        
        return color + String.format("%.2f", value) + "%";
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== Справка по команде /performance =====");
        sender.sendMessage(ChatColor.GOLD + "/performance" + ChatColor.WHITE + " - Показать отчет о производительности");
        sender.sendMessage(ChatColor.GOLD + "/performance report" + ChatColor.WHITE + " - Показать отчет о производительности");
        sender.sendMessage(ChatColor.GOLD + "/performance history" + ChatColor.WHITE + " - Показать историю производительности");
        sender.sendMessage(ChatColor.GOLD + "/performance settings" + ChatColor.WHITE + " - Показать текущие настройки");
        sender.sendMessage(ChatColor.GOLD + "/performance settings <параметр> <значение>" + ChatColor.WHITE + " - Изменить настройку");
        sender.sendMessage(ChatColor.GOLD + "/performance reset" + ChatColor.WHITE + " - Сбросить статистику");
        sender.sendMessage(ChatColor.GOLD + "/performance help" + ChatColor.WHITE + " - Показать эту справку");
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.performance")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            List<String> settingsParams = Arrays.asList(
                    "memoryCheckInterval", "cpuCheckInterval", "pluginCheckInterval",
                    "tpsWarningThreshold", "memoryWarningThreshold", "cpuWarningThreshold",
                    "enableTpsWarnings", "enableMemoryWarnings", "enableCpuWarnings",
                    "broadcastWarnings", "enablePluginMonitoring", "enableHistoryTracking",
                    "saveDataOnDisable", "historySize"
            );
            
            return settingsParams.stream()
                    .filter(param -> param.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            String param = args[1].toLowerCase();
            
                        if (param.contains("enable") || param.contains("broadcast") || param.contains("save")) {
                return Arrays.asList("true", "false").stream()
                        .filter(val -> val.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 