package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ProtocolOptimizerSettings {

    private final UltimaCore plugin;
    private final ProtocolOptimizerModule module;
    private FileConfiguration config;
    
        private boolean enabled = true;
    private boolean debugMode = false;
    
        private boolean optimizeEntityPackets = true;
    private int entityViewDistance = 32;
    private int entityUpdateRate = 4;
    
        private boolean optimizeBlockPackets = true;
    private int maxQueuedBlockUpdates = 64;
    private int blockUpdateInterval = 2;
    
        private boolean optimizeMovementPackets = true;
    private int movementUpdateRate = 2;
    
        private boolean filterPackets = true;
    private List<String> blockedPacketTypes = new ArrayList<>();
    
        private boolean asyncProcessing = true;
    private int processingThreads = 2;
    
        private int initialPacketDelay = 20;     private int actionOptimizationInterval = 500;     private int viewDistance = 8;     
        private int statsCollectionInterval = 6000;     private int optimizationUpdateInterval = 1200;     
        private boolean compressPackets = true;
    private int compressionLevel = 6;
    
    
    public ProtocolOptimizerSettings(UltimaCore plugin, ProtocolOptimizerModule module) {
        this.plugin = plugin;
        this.module = module;
    }
    
    
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "protocol-optimizer.yml");
        
                if (!configFile.exists()) {
            plugin.saveResource("protocol-optimizer.yml", false);
        }
        
                config = YamlConfiguration.loadConfiguration(configFile);
        
                enabled = config.getBoolean("enabled", true);
        debugMode = config.getBoolean("debug-mode", false);
        
                ConfigurationSection entitySection = config.getConfigurationSection("entity-optimization");
        if (entitySection != null) {
            optimizeEntityPackets = entitySection.getBoolean("enabled", true);
            entityViewDistance = entitySection.getInt("view-distance", 32);
            entityUpdateRate = entitySection.getInt("update-rate", 4);
        }
        
                ConfigurationSection blockSection = config.getConfigurationSection("block-optimization");
        if (blockSection != null) {
            optimizeBlockPackets = blockSection.getBoolean("enabled", true);
            maxQueuedBlockUpdates = blockSection.getInt("max-queued-updates", 64);
            blockUpdateInterval = blockSection.getInt("update-interval", 2);
        }
        
                ConfigurationSection movementSection = config.getConfigurationSection("movement-optimization");
        if (movementSection != null) {
            optimizeMovementPackets = movementSection.getBoolean("enabled", true);
            movementUpdateRate = movementSection.getInt("update-rate", 2);
        }
        
                ConfigurationSection filterSection = config.getConfigurationSection("packet-filter");
        if (filterSection != null) {
            filterPackets = filterSection.getBoolean("enabled", true);
            blockedPacketTypes = filterSection.getStringList("blocked-types");
        }
        
                ConfigurationSection asyncSection = config.getConfigurationSection("async-processing");
        if (asyncSection != null) {
            asyncProcessing = asyncSection.getBoolean("enabled", true);
            processingThreads = asyncSection.getInt("threads", 2);
        }
        
                initialPacketDelay = config.getInt("initial-packet-delay", 20);
        actionOptimizationInterval = config.getInt("action-optimization-interval", 500);
        viewDistance = config.getInt("view-distance", 8);
        
                statsCollectionInterval = config.getInt("stats-collection-interval", 6000);
        optimizationUpdateInterval = config.getInt("optimization-update-interval", 1200);
        
                compressPackets = config.getBoolean("compress-packets", true);
        compressionLevel = config.getInt("compression-level", 6);
        
        LoggerUtil.info("&aНастройки модуля оптимизации протокола загружены");
    }
    
    
    public void saveConfig() {
                config.set("enabled", enabled);
        config.set("debug-mode", debugMode);
        
                ConfigurationSection entitySection = config.getConfigurationSection("entity-optimization");
        if (entitySection == null) {
            entitySection = config.createSection("entity-optimization");
        }
        entitySection.set("enabled", optimizeEntityPackets);
        entitySection.set("view-distance", entityViewDistance);
        entitySection.set("update-rate", entityUpdateRate);
        
                ConfigurationSection blockSection = config.getConfigurationSection("block-optimization");
        if (blockSection == null) {
            blockSection = config.createSection("block-optimization");
        }
        blockSection.set("enabled", optimizeBlockPackets);
        blockSection.set("max-queued-updates", maxQueuedBlockUpdates);
        blockSection.set("update-interval", blockUpdateInterval);
        
                ConfigurationSection movementSection = config.getConfigurationSection("movement-optimization");
        if (movementSection == null) {
            movementSection = config.createSection("movement-optimization");
        }
        movementSection.set("enabled", optimizeMovementPackets);
        movementSection.set("update-rate", movementUpdateRate);
        
                ConfigurationSection filterSection = config.getConfigurationSection("packet-filter");
        if (filterSection == null) {
            filterSection = config.createSection("packet-filter");
        }
        filterSection.set("enabled", filterPackets);
        filterSection.set("blocked-types", blockedPacketTypes);
        
                ConfigurationSection asyncSection = config.getConfigurationSection("async-processing");
        if (asyncSection == null) {
            asyncSection = config.createSection("async-processing");
        }
        asyncSection.set("enabled", asyncProcessing);
        asyncSection.set("threads", processingThreads);
        
                config.set("initial-packet-delay", initialPacketDelay);
        config.set("action-optimization-interval", actionOptimizationInterval);
        config.set("view-distance", viewDistance);
        
                config.set("stats-collection-interval", statsCollectionInterval);
        config.set("optimization-update-interval", optimizationUpdateInterval);
        
                config.set("compress-packets", compressPackets);
        config.set("compression-level", compressionLevel);
        
        try {
            config.save(new File(plugin.getDataFolder(), "protocol-optimizer.yml"));
            LoggerUtil.info("&aНастройки модуля оптимизации протокола сохранены");
        } catch (Exception e) {
            LoggerUtil.error("&cОшибка при сохранении настроек модуля оптимизации протокола: " + e.getMessage());
        }
    }
    
        
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    public boolean isOptimizeEntityPackets() {
        return optimizeEntityPackets;
    }
    
    public void setOptimizeEntityPackets(boolean optimizeEntityPackets) {
        this.optimizeEntityPackets = optimizeEntityPackets;
    }
    
    public int getEntityViewDistance() {
        return entityViewDistance;
    }
    
    public void setEntityViewDistance(int entityViewDistance) {
        this.entityViewDistance = entityViewDistance;
    }
    
    public int getEntityUpdateRate() {
        return entityUpdateRate;
    }
    
    public void setEntityUpdateRate(int entityUpdateRate) {
        this.entityUpdateRate = entityUpdateRate;
    }
    
    public boolean isOptimizeBlockPackets() {
        return optimizeBlockPackets;
    }
    
    public void setOptimizeBlockPackets(boolean optimizeBlockPackets) {
        this.optimizeBlockPackets = optimizeBlockPackets;
    }
    
    public int getMaxQueuedBlockUpdates() {
        return maxQueuedBlockUpdates;
    }
    
    public void setMaxQueuedBlockUpdates(int maxQueuedBlockUpdates) {
        this.maxQueuedBlockUpdates = maxQueuedBlockUpdates;
    }
    
    public int getBlockUpdateInterval() {
        return blockUpdateInterval;
    }
    
    public void setBlockUpdateInterval(int blockUpdateInterval) {
        this.blockUpdateInterval = blockUpdateInterval;
    }
    
    public boolean isOptimizeMovementPackets() {
        return optimizeMovementPackets;
    }
    
    public void setOptimizeMovementPackets(boolean optimizeMovementPackets) {
        this.optimizeMovementPackets = optimizeMovementPackets;
    }
    
    public int getMovementUpdateRate() {
        return movementUpdateRate;
    }
    
    public void setMovementUpdateRate(int movementUpdateRate) {
        this.movementUpdateRate = movementUpdateRate;
    }
    
    public boolean isFilterPackets() {
        return filterPackets;
    }
    
    public void setFilterPackets(boolean filterPackets) {
        this.filterPackets = filterPackets;
    }
    
    public List<String> getBlockedPacketTypes() {
        return blockedPacketTypes;
    }
    
    public void setBlockedPacketTypes(List<String> blockedPacketTypes) {
        this.blockedPacketTypes = blockedPacketTypes;
    }
    
    public boolean isAsyncProcessing() {
        return asyncProcessing;
    }
    
    public void setAsyncProcessing(boolean asyncProcessing) {
        this.asyncProcessing = asyncProcessing;
    }
    
    public int getProcessingThreads() {
        return processingThreads;
    }
    
    public void setProcessingThreads(int processingThreads) {
        this.processingThreads = processingThreads;
    }
    
    public int getInitialPacketDelay() {
        return initialPacketDelay;
    }
    
    public void setInitialPacketDelay(int initialPacketDelay) {
        this.initialPacketDelay = initialPacketDelay;
    }
    
    public int getActionOptimizationInterval() {
        return actionOptimizationInterval;
    }
    
    public void setActionOptimizationInterval(int actionOptimizationInterval) {
        this.actionOptimizationInterval = actionOptimizationInterval;
    }
    
    public int getViewDistance() {
        return viewDistance;
    }
    
    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }
    
    public int getStatsCollectionInterval() {
        return statsCollectionInterval;
    }
    
    public void setStatsCollectionInterval(int statsCollectionInterval) {
        this.statsCollectionInterval = statsCollectionInterval;
    }
    
    public int getOptimizationUpdateInterval() {
        return optimizationUpdateInterval;
    }
    
    public void setOptimizationUpdateInterval(int optimizationUpdateInterval) {
        this.optimizationUpdateInterval = optimizationUpdateInterval;
    }
    
    
    public int getCompressionLevel() {
        return compressionLevel;
    }
    
    
    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel >= 0 && compressionLevel <= 9) {
            this.compressionLevel = compressionLevel;
        }
    }
    
    
    public int getEntityUpdateInterval() {
        return entityUpdateRate * 50;     }
    
    
    public boolean isEnableEntityPacketOptimization() {
        return optimizeEntityPackets;
    }

    
    public boolean isEnableBlockUpdateOptimization() {
        return optimizeBlockPackets;
    }

    
    public int getMaxBlockUpdatesPerTick() {
        return maxQueuedBlockUpdates / 2;     }

    
    public int getBatchSize() {
        return Math.max(16, maxQueuedBlockUpdates / 4);     }

    
    public int getBlockViewDistance() {
        return viewDistance;
    }

    
    public boolean isCompressPackets() {
        return compressPackets;
    }

    
    public void setCompressPackets(boolean compressPackets) {
        this.compressPackets = compressPackets;
    }

    
    public boolean isDelayInitialPackets() {
        return initialPacketDelay > 0;
    }

    
    public boolean isOptimizeChunkPackets() {
        return optimizeBlockPackets;
    }

    
    public boolean isOptimizeChunkLoadOnTeleport() {
        return optimizeBlockPackets;
    }

    
    public boolean isOptimizeActionPackets() {
        return true;     }
} 