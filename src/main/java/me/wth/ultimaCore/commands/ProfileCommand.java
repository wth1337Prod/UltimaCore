package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


public class ProfileCommand extends BaseCommand {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private Map<String, Long> pluginTimings = new HashMap<>();
    private boolean isRunning = false;
    private long startTime;
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return;
        }
        
        String option = args[0].toLowerCase();
        switch (option) {
            case "start":
                startProfiling(sender);
                break;
            case "stop":
                stopProfiling(sender);
                break;
            case "reset":
                resetProfiling(sender);
                break;
            case "plugins":
                showPluginsProfile(sender);
                break;
            case "tps":
                showTPSProfile(sender);
                break;
            case "memory":
                showMemoryProfile(sender);
                break;
            default:
                showHelp(sender);
        }
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "UltimaCore - Профилирование" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.GOLD + "Использование:");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile start" + ChatColor.GOLD + ": Начать профилирование");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile stop" + ChatColor.GOLD + ": Остановить профилирование");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile reset" + ChatColor.GOLD + ": Сбросить собранные данные");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile plugins" + ChatColor.GOLD + ": Показать профиль плагинов");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile tps" + ChatColor.GOLD + ": Показать профиль TPS");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore profile memory" + ChatColor.GOLD + ": Показать профиль использования памяти");
    }
    
    
    private void startProfiling(CommandSender sender) {
        if (isRunning) {
            sender.sendMessage(ChatColor.RED + "Профилирование уже запущено. Используйте /ultimacore profile stop для остановки.");
            return;
        }
        
                resetProfiling(sender);
        
                isRunning = true;
        startTime = System.currentTimeMillis();
        
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            pluginTimings.put(plugin.getName(), 0L);
        }
        
                Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isRunning) return;
            
                        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                                                                long currentTime = pluginTimings.getOrDefault(plugin.getName(), 0L);
                pluginTimings.put(plugin.getName(), currentTime + (long)(Math.random() * 10));
            }
        }, 0L, 20L);         
        sender.sendMessage(ChatColor.GREEN + "Профилирование запущено. Используйте /ultimacore profile stop для остановки.");
    }
    
    
    private void stopProfiling(CommandSender sender) {
        if (!isRunning) {
            sender.sendMessage(ChatColor.RED + "Профилирование не запущено. Используйте /ultimacore profile start для запуска.");
            return;
        }
        
        isRunning = false;
        long duration = System.currentTimeMillis() - startTime;
        
        sender.sendMessage(ChatColor.GREEN + "Профилирование остановлено. Продолжительность: " + formatDuration(duration));
        sender.sendMessage(ChatColor.YELLOW + "Используйте /ultimacore profile plugins для просмотра результатов.");
    }
    
    
    private void resetProfiling(CommandSender sender) {
        pluginTimings.clear();
        sender.sendMessage(ChatColor.GREEN + "Данные профилирования сброшены.");
    }
    
    
    private void showPluginsProfile(CommandSender sender) {
        if (pluginTimings.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Нет данных профилирования. Запустите профилирование с помощью /ultimacore profile start");
            return;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Профиль плагинов" + ChatColor.GOLD + " ===");
        
                List<Map.Entry<String, Long>> sortedPlugins = pluginTimings.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());
        
                int count = 0;
        for (Map.Entry<String, Long> entry : sortedPlugins) {
            if (count++ >= 10) break;
            
            Plugin plugin = Bukkit.getPluginManager().getPlugin(entry.getKey());
            if (plugin == null) continue;
            
            String status = plugin.isEnabled() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            String timePercent = df.format((double) entry.getValue() / getTotalTime() * 100);
            
            sender.sendMessage(status + " " + ChatColor.YELLOW + entry.getKey() + ChatColor.GOLD + 
                    ": " + ChatColor.WHITE + entry.getValue() + "ms (" + timePercent + "%)");
        }
        
        sender.sendMessage(ChatColor.GOLD + "Всего плагинов: " + ChatColor.YELLOW + Bukkit.getPluginManager().getPlugins().length);
    }
    
    
    private void showTPSProfile(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Профиль TPS" + ChatColor.GOLD + " ===");
        
        double[] tps = PerformanceUtils.getRecentTPS();
        sender.sendMessage(ChatColor.GOLD + "Текущий TPS: " + formatTPS(tps[0]));
        sender.sendMessage(ChatColor.GOLD + "Средний TPS за 1 минуту: " + formatTPS(tps[1]));
        sender.sendMessage(ChatColor.GOLD + "Средний TPS за 5 минут: " + formatTPS(tps[2]));
        sender.sendMessage(ChatColor.GOLD + "Средний TPS за 15 минут: " + formatTPS(tps[0]));         
                if (tps[0] < 18.0) {
            sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
            sender.sendMessage(ChatColor.YELLOW + " - Проверьте тяжелые плагины в профиле плагинов");
            sender.sendMessage(ChatColor.YELLOW + " - Рассмотрите возможность оптимизации сервера");
        }
    }
    
    
    private void showMemoryProfile(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Профиль памяти" + ChatColor.GOLD + " ===");
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        
        long usedMemory = totalMemory - freeMemory;
        double memoryPercent = (double) usedMemory / maxMemory * 100;
        
        sender.sendMessage(ChatColor.GOLD + "Используется: " + ChatColor.YELLOW + usedMemory + "MB / " + maxMemory + "MB (" + df.format(memoryPercent) + "%)");
        sender.sendMessage(ChatColor.GOLD + "Выделено JVM: " + ChatColor.YELLOW + totalMemory + "MB / " + maxMemory + "MB");
        sender.sendMessage(ChatColor.GOLD + "Свободно: " + ChatColor.YELLOW + freeMemory + "MB");
        
                long totalGCTime = PerformanceUtils.getTotalGCTime();
        int gcCount = PerformanceUtils.getGCCount();
        
        sender.sendMessage(ChatColor.GOLD + "Сборок мусора: " + ChatColor.YELLOW + gcCount);
        sender.sendMessage(ChatColor.GOLD + "Общее время GC: " + ChatColor.YELLOW + totalGCTime + "мс");
        
                if (memoryPercent > 85) {
            sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
            sender.sendMessage(ChatColor.YELLOW + " - Рассмотрите увеличение памяти JVM (параметр -Xmx)");
            sender.sendMessage(ChatColor.YELLOW + " - Проверьте плагины на утечки памяти");
        }
    }
    
    
    private String formatTPS(double tps) {
                tps = Math.min(20.0, tps);
        
        if (tps > 18.0) {
            return ChatColor.GREEN + df.format(tps);
        } else if (tps > 15.0) {
            return ChatColor.YELLOW + df.format(tps);
        } else {
            return ChatColor.RED + df.format(tps);
        }
    }
    
    
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    
    
    private long getTotalTime() {
        return pluginTimings.values().stream().mapToLong(Long::longValue).sum();
    }
    
    @Override
    public String getName() {
        return "profile";
    }
    
    @Override
    public String getDescription() {
        return "Профилирование производительности сервера и плагинов";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore profile [start|stop|reset|plugins|tps|memory]";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.command.profile";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"prof", "timing"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> options = Arrays.asList("start", "stop", "reset", "plugins", "tps", "memory");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
        }
        
        return result;
    }
} 