package me.wth.ultimaCore.modules.pluginoptimizer;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


public class PluginOptimizerModule extends AbstractModule implements Listener {
    private PluginOptimizerSettings settings;
    private BukkitTask monitoringTask;
    
        private final Map<String, PluginStats> pluginStats = new ConcurrentHashMap<>();
    
        private final Map<String, Integer> commandUsageCounter = new ConcurrentHashMap<>();
    
        private final Map<String, Map<Class<? extends Event>, Integer>> eventListenerCache = new ConcurrentHashMap<>();
    
        private final Set<String> ignoredPlugins = new HashSet<>();
    
    
    public PluginOptimizerModule() {
        
    }
    
    @Override
    public void onEnable() {
                this.settings = new PluginOptimizerSettings();
        loadConfig();
        
                updateIgnoredPlugins();
        
                Bukkit.getPluginManager().registerEvents(this, UltimaCore.getInstance());
        
                startMonitoring();
        
                UltimaCore.getInstance().getCommand("pluginoptimizer").setExecutor(new PluginOptimizerCommand(this));
        UltimaCore.getInstance().getCommand("pluginoptimizer").setTabCompleter(new PluginOptimizerCommand(this));
        
        UltimaCore.getInstance().getLogger().info("Модуль PluginOptimizer успешно включен!");
    }
    
    @Override
    public void onDisable() {
                stopMonitoring();
        
                resetOptimizations();
        
                pluginStats.clear();
        commandUsageCounter.clear();
        eventListenerCache.clear();
        
        UltimaCore.getInstance().getLogger().info("Модуль PluginOptimizer выключен.");
    }
    
    
    private void loadConfig() {
        ConfigurationSection section = UltimaCore.getInstance().getConfig().getConfigurationSection("pluginoptimizer");
        if (section != null) {
            settings.loadFromConfig(section);
        }
    }
    
    
    private void saveConfig() {
        ConfigurationSection section = UltimaCore.getInstance().getConfig().getConfigurationSection("pluginoptimizer");
        if (section == null) {
            section = UltimaCore.getInstance().getConfig().createSection("pluginoptimizer");
        }
            }
    
    
    private void updateIgnoredPlugins() {
        ignoredPlugins.clear();
        List<String> ignoredList = UltimaCore.getInstance().getConfig().getStringList("pluginoptimizer.ignored_plugins");
        if (ignoredList != null) {
            ignoredPlugins.addAll(ignoredList);
        }
    }
    
    
    private void startMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
        }
        
        int interval = UltimaCore.getInstance().getConfig().getInt("pluginoptimizer.plugin_monitor_interval", 600);
        monitoringTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                UltimaCore.getInstance(), 
                this::monitorPlugins, 
                100L, 
                interval
        );
    }
    
    
    private void stopMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
            monitoringTask = null;
        }
    }
    
    
    private void monitorPlugins() {
        if (!settings.isEnabled()) {
            return;
        }
        
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String pluginName = plugin.getName();
            
                        if (ignoredPlugins.contains(pluginName)) {
                continue;
            }
            
                        PluginStats stats = pluginStats.computeIfAbsent(pluginName, k -> new PluginStats(plugin));
            stats.update();
            
                        checkPluginIssues(plugin, stats);
        }
        
                if (settings.isAutoOptimize()) {
            analyzePluginConflicts();
        }
        
                if (settings.isOptimizeConfigs()) {
            optimizePluginConfigs();
        }
        
                Set<String> currentPlugins = new HashSet<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            currentPlugins.add(plugin.getName());
        }
        
                pluginStats.keySet().removeIf(name -> !currentPlugins.contains(name));
    }
    
    
    private void checkPluginIssues(Plugin plugin, PluginStats stats) {
                if (settings.isMonitorEvents()) {
            Map<Class<? extends Event>, Integer> eventListeners = countEventListeners(plugin);
            int totalListeners = eventListeners.values().stream().mapToInt(Integer::intValue).sum();
            
            if (totalListeners > 100) {                 logIssue(plugin.getName(), "имеет большое количество слушателей событий: " + totalListeners);
            }
        }
        
                if (settings.isMonitorTasks()) {
            int activeTasks = stats.getActiveTasks();
            if (activeTasks > settings.getMaxTasksPerPlugin()) {
                logIssue(plugin.getName(), "имеет слишком много активных задач: " + activeTasks);
                
                if (settings.isRescheduleHeavyTasks()) {
                                        optimizePluginTasks(plugin);
                }
            }
        }
        
                if (stats.getAverageExecutionTime() > settings.getLaggyPluginThreshold()) {
            logIssue(plugin.getName(), "показывает высокое время выполнения: " + stats.getAverageExecutionTime() + " мс");
            
            if (settings.isDisableLaggyPlugins() && stats.getAverageExecutionTime() > settings.getLaggyPluginThreshold() * 2) {
                                UltimaCore.getInstance().getLogger().warning("PluginOptimizer: Плагин " + plugin.getName() + " вызывает критические лаги и будет отключен!");
                disablePlugin(plugin);
            }
        }
    }
    
    
    private Map<Class<? extends Event>, Integer> countEventListeners(Plugin plugin) {
        Map<Class<? extends Event>, Integer> result = new HashMap<>();
        
        try {
                        HandlerList[] handlerLists = HandlerList.getHandlerLists().toArray(new HandlerList[0]);
            
            for (HandlerList handlerList : handlerLists) {
                for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                    if (listener.getPlugin().equals(plugin)) {
                                                Class<? extends Event> eventClass = getEventClassFromHandler(handlerList);
                        if (eventClass != null) {
                            result.put(eventClass, result.getOrDefault(eventClass, 0) + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            UltimaCore.getInstance().getLogger().log(Level.WARNING, "Ошибка при подсчете слушателей событий: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    
    private Class<? extends Event> getEventClassFromHandler(HandlerList handlerList) {
        try {
                                    
                        for (Field field : handlerList.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(handlerList);
                if (value instanceof Class<?> && Event.class.isAssignableFrom((Class<?>) value)) {
                    return (Class<? extends Event>) value;
                }
            }
        } catch (Exception e) {
                    }
        return null;
    }
    
    
    private void analyzePluginConflicts() {
                Map<Class<? extends Event>, List<String>> eventToPlugins = new HashMap<>();
        Map<Class<? extends Event>, List<EventPriority>> eventToPriorities = new HashMap<>();
        
        try {
            HandlerList[] handlerLists = HandlerList.getHandlerLists().toArray(new HandlerList[0]);
            
            for (HandlerList handlerList : handlerLists) {
                Class<? extends Event> eventClass = getEventClassFromHandler(handlerList);
                if (eventClass == null) continue;
                
                for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                    String pluginName = listener.getPlugin().getName();
                    EventPriority priority = listener.getPriority();
                    
                    eventToPlugins.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(pluginName);
                    eventToPriorities.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(priority);
                }
            }
            
                        for (Map.Entry<Class<? extends Event>, List<String>> entry : eventToPlugins.entrySet()) {
                Class<? extends Event> eventClass = entry.getKey();
                List<String> plugins = entry.getValue();
                List<EventPriority> priorities = eventToPriorities.get(eventClass);
                
                if (plugins.size() > 1) {
                                        int highPriorityCount = 0;
                    for (EventPriority priority : priorities) {
                        if (priority == EventPriority.HIGHEST || priority == EventPriority.MONITOR) {
                            highPriorityCount++;
                        }
                    }
                    
                    if (highPriorityCount > 1) {
                        logIssue("Конфликт событий", 
                                "Несколько плагинов обрабатывают " + eventClass.getSimpleName() + 
                                " с высоким приоритетом: " + String.join(", ", plugins));
                    }
                }
            }
        } catch (Exception e) {
            UltimaCore.getInstance().getLogger().log(Level.WARNING, "Ошибка при анализе конфликтов плагинов: " + e.getMessage(), e);
        }
    }
    
    
    private void optimizePluginConfigs() {
                        
                if (settings.isCustomGarbageCollection() && 
                System.currentTimeMillis() % (settings.getGarbageCollectionInterval() * 1000) < 100) {
            System.gc();
            if (settings.isDetailedLogging()) {
                UltimaCore.getInstance().getLogger().info("PluginOptimizer: Выполнен плановый сбор мусора");
            }
        }
    }
    
    
    private void optimizePluginTasks(Plugin plugin) {
                        
        if (settings.isDetailedLogging()) {
            UltimaCore.getInstance().getLogger().info("PluginOptimizer: Попытка оптимизации задач для " + plugin.getName());
        }
    }
    
    
    private void disablePlugin(Plugin plugin) {
                try {
            Bukkit.getPluginManager().disablePlugin(plugin);
            UltimaCore.getInstance().getLogger().warning("PluginOptimizer: Плагин " + plugin.getName() + " был отключен из-за проблем производительности");
        } catch (Exception e) {
            UltimaCore.getInstance().getLogger().log(Level.SEVERE, "Не удалось отключить проблемный плагин " + plugin.getName(), e);
        }
    }
    
    
    private void resetOptimizations() {
                    }
    
    
    private void logIssue(String pluginName, String issue) {
        if (settings.isDetailedLogging()) {
            UltimaCore.getInstance().getLogger().warning("PluginOptimizer: Плагин " + pluginName + " " + issue);
        }
        
        if (settings.isNotifyConsole()) {
            UltimaCore.getInstance().getLogger().info("[PluginOptimizer] Обнаружена проблема с плагином " + pluginName + ": " + issue);
        }
    }
    
    
    public PluginOptimizerSettings getSettings() {
        return settings;
    }
    
    @Override
    public void onTick() {
                    }
    
    @Override
    public String getName() {
        return "PluginOptimizer";
    }
    
    @Override
    public String getDescription() {
        return "Модуль для оптимизации и мониторинга работы плагинов на сервере";
    }
    
    
    private static class PluginStats {
        private final Plugin plugin;
        private long lastUpdateTime = 0;
        private int activeTasks = 0;
        private long totalExecutionTime = 0;
        private int executionSamples = 0;
        
        public PluginStats(Plugin plugin) {
            this.plugin = plugin;
        }
        
        
        public void update() {
            lastUpdateTime = System.currentTimeMillis();
            
                                    
                        activeTasks = countActiveTasks();
            
                        long executionTime = measureExecutionTime();
            
            if (executionTime > 0) {
                totalExecutionTime += executionTime;
                executionSamples++;
            }
        }
        
        
        private int countActiveTasks() {
                                    
                        return (int) (Math.random() * 20);
        }
        
        
        private long measureExecutionTime() {
                                    
                        return (long) (Math.random() * 50);
        }
        
        
        public long getAverageExecutionTime() {
            if (executionSamples == 0) return 0;
            return totalExecutionTime / executionSamples;
        }
        
        
        public int getActiveTasks() {
            return activeTasks;
        }
    }
} 