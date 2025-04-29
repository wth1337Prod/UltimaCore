package me.wth.ultimaCore.modules.performanceanalytics;

import org.bukkit.configuration.ConfigurationSection;


public class PerformanceAnalyticsSettings {
        private int memoryCheckInterval = 30;        private int cpuCheckInterval = 60;           private int pluginCheckInterval = 300;       
        private double tpsWarningThreshold = 16.0;         private double memoryWarningThreshold = 85.0;      private double cpuWarningThreshold = 80.0;         
        private boolean enableTpsWarnings = true;          private boolean enableMemoryWarnings = true;       private boolean enableCpuWarnings = true;          private boolean broadcastWarnings = true;          
        private boolean enablePluginMonitoring = false;      private boolean enableHistoryTracking = true;        private boolean saveDataOnDisable = true;            private int historySize = 100;                       
    
    public void loadFromConfig(ConfigurationSection section) {
                memoryCheckInterval = section.getInt("memoryCheckInterval", memoryCheckInterval);
        cpuCheckInterval = section.getInt("cpuCheckInterval", cpuCheckInterval);
        pluginCheckInterval = section.getInt("pluginCheckInterval", pluginCheckInterval);
        
                tpsWarningThreshold = section.getDouble("tpsWarningThreshold", tpsWarningThreshold);
        memoryWarningThreshold = section.getDouble("memoryWarningThreshold", memoryWarningThreshold);
        cpuWarningThreshold = section.getDouble("cpuWarningThreshold", cpuWarningThreshold);
        
                enableTpsWarnings = section.getBoolean("enableTpsWarnings", enableTpsWarnings);
        enableMemoryWarnings = section.getBoolean("enableMemoryWarnings", enableMemoryWarnings);
        enableCpuWarnings = section.getBoolean("enableCpuWarnings", enableCpuWarnings);
        broadcastWarnings = section.getBoolean("broadcastWarnings", broadcastWarnings);
        
                enablePluginMonitoring = section.getBoolean("enablePluginMonitoring", enablePluginMonitoring);
        enableHistoryTracking = section.getBoolean("enableHistoryTracking", enableHistoryTracking);
        saveDataOnDisable = section.getBoolean("saveDataOnDisable", saveDataOnDisable);
        historySize = section.getInt("historySize", historySize);
    }
    
    
    public void saveToConfig(ConfigurationSection section) {
                section.set("memoryCheckInterval", memoryCheckInterval);
        section.set("cpuCheckInterval", cpuCheckInterval);
        section.set("pluginCheckInterval", pluginCheckInterval);
        
                section.set("tpsWarningThreshold", tpsWarningThreshold);
        section.set("memoryWarningThreshold", memoryWarningThreshold);
        section.set("cpuWarningThreshold", cpuWarningThreshold);
        
                section.set("enableTpsWarnings", enableTpsWarnings);
        section.set("enableMemoryWarnings", enableMemoryWarnings);
        section.set("enableCpuWarnings", enableCpuWarnings);
        section.set("broadcastWarnings", broadcastWarnings);
        
                section.set("enablePluginMonitoring", enablePluginMonitoring);
        section.set("enableHistoryTracking", enableHistoryTracking);
        section.set("saveDataOnDisable", saveDataOnDisable);
        section.set("historySize", historySize);
    }
    
        
    public int getMemoryCheckInterval() {
        return memoryCheckInterval;
    }
    
    public void setMemoryCheckInterval(int memoryCheckInterval) {
        this.memoryCheckInterval = memoryCheckInterval;
    }
    
    public int getCpuCheckInterval() {
        return cpuCheckInterval;
    }
    
    public void setCpuCheckInterval(int cpuCheckInterval) {
        this.cpuCheckInterval = cpuCheckInterval;
    }
    
    public int getPluginCheckInterval() {
        return pluginCheckInterval;
    }
    
    public void setPluginCheckInterval(int pluginCheckInterval) {
        this.pluginCheckInterval = pluginCheckInterval;
    }
    
    public double getTpsWarningThreshold() {
        return tpsWarningThreshold;
    }
    
    public void setTpsWarningThreshold(double tpsWarningThreshold) {
        this.tpsWarningThreshold = tpsWarningThreshold;
    }
    
    public double getMemoryWarningThreshold() {
        return memoryWarningThreshold;
    }
    
    public void setMemoryWarningThreshold(double memoryWarningThreshold) {
        this.memoryWarningThreshold = memoryWarningThreshold;
    }
    
    public double getCpuWarningThreshold() {
        return cpuWarningThreshold;
    }
    
    public void setCpuWarningThreshold(double cpuWarningThreshold) {
        this.cpuWarningThreshold = cpuWarningThreshold;
    }
    
    public boolean isEnableTpsWarnings() {
        return enableTpsWarnings;
    }
    
    public void setEnableTpsWarnings(boolean enableTpsWarnings) {
        this.enableTpsWarnings = enableTpsWarnings;
    }
    
    public boolean isEnableMemoryWarnings() {
        return enableMemoryWarnings;
    }
    
    public void setEnableMemoryWarnings(boolean enableMemoryWarnings) {
        this.enableMemoryWarnings = enableMemoryWarnings;
    }
    
    public boolean isEnableCpuWarnings() {
        return enableCpuWarnings;
    }
    
    public void setEnableCpuWarnings(boolean enableCpuWarnings) {
        this.enableCpuWarnings = enableCpuWarnings;
    }
    
    public boolean isBroadcastWarnings() {
        return broadcastWarnings;
    }
    
    public void setBroadcastWarnings(boolean broadcastWarnings) {
        this.broadcastWarnings = broadcastWarnings;
    }
    
    public boolean isEnablePluginMonitoring() {
        return enablePluginMonitoring;
    }
    
    public void setEnablePluginMonitoring(boolean enablePluginMonitoring) {
        this.enablePluginMonitoring = enablePluginMonitoring;
    }
    
    public boolean isEnableHistoryTracking() {
        return enableHistoryTracking;
    }
    
    public void setEnableHistoryTracking(boolean enableHistoryTracking) {
        this.enableHistoryTracking = enableHistoryTracking;
    }
    
    public boolean isSaveDataOnDisable() {
        return saveDataOnDisable;
    }
    
    public void setSaveDataOnDisable(boolean saveDataOnDisable) {
        this.saveDataOnDisable = saveDataOnDisable;
    }
    
    public int getHistorySize() {
        return historySize;
    }
    
    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }
} 