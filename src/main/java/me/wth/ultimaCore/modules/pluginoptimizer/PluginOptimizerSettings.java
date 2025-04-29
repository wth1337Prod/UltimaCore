package me.wth.ultimaCore.modules.pluginoptimizer;

import org.bukkit.configuration.ConfigurationSection;


public class PluginOptimizerSettings {
        private boolean enabled = true;                         private boolean monitorCommands = true;                 private boolean monitorTasks = true;                    private boolean monitorEvents = true;                   private boolean limitResourceUsage = true;              
        private int performanceCheckInterval = 60;              private int lagThreshold = 50;                          private boolean notifyConsole = true;                   private boolean notifyAdmins = true;                    
        private int maxCommandsPerSecond = 100;                 private int maxTasksPerPlugin = 50;                     private int minTaskInterval = 5;                        private double commandThrottleThreshold = 0.8;          
        private boolean autoOptimize = true;                    private boolean rescheduleHeavyTasks = true;            private boolean disableLaggyPlugins = false;            private int laggyPluginThreshold = 500;                 private boolean optimizeConfigs = true;                 
        private boolean customGarbageCollection = true;         private int garbageCollectionInterval = 300;            private boolean aggressiveGc = false;                   
        private boolean detailedLogging = false;                private boolean logPerformanceStats = true;             private int statsSaveInterval = 600;                    
    
    public void loadFromConfig(ConfigurationSection section) {
                enabled = section.getBoolean("enabled", enabled);
        monitorCommands = section.getBoolean("monitorCommands", monitorCommands);
        monitorTasks = section.getBoolean("monitorTasks", monitorTasks);
        monitorEvents = section.getBoolean("monitorEvents", monitorEvents);
        limitResourceUsage = section.getBoolean("limitResourceUsage", limitResourceUsage);
        
                performanceCheckInterval = section.getInt("performanceCheckInterval", performanceCheckInterval);
        lagThreshold = section.getInt("lagThreshold", lagThreshold);
        notifyConsole = section.getBoolean("notifyConsole", notifyConsole);
        notifyAdmins = section.getBoolean("notifyAdmins", notifyAdmins);
        
                maxCommandsPerSecond = section.getInt("maxCommandsPerSecond", maxCommandsPerSecond);
        maxTasksPerPlugin = section.getInt("maxTasksPerPlugin", maxTasksPerPlugin);
        minTaskInterval = section.getInt("minTaskInterval", minTaskInterval);
        commandThrottleThreshold = section.getDouble("commandThrottleThreshold", commandThrottleThreshold);
        
                autoOptimize = section.getBoolean("autoOptimize", autoOptimize);
        rescheduleHeavyTasks = section.getBoolean("rescheduleHeavyTasks", rescheduleHeavyTasks);
        disableLaggyPlugins = section.getBoolean("disableLaggyPlugins", disableLaggyPlugins);
        laggyPluginThreshold = section.getInt("laggyPluginThreshold", laggyPluginThreshold);
        optimizeConfigs = section.getBoolean("optimizeConfigs", optimizeConfigs);
        
                customGarbageCollection = section.getBoolean("customGarbageCollection", customGarbageCollection);
        garbageCollectionInterval = section.getInt("garbageCollectionInterval", garbageCollectionInterval);
        aggressiveGc = section.getBoolean("aggressiveGc", aggressiveGc);
        
                detailedLogging = section.getBoolean("detailedLogging", detailedLogging);
        logPerformanceStats = section.getBoolean("logPerformanceStats", logPerformanceStats);
        statsSaveInterval = section.getInt("statsSaveInterval", statsSaveInterval);
    }
    
    
    public void saveToConfig(ConfigurationSection section) {
                section.set("enabled", enabled);
        section.set("monitorCommands", monitorCommands);
        section.set("monitorTasks", monitorTasks);
        section.set("monitorEvents", monitorEvents);
        section.set("limitResourceUsage", limitResourceUsage);
        
                section.set("performanceCheckInterval", performanceCheckInterval);
        section.set("lagThreshold", lagThreshold);
        section.set("notifyConsole", notifyConsole);
        section.set("notifyAdmins", notifyAdmins);
        
                section.set("maxCommandsPerSecond", maxCommandsPerSecond);
        section.set("maxTasksPerPlugin", maxTasksPerPlugin);
        section.set("minTaskInterval", minTaskInterval);
        section.set("commandThrottleThreshold", commandThrottleThreshold);
        
                section.set("autoOptimize", autoOptimize);
        section.set("rescheduleHeavyTasks", rescheduleHeavyTasks);
        section.set("disableLaggyPlugins", disableLaggyPlugins);
        section.set("laggyPluginThreshold", laggyPluginThreshold);
        section.set("optimizeConfigs", optimizeConfigs);
        
                section.set("customGarbageCollection", customGarbageCollection);
        section.set("garbageCollectionInterval", garbageCollectionInterval);
        section.set("aggressiveGc", aggressiveGc);
        
                section.set("detailedLogging", detailedLogging);
        section.set("logPerformanceStats", logPerformanceStats);
        section.set("statsSaveInterval", statsSaveInterval);
    }
    
        
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isMonitorCommands() {
        return monitorCommands;
    }
    
    public void setMonitorCommands(boolean monitorCommands) {
        this.monitorCommands = monitorCommands;
    }
    
    public boolean isMonitorTasks() {
        return monitorTasks;
    }
    
    public void setMonitorTasks(boolean monitorTasks) {
        this.monitorTasks = monitorTasks;
    }
    
    public boolean isMonitorEvents() {
        return monitorEvents;
    }
    
    public void setMonitorEvents(boolean monitorEvents) {
        this.monitorEvents = monitorEvents;
    }
    
    public boolean isLimitResourceUsage() {
        return limitResourceUsage;
    }
    
    public void setLimitResourceUsage(boolean limitResourceUsage) {
        this.limitResourceUsage = limitResourceUsage;
    }
    
    public int getPerformanceCheckInterval() {
        return performanceCheckInterval;
    }
    
    public void setPerformanceCheckInterval(int performanceCheckInterval) {
        this.performanceCheckInterval = performanceCheckInterval;
    }
    
    public int getLagThreshold() {
        return lagThreshold;
    }
    
    public void setLagThreshold(int lagThreshold) {
        this.lagThreshold = lagThreshold;
    }
    
    public boolean isNotifyConsole() {
        return notifyConsole;
    }
    
    public void setNotifyConsole(boolean notifyConsole) {
        this.notifyConsole = notifyConsole;
    }
    
    public boolean isNotifyAdmins() {
        return notifyAdmins;
    }
    
    public void setNotifyAdmins(boolean notifyAdmins) {
        this.notifyAdmins = notifyAdmins;
    }
    
    public int getMaxCommandsPerSecond() {
        return maxCommandsPerSecond;
    }
    
    public void setMaxCommandsPerSecond(int maxCommandsPerSecond) {
        this.maxCommandsPerSecond = maxCommandsPerSecond;
    }
    
    public int getMaxTasksPerPlugin() {
        return maxTasksPerPlugin;
    }
    
    public void setMaxTasksPerPlugin(int maxTasksPerPlugin) {
        this.maxTasksPerPlugin = maxTasksPerPlugin;
    }
    
    public int getMinTaskInterval() {
        return minTaskInterval;
    }
    
    public void setMinTaskInterval(int minTaskInterval) {
        this.minTaskInterval = minTaskInterval;
    }
    
    public double getCommandThrottleThreshold() {
        return commandThrottleThreshold;
    }
    
    public void setCommandThrottleThreshold(double commandThrottleThreshold) {
        this.commandThrottleThreshold = commandThrottleThreshold;
    }
    
    public boolean isAutoOptimize() {
        return autoOptimize;
    }
    
    public void setAutoOptimize(boolean autoOptimize) {
        this.autoOptimize = autoOptimize;
    }
    
    public boolean isRescheduleHeavyTasks() {
        return rescheduleHeavyTasks;
    }
    
    public void setRescheduleHeavyTasks(boolean rescheduleHeavyTasks) {
        this.rescheduleHeavyTasks = rescheduleHeavyTasks;
    }
    
    public boolean isDisableLaggyPlugins() {
        return disableLaggyPlugins;
    }
    
    public void setDisableLaggyPlugins(boolean disableLaggyPlugins) {
        this.disableLaggyPlugins = disableLaggyPlugins;
    }
    
    public int getLaggyPluginThreshold() {
        return laggyPluginThreshold;
    }
    
    public void setLaggyPluginThreshold(int laggyPluginThreshold) {
        this.laggyPluginThreshold = laggyPluginThreshold;
    }
    
    public boolean isOptimizeConfigs() {
        return optimizeConfigs;
    }
    
    public void setOptimizeConfigs(boolean optimizeConfigs) {
        this.optimizeConfigs = optimizeConfigs;
    }
    
    public boolean isCustomGarbageCollection() {
        return customGarbageCollection;
    }
    
    public void setCustomGarbageCollection(boolean customGarbageCollection) {
        this.customGarbageCollection = customGarbageCollection;
    }
    
    public int getGarbageCollectionInterval() {
        return garbageCollectionInterval;
    }
    
    public void setGarbageCollectionInterval(int garbageCollectionInterval) {
        this.garbageCollectionInterval = garbageCollectionInterval;
    }
    
    public boolean isAggressiveGc() {
        return aggressiveGc;
    }
    
    public void setAggressiveGc(boolean aggressiveGc) {
        this.aggressiveGc = aggressiveGc;
    }
    
    public boolean isDetailedLogging() {
        return detailedLogging;
    }
    
    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }
    
    public boolean isLogPerformanceStats() {
        return logPerformanceStats;
    }
    
    public void setLogPerformanceStats(boolean logPerformanceStats) {
        this.logPerformanceStats = logPerformanceStats;
    }
    
    public int getStatsSaveInterval() {
        return statsSaveInterval;
    }
    
    public void setStatsSaveInterval(int statsSaveInterval) {
        this.statsSaveInterval = statsSaveInterval;
    }
} 