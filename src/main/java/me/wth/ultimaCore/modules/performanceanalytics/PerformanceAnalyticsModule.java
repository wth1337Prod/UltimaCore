package me.wth.ultimaCore.modules.performanceanalytics;

import com.sun.management.OperatingSystemMXBean;
import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.config.Config;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class PerformanceAnalyticsModule extends AbstractModule {
    private PerformanceAnalyticsSettings settings;
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
        private BukkitTask tpsMonitorTask;
    private BukkitTask memoryMonitorTask;
    private BukkitTask cpuMonitorTask;
    private BukkitTask pluginMonitorTask;
    
        private final Map<String, PerformanceMetric> metrics = new ConcurrentHashMap<>();
    private final LinkedList<PerformanceSnapshot> history = new LinkedList<>();
    
        private long lastTickTime = System.currentTimeMillis();
    private int tickCount = 0;
    private double currentTps = 20.0;
    
        private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    
    public PerformanceAnalyticsModule() {
        super();
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                this.settings = new PerformanceAnalyticsSettings();
        loadConfig();
        
                startMonitoringTasks();
        
                try {
            PerformanceAnalyticsCommand commandHandler = new PerformanceAnalyticsCommand(this);
            if (plugin.getCommand("performance") != null) {
                plugin.getCommand("performance").setExecutor(commandHandler);
                plugin.getCommand("performance").setTabCompleter(commandHandler);
            } else {
                LoggerUtil.warning("Команда 'performance' не найдена в plugin.yml. Команда не будет зарегистрирована.");
            }
        } catch (Exception e) {
            LoggerUtil.warning("Не удалось зарегистрировать команду для модуля PerformanceAnalytics", e);
        }
        
        LoggerUtil.info("Модуль PerformanceAnalytics успешно включен!");
    }
    
    @Override
    public void onDisable() {
                stopMonitoringTasks();
        
                if (settings.isSaveDataOnDisable()) {
            savePerformanceData();
        }
        
        super.onDisable();
        LoggerUtil.info("Модуль PerformanceAnalytics выключен.");
    }
    
    
    public void loadConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("performanceanalytics");
        
        if (section == null) {
            section = config.createSection("performanceanalytics");
        }
        
        settings.loadFromConfig(section);
        config.save();
    }
    
    
    public void saveConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("performanceanalytics");
        
        if (section == null) {
            section = config.createSection("performanceanalytics");
        }
        
        settings.saveToConfig(section);
        config.save();
    }
    
    
    private void startMonitoringTasks() {
                tpsMonitorTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTpsCounter, 1L, 1L);
        
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::calculateTps, 100L, 100L);
        
                memoryMonitorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, 
                this::collectMemoryMetrics, 20L, 20L * settings.getMemoryCheckInterval());
        
                cpuMonitorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, 
                this::collectCpuMetrics, 20L, 20L * settings.getCpuCheckInterval());
        
                if (settings.isEnablePluginMonitoring()) {
            pluginMonitorTask = Bukkit.getScheduler().runTaskTimer(plugin, 
                    this::collectPluginMetrics, 20L, 20L * settings.getPluginCheckInterval());
        }
    }
    
    
    private void stopMonitoringTasks() {
        if (tpsMonitorTask != null) {
            tpsMonitorTask.cancel();
            tpsMonitorTask = null;
        }
        
        if (memoryMonitorTask != null) {
            memoryMonitorTask.cancel();
            memoryMonitorTask = null;
        }
        
        if (cpuMonitorTask != null) {
            cpuMonitorTask.cancel();
            cpuMonitorTask = null;
        }
        
        if (pluginMonitorTask != null) {
            pluginMonitorTask.cancel();
            pluginMonitorTask = null;
        }
    }
    
    
    private void updateTpsCounter() {
        tickCount++;
    }
    
    
    private void calculateTps() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickTime;
        
        if (elapsed < 1000) {
            return;
        }
        
        currentTps = tickCount / (elapsed / 1000.0);
        if (currentTps > 20.0) {
            currentTps = 20.0;
        }
        
                updateMetric("tps", currentTps);
        
                if (settings.isEnableHistoryTracking()) {
            createPerformanceSnapshot();
        }
        
                checkThresholds();
        
                lastTickTime = now;
        tickCount = 0;
    }
    
    
    private void collectMemoryMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
                updateMetric("memory.max", maxMemory / (1024 * 1024));         updateMetric("memory.total", totalMemory / (1024 * 1024));         updateMetric("memory.used", usedMemory / (1024 * 1024));         updateMetric("memory.free", freeMemory / (1024 * 1024));         updateMetric("memory.usage", memoryUsagePercent);         
                collectGcMetrics();
    }
    
    
    private void collectGcMetrics() {
        long gcCount = 0;
        long gcTime = 0;
        
        for (var gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gcBean.getCollectionCount();
            if (count > 0) {
                gcCount += count;
                gcTime += gcBean.getCollectionTime();
            }
        }
        
        updateMetric("gc.count", gcCount);
        updateMetric("gc.time", gcTime);     }
    
    
    private void collectCpuMetrics() {
        double cpuUsage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        
                if (cpuUsage >= 0) {
            double normalizedCpuUsage = (cpuUsage / availableProcessors) * 100;
            updateMetric("cpu.system", normalizedCpuUsage);         }
        
                updateMetric("threads.count", threadBean.getThreadCount());
    }
    
    
    private void collectPluginMetrics() {
                            }
    
    
    private void createPerformanceSnapshot() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.timestamp = System.currentTimeMillis();
        snapshot.tps = getMetricValue("tps");
        snapshot.memoryUsage = getMetricValue("memory.usage");
        snapshot.cpuUsage = getMetricValue("cpu.system");
        snapshot.playersOnline = Bukkit.getOnlinePlayers().size();
        snapshot.chunksLoaded = countLoadedChunks();
        snapshot.entitiesCount = countEntities();
        
        history.add(snapshot);
        
                while (history.size() > settings.getHistorySize()) {
            history.removeFirst();
        }
    }
    
    
    private void checkThresholds() {
                if (currentTps < settings.getTpsWarningThreshold() && settings.isEnableTpsWarnings()) {
            String message = ChatColor.RED + "Внимание! TPS сервера упал до " + 
                    decimalFormat.format(currentTps) + "!";
            broadcastWarning(message);
        }
        
                double memoryUsage = getMetricValue("memory.usage");
        if (memoryUsage > settings.getMemoryWarningThreshold() && settings.isEnableMemoryWarnings()) {
            String message = ChatColor.RED + "Внимание! Использование памяти достигло " + 
                    decimalFormat.format(memoryUsage) + "%!";
            broadcastWarning(message);
        }
        
                double cpuUsage = getMetricValue("cpu.system");
        if (cpuUsage > settings.getCpuWarningThreshold() && settings.isEnableCpuWarnings()) {
            String message = ChatColor.RED + "Внимание! Загрузка CPU достигла " + 
                    decimalFormat.format(cpuUsage) + "%!";
            broadcastWarning(message);
        }
    }
    
    
    private void broadcastWarning(String message) {
        LoggerUtil.warning(message);
        
        if (settings.isBroadcastWarnings()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("ultimacore.performance.warnings")) {
                    player.sendMessage(message);
                }
            }
        }
    }
    
    
    private int countLoadedChunks() {
        int count = 0;
        for (var world : Bukkit.getWorlds()) {
            count += world.getLoadedChunks().length;
        }
        return count;
    }
    
    
    private int countEntities() {
                AtomicInteger count = new AtomicInteger(0);
        
                try {
            if (Bukkit.isPrimaryThread()) {
                                for (var world : Bukkit.getWorlds()) {
                    count.addAndGet(world.getEntities().size());
                }
            } else {
                                CompletableFuture<Integer> future = new CompletableFuture<>();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    int totalEntities = 0;
                    for (var world : Bukkit.getWorlds()) {
                        totalEntities += world.getEntities().size();
                    }
                    future.complete(totalEntities);
                });
                
                try {
                                        count.set(future.get(1, TimeUnit.SECONDS));
                } catch (Exception e) {
                    LoggerUtil.warning("Не удалось получить количество сущностей асинхронно", e);
                }
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при подсчете сущностей", e);
        }
        
        return count.get();
    }
    
    
    private void updateMetric(String name, double value) {
        PerformanceMetric metric = metrics.computeIfAbsent(name, k -> new PerformanceMetric(name));
        metric.update(value);
    }
    
    
    public double getMetricValue(String name) {
        PerformanceMetric metric = metrics.get(name);
        return metric != null ? metric.getCurrentValue() : 0.0;
    }
    
    
    private void savePerformanceData() {
                    }
    
    
    public void showPerformanceReport(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== Отчет о производительности сервера =====");
        
                double tps = getMetricValue("tps");
        String tpsColor = tps > 18 ? ChatColor.GREEN.toString() : 
                          tps > 15 ? ChatColor.YELLOW.toString() : ChatColor.RED.toString();
        sender.sendMessage(ChatColor.GOLD + "TPS: " + tpsColor + decimalFormat.format(tps));
        
                double memUsed = getMetricValue("memory.used");
        double memMax = getMetricValue("memory.max");
        double memPercent = getMetricValue("memory.usage");
        String memColor = memPercent < 70 ? ChatColor.GREEN.toString() : 
                          memPercent < 85 ? ChatColor.YELLOW.toString() : ChatColor.RED.toString();
        
        sender.sendMessage(ChatColor.GOLD + "Память: " + memColor + decimalFormat.format(memUsed) + 
                " / " + decimalFormat.format(memMax) + " МБ (" + decimalFormat.format(memPercent) + "%)");
        
                double cpu = getMetricValue("cpu.system");
        String cpuColor = cpu < 50 ? ChatColor.GREEN.toString() : 
                          cpu < 75 ? ChatColor.YELLOW.toString() : ChatColor.RED.toString();
        sender.sendMessage(ChatColor.GOLD + "Загрузка CPU: " + cpuColor + decimalFormat.format(cpu) + "%");
        
                int threads = (int) getMetricValue("threads.count");
        sender.sendMessage(ChatColor.GOLD + "Потоки: " + ChatColor.WHITE + threads);
        
                sender.sendMessage(ChatColor.GOLD + "Загружено чанков: " + ChatColor.WHITE + countLoadedChunks());
        sender.sendMessage(ChatColor.GOLD + "Сущностей: " + ChatColor.WHITE + countEntities());
        
                sender.sendMessage(ChatColor.GOLD + "Игроков онлайн: " + ChatColor.WHITE + 
                Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
        
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    
    public PerformanceAnalyticsSettings getSettings() {
        return settings;
    }
    
    
    public List<PerformanceSnapshot> getHistory() {
        return Collections.unmodifiableList(history);
    }
    
    @Override
    public void onTick() {
            }
    
    @Override
    public String getName() {
        return "PerformanceAnalytics";
    }
    
    @Override
    public String getDescription() {
        return "Модуль для анализа производительности сервера и мониторинга ключевых метрик";
    }
    
    
    private static class PerformanceMetric {
        private final String name;
        private double currentValue;
        private double minValue = Double.MAX_VALUE;
        private double maxValue = Double.MIN_VALUE;
        private double totalValue;
        private long updateCount;
        private long lastUpdateTime;
        
        public PerformanceMetric(String name) {
            this.name = name;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public void update(double value) {
            this.currentValue = value;
            this.totalValue += value;
            this.updateCount++;
            this.lastUpdateTime = System.currentTimeMillis();
            
            if (value < minValue) {
                minValue = value;
            }
            
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        public String getName() {
            return name;
        }
        
        public double getCurrentValue() {
            return currentValue;
        }
        
        public double getMinValue() {
            return minValue == Double.MAX_VALUE ? 0 : minValue;
        }
        
        public double getMaxValue() {
            return maxValue == Double.MIN_VALUE ? 0 : maxValue;
        }
        
        public double getAverageValue() {
            return updateCount > 0 ? totalValue / updateCount : 0;
        }
        
        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
    }
    
    
    public static class PerformanceSnapshot {
        public long timestamp;
        public double tps;
        public double memoryUsage;
        public double cpuUsage;
        public int playersOnline;
        public int chunksLoaded;
        public int entitiesCount;
    }
} 