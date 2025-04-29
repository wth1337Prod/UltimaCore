package me.wth.ultimaCore.gui;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.InventoryUtils;
import me.wth.ultimaCore.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ServerStatusGUI extends AbstractGUI {
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private int taskId = -1;
    
    
    public ServerStatusGUI(UltimaCore plugin) {
        super(plugin, "&8⚡ &bСостояние сервера", 54);
    }
    
    @Override
    public Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);

        fillBorders(inventory, 11);

        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        long hours = TimeUnit.MILLISECONDS.toHours(uptime) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime) % 60;
        
        String uptimeStr = String.format("%d дн. %02d:%02d:%02d", days, hours, minutes, seconds);
        
        inventory.setItem(4, InventoryUtils.createItem(Material.CLOCK, 
                "&e&lВремя работы сервера", 
                "&7" + uptimeStr, 
                "&7Текущее время: &f" + timeFormat.format(new Date())));

        double[] tps = PerformanceUtils.getRecentTPS();
        List<String> tpsLore = new ArrayList<>();
        tpsLore.add("");
        
        String[] tpsNames = new String[]{"&71 минута: ", "&75 минут: ", "&715 минут: "};
        for (int i = 0; i < tps.length; i++) {
            double currentTps = Math.min(20.0, tps[i]);
            String color;
            
            if (currentTps >= 18.0) {
                color = "&a";
            } else if (currentTps >= 15.0) {
                color = "&e";
            } else {
                color = "&c";
            }
            
            tpsLore.add(tpsNames[i] + color + String.format("%.2f", currentTps));
        }
        
        tpsLore.add("");
        tpsLore.add("&7Состояние:");
        
        double averageTps = (tps[0] + tps[1] + tps[2]) / 3.0;
        if (averageTps >= 19.5) {
            tpsLore.add("&a■ Отличное");
        } else if (averageTps >= 18.0) {
            tpsLore.add("&a■ Хорошее");
        } else if (averageTps >= 15.0) {
            tpsLore.add("&e■ Нормальное");
        } else if (averageTps >= 10.0) {
            tpsLore.add("&6■ Пониженное");
        } else {
            tpsLore.add("&c■ Критическое");
        }
        
        Material tpsMaterial = (averageTps >= 18.0) ? Material.LIME_CONCRETE : 
                             (averageTps >= 15.0) ? Material.YELLOW_CONCRETE : 
                             Material.RED_CONCRETE;
        
        inventory.setItem(20, InventoryUtils.createItem(tpsMaterial, "&b&lTPS сервера", tpsLore));

        double cpuUsage = PerformanceUtils.getCPUUsage();
        String cpuBar = InventoryUtils.createProgressBar(cpuUsage, 100.0, 20, '■', '□');
        
        String cpuColor;
        if (cpuUsage < 30.0) {
            cpuColor = "&a";
        } else if (cpuUsage < 70.0) {
            cpuColor = "&e";
        } else {
            cpuColor = "&c";
        }
        
        inventory.setItem(22, InventoryUtils.createItem(Material.FURNACE, 
                "&b&lИспользование CPU", 
                "",
                cpuColor + String.format("%.1f%%", cpuUsage),
                "&7" + cpuBar,
                "",
                "&7Доступно ядер: &f" + Runtime.getRuntime().availableProcessors()));

        long maxMemory = Runtime.getRuntime().maxMemory() / 1048576L;
        long totalMemory = Runtime.getRuntime().totalMemory() / 1048576L;
        long freeMemory = Runtime.getRuntime().freeMemory() / 1048576L;
        long usedMemory = totalMemory - freeMemory;
        
        double memoryPercentage = (double) usedMemory / maxMemory * 100.0;
        String memoryBar = InventoryUtils.createProgressBar(usedMemory, maxMemory, 20, '■', '□');
        
        String memoryColor;
        if (memoryPercentage < 70.0) {
            memoryColor = "&a";
        } else if (memoryPercentage < 85.0) {
            memoryColor = "&e";
        } else {
            memoryColor = "&c";
        }
        
        inventory.setItem(24, InventoryUtils.createItem(Material.HOPPER, 
                "&b&lИспользование памяти", 
                "",
                memoryColor + String.format("%d MB / %d MB (%.1f%%)", usedMemory, maxMemory, memoryPercentage),
                "&7" + memoryBar,
                "",
                "&7Выделено: &f" + totalMemory + " MB",
                "&7Свободно: &f" + freeMemory + " MB",
                "&7Всего доступно: &f" + maxMemory + " MB"));

        long gcTime = PerformanceUtils.getTotalGCTime() / 1000;
        int gcCount = PerformanceUtils.getGCCount();
        
        inventory.setItem(30, InventoryUtils.createItem(Material.COMPOSTER, 
                "&b&lСборщик мусора", 
                "",
                "&7Всего сборок: &f" + gcCount,
                "&7Общее время: &f" + gcTime + " сек",
                "&7Среднее время: &f" + (gcCount > 0 ? String.format("%.2f", (double) gcTime / gcCount) : "0") + " сек"));

        int totalEntities = PerformanceUtils.getTotalEntities();
        int totalChunks = PerformanceUtils.getTotalChunks();
        
        List<String> worldLore = new ArrayList<>();
        worldLore.add("");
        worldLore.add("&7Загружено миров: &f" + Bukkit.getWorlds().size());
        worldLore.add("&7Загружено чанков: &f" + totalChunks);
        worldLore.add("&7Загружено сущностей: &f" + totalEntities);
        worldLore.add("");
        
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            worldLore.add("&e" + world.getName() + "&7:");
            worldLore.add("&8 ⁕ &7Чанки: &f" + world.getLoadedChunks().length);
            worldLore.add("&8 ⁕ &7Сущности: &f" + world.getEntities().size());
            worldLore.add("&8 ⁕ &7Игроки: &f" + world.getPlayers().size());
        }
        
        inventory.setItem(32, InventoryUtils.createItem(Material.GRASS_BLOCK, 
                "&b&lМиры и сущности", 
                worldLore));

        if (plugin.getModules().containsKey("ProtocolOptimizer")) {
            ProtocolOptimizerModule optimizer = (ProtocolOptimizerModule) plugin.getModules().get("ProtocolOptimizer");

            if (optimizer.isEnabled()) {
                List<String> optimizerLore = new ArrayList<>();
                optimizerLore.add("");
                optimizerLore.add("&7Статус: &a✓ Активен");
                optimizerLore.add("&7Текущий TPS: &f" + String.format("%.2f", optimizer.getTPS()));
                optimizerLore.add("");
                optimizerLore.add("&7Обработано пакетов:");
                optimizerLore.add("&8 ⁕ &7Блоки: &f" + optimizer.getBlockPacketsProcessed());
                optimizerLore.add("&8 ⁕ &7Сущности: &f" + optimizer.getEntityPacketsProcessed());
                optimizerLore.add("");
                optimizerLore.add("&7Вид. расстояние: &f" + optimizer.getSpawnViewDistance() + " чанков");
                optimizerLore.add("&7Экономия трафика: &f" + String.format("%.1f%%", optimizer.getBandwidthSavingPercent()));
                
                inventory.setItem(38, InventoryUtils.createItem(Material.CONDUIT, 
                        "&b&lProtocolOptimizer", 
                        optimizerLore));
            } else {
                                List<String> optimizerLore = new ArrayList<>();
                optimizerLore.add("");
                optimizerLore.add("&7Статус: &c✗ Неактивен");
                optimizerLore.add("");
                optimizerLore.add("&7Для получения информации");
                optimizerLore.add("&7необходимо активировать модуль");
                
                inventory.setItem(38, InventoryUtils.createItem(Material.BARRIER, 
                        "&c&lProtocolOptimizer", 
                        optimizerLore));
            }
        }
        
                inventory.setItem(49, InventoryUtils.createItem(Material.BOOKSHELF, 
                "&a&lМодули UltimaCore", 
                "",
                "&7Нажмите, чтобы просмотреть",
                "&7модули плагина и их настройки"));
        
                inventory.setItem(51, InventoryUtils.createItem(Material.REDSTONE, 
                "&c&lСборка мусора", 
                "",
                "&7Нажмите для запуска",
                "&7принудительной сборки мусора",
                "&c&lВнимание: &7может вызвать лаг!"));
        
        return inventory;
    }
    
    @Override
    public void handleClick(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event) {
        if (slot == 49) {
            new ModulesGUI(plugin).open(player);
        } else if (slot == 51 && player.hasPermission("ultimacore.admin")) {
            player.sendMessage(ChatColor.GREEN + "Запуск принудительной сборки мусора...");
            PerformanceUtils.forceGarbageCollection();
            update(player);
        }
    }
    
    @Override
    public void open(Player player) {
        super.open(player);

        if (taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (player.isOnline() && player.getOpenInventory().getTitle().equals(title)) {
                    update(player);
                } else {
                    Bukkit.getScheduler().cancelTask(taskId);
                    taskId = -1;
                }
            }, 20L, 20L).getTaskId();
        }
    }
} 