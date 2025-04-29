package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class StatusCommand extends BaseCommand {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    @Override
    public void execute(CommandSender sender, String[] args) {
                sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "UltimaCore - Статус сервера" + ChatColor.GOLD + " ===");
        
                double[] tps = PerformanceUtils.getRecentTPS();
        String tpsString = ChatColor.GREEN + df.format(tps[0]);
        if (tps[0] < 18.0) {
            tpsString = ChatColor.YELLOW + df.format(tps[0]);
        } else if (tps[0] < 10.0) {
            tpsString = ChatColor.RED + df.format(tps[0]);
        }
        
        sender.sendMessage(ChatColor.GOLD + "TPS: " + tpsString + ChatColor.GOLD + " (1м: " + ChatColor.YELLOW + df.format(tps[1]) + 
                ChatColor.GOLD + ", 5м: " + ChatColor.YELLOW + df.format(tps[2]) + ChatColor.GOLD + ")");
        
                Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        double memoryPercent = (double) usedMemory / maxMemory * 100;
        
        String memColor = ChatColor.GREEN.toString();
        if (memoryPercent > 70) {
            memColor = ChatColor.YELLOW.toString();
        } else if (memoryPercent > 90) {
            memColor = ChatColor.RED.toString();
        }
        
        sender.sendMessage(ChatColor.GOLD + "Память: " + memColor + usedMemory + "MB / " + maxMemory + "MB " + 
                "(" + df.format(memoryPercent) + "%)");
        
                long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
        long days = uptime / 86400;
        long hours = (uptime % 86400) / 3600;
        long minutes = (uptime % 3600) / 60;
        long seconds = uptime % 60;
        
        sender.sendMessage(ChatColor.GOLD + "Время работы: " + ChatColor.YELLOW + 
                (days > 0 ? days + "д " : "") + 
                (hours > 0 ? hours + "ч " : "") + 
                (minutes > 0 ? minutes + "м " : "") + 
                seconds + "с");
        
                int totalChunks = 0;
        int totalEntities = 0;
        
        for (World world : Bukkit.getWorlds()) {
            totalChunks += world.getLoadedChunks().length;
            totalEntities += world.getEntities().size();
        }
        
        sender.sendMessage(ChatColor.GOLD + "Миры: " + ChatColor.YELLOW + Bukkit.getWorlds().size() + 
                ChatColor.GOLD + ", Чанки: " + ChatColor.YELLOW + totalChunks + 
                ChatColor.GOLD + ", Сущности: " + ChatColor.YELLOW + totalEntities);
        
                sender.sendMessage(ChatColor.GOLD + "Игроки: " + ChatColor.YELLOW + Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        
                sender.sendMessage(ChatColor.GOLD + "Модули UltimaCore:");
        for (Module module : plugin.getModules().values()) {
            String status = module instanceof me.wth.ultimaCore.api.AbstractModule abstractModule && abstractModule.isEnabled() ? 
                    ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            sender.sendMessage(ChatColor.GOLD + " - " + status + " " + ChatColor.YELLOW + module.getName() + 
                    ChatColor.GRAY + ": " + module.getDescription());
        }
        
                if (args.length > 0) {
            if (args[0].equalsIgnoreCase("detailed")) {
                showDetailedStatus(sender);
            }
        }
    }
    
    
    private void showDetailedStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Детальная информация" + ChatColor.GOLD + " ===");
        
                double cpuUsage = PerformanceUtils.getCPUUsage();
        String cpuColor = ChatColor.GREEN.toString();
        if (cpuUsage > 60) {
            cpuColor = ChatColor.YELLOW.toString();
        } else if (cpuUsage > 85) {
            cpuColor = ChatColor.RED.toString();
        }
        
        sender.sendMessage(ChatColor.GOLD + "Загрузка CPU: " + cpuColor + df.format(cpuUsage) + "%");
        
                String jvmVersion = ManagementFactory.getRuntimeMXBean().getVmVersion();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        sender.sendMessage(ChatColor.GOLD + "JVM: " + ChatColor.YELLOW + jvmVersion + 
                ChatColor.GOLD + ", Процессоров: " + ChatColor.YELLOW + availableProcessors);
        
                long totalGCTime = PerformanceUtils.getTotalGCTime();
        sender.sendMessage(ChatColor.GOLD + "Время GC: " + ChatColor.YELLOW + totalGCTime + "мс");
        
                sender.sendMessage(ChatColor.GOLD + "Информация о мирах:");
        for (World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            int entities = world.getEntities().size();
            int players = world.getPlayers().size();
            
            sender.sendMessage(ChatColor.YELLOW + " - " + world.getName() + 
                    ChatColor.GOLD + " (тип: " + ChatColor.YELLOW + world.getEnvironment().name() + 
                    ChatColor.GOLD + ", чанки: " + ChatColor.YELLOW + chunks + 
                    ChatColor.GOLD + ", сущности: " + ChatColor.YELLOW + entities + 
                    ChatColor.GOLD + ", игроки: " + ChatColor.YELLOW + players + 
                    ChatColor.GOLD + ")");
        }
    }
    
    @Override
    public String getName() {
        return "status";
    }
    
    @Override
    public String getDescription() {
        return "Отображение текущего статуса сервера и плагина";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore status [detailed]";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.command.status";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"stat", "info"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        
        if (args.length == 1) {
            if ("detailed".startsWith(args[0].toLowerCase())) {
                result.add("detailed");
            }
        }
        
        return result;
    }
} 