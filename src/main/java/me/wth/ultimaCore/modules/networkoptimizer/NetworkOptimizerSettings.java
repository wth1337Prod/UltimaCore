package me.wth.ultimaCore.modules.networkoptimizer;

import org.bukkit.configuration.ConfigurationSection;


public class NetworkOptimizerSettings {
        private int mediumLatencyThreshold = 150;      private int highLatencyThreshold = 300;        
        private int monitoringInterval = 30;           
        private boolean enabledEntityVisibilityOptimization = true;      private int normalEntityViewDistance = 64;                        private int reducedEntityViewDistance = 32;                       
        private boolean enabledChunkUpdateOptimization = true;         private int normalChunkUpdateRate = 2;                         private int reducedChunkUpdateRate = 4;                        
        private boolean enablePacketCompression = true;                private int compressionThreshold = 256;                        
        private boolean enableDdosProtection = true;                   private int connectionThrottleTime = 3000;                     private int maxConnectionsPerIP = 3;                           private int loginTimeout = 30;                                 private int packetSpamThreshold = 300;                         private boolean enableConnectionRateLimit = true;              private int maxGlobalConnections = 20;                         private int connectionLimitPeriod = 60;                        private boolean blockProxiesAndVPNs = true;                    private boolean enableGeoIPFiltering = false;                  private String[] allowedCountries = {"RU", "BY", "KZ"};       
        private boolean enableDetailedLogging = false;                 private boolean enableOptimizationOnJoin = true;               
    
    public NetworkOptimizerSettings() {
            }
    
    
    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;
        
                this.mediumLatencyThreshold = section.getInt("mediumLatencyThreshold", mediumLatencyThreshold);
        this.highLatencyThreshold = section.getInt("highLatencyThreshold", highLatencyThreshold);
        
                this.monitoringInterval = section.getInt("monitoringInterval", monitoringInterval);
        
                this.enabledEntityVisibilityOptimization = section.getBoolean("enabledEntityVisibilityOptimization", enabledEntityVisibilityOptimization);
        this.normalEntityViewDistance = section.getInt("normalEntityViewDistance", normalEntityViewDistance);
        this.reducedEntityViewDistance = section.getInt("reducedEntityViewDistance", reducedEntityViewDistance);
        
                this.enabledChunkUpdateOptimization = section.getBoolean("enabledChunkUpdateOptimization", enabledChunkUpdateOptimization);
        this.normalChunkUpdateRate = section.getInt("normalChunkUpdateRate", normalChunkUpdateRate);
        this.reducedChunkUpdateRate = section.getInt("reducedChunkUpdateRate", reducedChunkUpdateRate);
        
                this.enablePacketCompression = section.getBoolean("enablePacketCompression", enablePacketCompression);
        this.compressionThreshold = section.getInt("compressionThreshold", compressionThreshold);
        
                this.enableDdosProtection = section.getBoolean("enableDdosProtection", enableDdosProtection);
        this.connectionThrottleTime = section.getInt("connectionThrottleTime", connectionThrottleTime);
        this.maxConnectionsPerIP = section.getInt("maxConnectionsPerIP", maxConnectionsPerIP);
        this.loginTimeout = section.getInt("loginTimeout", loginTimeout);
        this.packetSpamThreshold = section.getInt("packetSpamThreshold", packetSpamThreshold);
        this.enableConnectionRateLimit = section.getBoolean("enableConnectionRateLimit", enableConnectionRateLimit);
        this.maxGlobalConnections = section.getInt("maxGlobalConnections", maxGlobalConnections);
        this.connectionLimitPeriod = section.getInt("connectionLimitPeriod", connectionLimitPeriod);
        this.blockProxiesAndVPNs = section.getBoolean("blockProxiesAndVPNs", blockProxiesAndVPNs);
        this.enableGeoIPFiltering = section.getBoolean("enableGeoIPFiltering", enableGeoIPFiltering);
        
        if (section.contains("allowedCountries")) {
            this.allowedCountries = section.getStringList("allowedCountries").toArray(new String[0]);
        }
        
                this.enableDetailedLogging = section.getBoolean("enableDetailedLogging", enableDetailedLogging);
        this.enableOptimizationOnJoin = section.getBoolean("enableOptimizationOnJoin", enableOptimizationOnJoin);
    }
    
    
    public void saveToConfig(ConfigurationSection section) {
        if (section == null) return;
        
                section.set("mediumLatencyThreshold", mediumLatencyThreshold);
        section.set("highLatencyThreshold", highLatencyThreshold);
        
                section.set("monitoringInterval", monitoringInterval);
        
                section.set("enabledEntityVisibilityOptimization", enabledEntityVisibilityOptimization);
        section.set("normalEntityViewDistance", normalEntityViewDistance);
        section.set("reducedEntityViewDistance", reducedEntityViewDistance);
        
                section.set("enabledChunkUpdateOptimization", enabledChunkUpdateOptimization);
        section.set("normalChunkUpdateRate", normalChunkUpdateRate);
        section.set("reducedChunkUpdateRate", reducedChunkUpdateRate);
        
                section.set("enablePacketCompression", enablePacketCompression);
        section.set("compressionThreshold", compressionThreshold);
        
                section.set("enableDdosProtection", enableDdosProtection);
        section.set("connectionThrottleTime", connectionThrottleTime);
        section.set("maxConnectionsPerIP", maxConnectionsPerIP);
        section.set("loginTimeout", loginTimeout);
        section.set("packetSpamThreshold", packetSpamThreshold);
        section.set("enableConnectionRateLimit", enableConnectionRateLimit);
        section.set("maxGlobalConnections", maxGlobalConnections);
        section.set("connectionLimitPeriod", connectionLimitPeriod);
        section.set("blockProxiesAndVPNs", blockProxiesAndVPNs);
        section.set("enableGeoIPFiltering", enableGeoIPFiltering);
        section.set("allowedCountries", allowedCountries);
        
                section.set("enableDetailedLogging", enableDetailedLogging);
        section.set("enableOptimizationOnJoin", enableOptimizationOnJoin);
    }
    
        
    public int getMediumLatencyThreshold() {
        return mediumLatencyThreshold;
    }
    
    public void setMediumLatencyThreshold(int mediumLatencyThreshold) {
        this.mediumLatencyThreshold = mediumLatencyThreshold;
    }
    
    public int getHighLatencyThreshold() {
        return highLatencyThreshold;
    }
    
    public void setHighLatencyThreshold(int highLatencyThreshold) {
        this.highLatencyThreshold = highLatencyThreshold;
    }
    
    public int getMonitoringInterval() {
        return monitoringInterval;
    }
    
    public void setMonitoringInterval(int monitoringInterval) {
        this.monitoringInterval = monitoringInterval;
    }
    
    public boolean isEnabledEntityVisibilityOptimization() {
        return enabledEntityVisibilityOptimization;
    }
    
    public void setEnabledEntityVisibilityOptimization(boolean enabledEntityVisibilityOptimization) {
        this.enabledEntityVisibilityOptimization = enabledEntityVisibilityOptimization;
    }
    
    public int getNormalEntityViewDistance() {
        return normalEntityViewDistance;
    }
    
    public void setNormalEntityViewDistance(int normalEntityViewDistance) {
        this.normalEntityViewDistance = normalEntityViewDistance;
    }
    
    public int getReducedEntityViewDistance() {
        return reducedEntityViewDistance;
    }
    
    public void setReducedEntityViewDistance(int reducedEntityViewDistance) {
        this.reducedEntityViewDistance = reducedEntityViewDistance;
    }
    
    public boolean isEnabledChunkUpdateOptimization() {
        return enabledChunkUpdateOptimization;
    }
    
    public void setEnabledChunkUpdateOptimization(boolean enabledChunkUpdateOptimization) {
        this.enabledChunkUpdateOptimization = enabledChunkUpdateOptimization;
    }
    
    public int getNormalChunkUpdateRate() {
        return normalChunkUpdateRate;
    }
    
    public void setNormalChunkUpdateRate(int normalChunkUpdateRate) {
        this.normalChunkUpdateRate = normalChunkUpdateRate;
    }
    
    public int getReducedChunkUpdateRate() {
        return reducedChunkUpdateRate;
    }
    
    public void setReducedChunkUpdateRate(int reducedChunkUpdateRate) {
        this.reducedChunkUpdateRate = reducedChunkUpdateRate;
    }
    
    public boolean isEnablePacketCompression() {
        return enablePacketCompression;
    }
    
    public void setEnablePacketCompression(boolean enablePacketCompression) {
        this.enablePacketCompression = enablePacketCompression;
    }
    
    public int getCompressionThreshold() {
        return compressionThreshold;
    }
    
    public void setCompressionThreshold(int compressionThreshold) {
        this.compressionThreshold = compressionThreshold;
    }
    
    public boolean isEnableDetailedLogging() {
        return enableDetailedLogging;
    }
    
    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
    }
    
    public boolean isEnableOptimizationOnJoin() {
        return enableOptimizationOnJoin;
    }
    
    public void setEnableOptimizationOnJoin(boolean enableOptimizationOnJoin) {
        this.enableOptimizationOnJoin = enableOptimizationOnJoin;
    }
    
        
    public boolean isEnableDdosProtection() {
        return enableDdosProtection;
    }
    
    public void setEnableDdosProtection(boolean enableDdosProtection) {
        this.enableDdosProtection = enableDdosProtection;
    }
    
    public int getConnectionThrottleTime() {
        return connectionThrottleTime;
    }
    
    public void setConnectionThrottleTime(int connectionThrottleTime) {
        this.connectionThrottleTime = connectionThrottleTime;
    }
    
    public int getMaxConnectionsPerIP() {
        return maxConnectionsPerIP;
    }
    
    public void setMaxConnectionsPerIP(int maxConnectionsPerIP) {
        this.maxConnectionsPerIP = maxConnectionsPerIP;
    }
    
    public int getLoginTimeout() {
        return loginTimeout;
    }
    
    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }
    
    public int getPacketSpamThreshold() {
        return packetSpamThreshold;
    }
    
    public void setPacketSpamThreshold(int packetSpamThreshold) {
        this.packetSpamThreshold = packetSpamThreshold;
    }
    
    public boolean isEnableConnectionRateLimit() {
        return enableConnectionRateLimit;
    }
    
    public void setEnableConnectionRateLimit(boolean enableConnectionRateLimit) {
        this.enableConnectionRateLimit = enableConnectionRateLimit;
    }
    
    public int getMaxGlobalConnections() {
        return maxGlobalConnections;
    }
    
    public void setMaxGlobalConnections(int maxGlobalConnections) {
        this.maxGlobalConnections = maxGlobalConnections;
    }
    
    public int getConnectionLimitPeriod() {
        return connectionLimitPeriod;
    }
    
    public void setConnectionLimitPeriod(int connectionLimitPeriod) {
        this.connectionLimitPeriod = connectionLimitPeriod;
    }
    
    public boolean isBlockProxiesAndVPNs() {
        return blockProxiesAndVPNs;
    }
    
    public void setBlockProxiesAndVPNs(boolean blockProxiesAndVPNs) {
        this.blockProxiesAndVPNs = blockProxiesAndVPNs;
    }
    
    public boolean isEnableGeoIPFiltering() {
        return enableGeoIPFiltering;
    }
    
    public void setEnableGeoIPFiltering(boolean enableGeoIPFiltering) {
        this.enableGeoIPFiltering = enableGeoIPFiltering;
    }
    
    public String[] getAllowedCountries() {
        return allowedCountries;
    }
    
    public void setAllowedCountries(String[] allowedCountries) {
        this.allowedCountries = allowedCountries;
    }
} 