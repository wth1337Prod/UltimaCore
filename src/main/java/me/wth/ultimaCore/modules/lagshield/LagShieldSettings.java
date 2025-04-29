package me.wth.ultimaCore.modules.lagshield;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class LagShieldSettings {
        private static final boolean DEFAULT_AUTO_CLEANUP_ENABLED = true;
    private static final int DEFAULT_CLEANUP_INTERVAL = 10;     private static final double DEFAULT_CLEANUP_TPS_THRESHOLD = 18.0;
    private static final boolean DEFAULT_CLEAN_ENTITIES_ENABLED = true;
    private static final boolean DEFAULT_UNLOAD_CHUNKS_ENABLED = true;
    private static final int DEFAULT_CHUNK_UNLOAD_TIME = 5;     private static final int DEFAULT_MAX_ENTITIES_PER_CHUNK = 30;
    private static final int DEFAULT_MAX_TILE_ENTITIES_PER_CHUNK = 20;
    private static final int DEFAULT_ENTITY_LIMIT_PER_TYPE = 150;
    private static final boolean DEFAULT_MEMORY_PROTECTION_ENABLED = true;
    private static final int DEFAULT_MEMORY_THRESHOLD = 85;     private static final boolean DEFAULT_ANTIFARM_PROTECTION = true;
    private static final int DEFAULT_MAX_FALLING_BLOCKS = 20;
    private static final int DEFAULT_MAX_TNT_DETONATIONS = 20;
    private static final List<String> DEFAULT_PROTECTED_ENTITY_TYPES = Arrays.asList(
            "VILLAGER", "IRON_GOLEM", "ARMOR_STAND", "MINECART", "BOAT"
    );
    
        private boolean autoCleanupEnabled;
    private int cleanupInterval;
    private double cleanupTpsThreshold;
    private boolean cleanEntitiesEnabled;
    private boolean unloadChunksEnabled;
    private int chunkUnloadTime;
    private int maxEntitiesPerChunk;
    private int maxTileEntitiesPerChunk;
    private int entityLimitPerType;
    private boolean memoryProtectionEnabled;
    private int memoryThreshold;
    private boolean antifarmProtection;
    private int maxFallingBlocks;
    private int maxTntDetonations;
    private final Set<EntityType> protectedEntityTypes = new HashSet<>();
    
    
    public LagShieldSettings() {
        this.autoCleanupEnabled = DEFAULT_AUTO_CLEANUP_ENABLED;
        this.cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
        this.cleanupTpsThreshold = DEFAULT_CLEANUP_TPS_THRESHOLD;
        this.cleanEntitiesEnabled = DEFAULT_CLEAN_ENTITIES_ENABLED;
        this.unloadChunksEnabled = DEFAULT_UNLOAD_CHUNKS_ENABLED;
        this.chunkUnloadTime = DEFAULT_CHUNK_UNLOAD_TIME;
        this.maxEntitiesPerChunk = DEFAULT_MAX_ENTITIES_PER_CHUNK;
        this.maxTileEntitiesPerChunk = DEFAULT_MAX_TILE_ENTITIES_PER_CHUNK;
        this.entityLimitPerType = DEFAULT_ENTITY_LIMIT_PER_TYPE;
        this.memoryProtectionEnabled = DEFAULT_MEMORY_PROTECTION_ENABLED;
        this.memoryThreshold = DEFAULT_MEMORY_THRESHOLD;
        this.antifarmProtection = DEFAULT_ANTIFARM_PROTECTION;
        this.maxFallingBlocks = DEFAULT_MAX_FALLING_BLOCKS;
        this.maxTntDetonations = DEFAULT_MAX_TNT_DETONATIONS;
        
                for (String entityTypeName : DEFAULT_PROTECTED_ENTITY_TYPES) {
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName);
                protectedEntityTypes.add(entityType);
            } catch (IllegalArgumentException e) {
                            }
        }
    }
    
    
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        autoCleanupEnabled = config.getBoolean("autoCleanupEnabled", DEFAULT_AUTO_CLEANUP_ENABLED);
        cleanupInterval = config.getInt("cleanupInterval", DEFAULT_CLEANUP_INTERVAL);
        cleanupTpsThreshold = config.getDouble("cleanupTpsThreshold", DEFAULT_CLEANUP_TPS_THRESHOLD);
        cleanEntitiesEnabled = config.getBoolean("cleanEntitiesEnabled", DEFAULT_CLEAN_ENTITIES_ENABLED);
        unloadChunksEnabled = config.getBoolean("unloadChunksEnabled", DEFAULT_UNLOAD_CHUNKS_ENABLED);
        chunkUnloadTime = config.getInt("chunkUnloadTime", DEFAULT_CHUNK_UNLOAD_TIME);
        maxEntitiesPerChunk = config.getInt("maxEntitiesPerChunk", DEFAULT_MAX_ENTITIES_PER_CHUNK);
        maxTileEntitiesPerChunk = config.getInt("maxTileEntitiesPerChunk", DEFAULT_MAX_TILE_ENTITIES_PER_CHUNK);
        entityLimitPerType = config.getInt("entityLimitPerType", DEFAULT_ENTITY_LIMIT_PER_TYPE);
        memoryProtectionEnabled = config.getBoolean("memoryProtectionEnabled", DEFAULT_MEMORY_PROTECTION_ENABLED);
        memoryThreshold = config.getInt("memoryThreshold", DEFAULT_MEMORY_THRESHOLD);
        antifarmProtection = config.getBoolean("antifarmProtection", DEFAULT_ANTIFARM_PROTECTION);
        maxFallingBlocks = config.getInt("maxFallingBlocks", DEFAULT_MAX_FALLING_BLOCKS);
        maxTntDetonations = config.getInt("maxTntDetonations", DEFAULT_MAX_TNT_DETONATIONS);
        
                protectedEntityTypes.clear();
        List<String> protectedEntityTypeNames = config.getStringList("protectedEntityTypes");
        if (protectedEntityTypeNames.isEmpty()) {
            protectedEntityTypeNames = DEFAULT_PROTECTED_ENTITY_TYPES;
        }
        
        for (String entityTypeName : protectedEntityTypeNames) {
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName);
                protectedEntityTypes.add(entityType);
            } catch (IllegalArgumentException e) {
                            }
        }
    }
    
    
    public void saveToConfig(ConfigurationSection config) {
        if (config == null) return;
        
        config.set("autoCleanupEnabled", autoCleanupEnabled);
        config.set("cleanupInterval", cleanupInterval);
        config.set("cleanupTpsThreshold", cleanupTpsThreshold);
        config.set("cleanEntitiesEnabled", cleanEntitiesEnabled);
        config.set("unloadChunksEnabled", unloadChunksEnabled);
        config.set("chunkUnloadTime", chunkUnloadTime);
        config.set("maxEntitiesPerChunk", maxEntitiesPerChunk);
        config.set("maxTileEntitiesPerChunk", maxTileEntitiesPerChunk);
        config.set("entityLimitPerType", entityLimitPerType);
        config.set("memoryProtectionEnabled", memoryProtectionEnabled);
        config.set("memoryThreshold", memoryThreshold);
        config.set("antifarmProtection", antifarmProtection);
        config.set("maxFallingBlocks", maxFallingBlocks);
        config.set("maxTntDetonations", maxTntDetonations);
        
                List<String> protectedEntityTypeNames = new ArrayList<>();
        for (EntityType entityType : protectedEntityTypes) {
            protectedEntityTypeNames.add(entityType.name());
        }
        config.set("protectedEntityTypes", protectedEntityTypeNames);
    }
    
    
    public boolean isAutoCleanupEnabled() {
        return autoCleanupEnabled;
    }
    
    
    public void setAutoCleanupEnabled(boolean autoCleanupEnabled) {
        this.autoCleanupEnabled = autoCleanupEnabled;
    }
    
    
    public int getCleanupInterval() {
        return cleanupInterval;
    }
    
    
    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
    
    
    public double getCleanupTpsThreshold() {
        return cleanupTpsThreshold;
    }
    
    
    public void setCleanupTpsThreshold(double cleanupTpsThreshold) {
        this.cleanupTpsThreshold = cleanupTpsThreshold;
    }
    
    
    public boolean isCleanEntitiesEnabled() {
        return cleanEntitiesEnabled;
    }
    
    
    public void setCleanEntitiesEnabled(boolean cleanEntitiesEnabled) {
        this.cleanEntitiesEnabled = cleanEntitiesEnabled;
    }
    
    
    public boolean isUnloadChunksEnabled() {
        return unloadChunksEnabled;
    }
    
    
    public void setUnloadChunksEnabled(boolean unloadChunksEnabled) {
        this.unloadChunksEnabled = unloadChunksEnabled;
    }
    
    
    public int getChunkUnloadTime() {
        return chunkUnloadTime;
    }
    
    
    public void setChunkUnloadTime(int chunkUnloadTime) {
        this.chunkUnloadTime = chunkUnloadTime;
    }
    
    
    public int getMaxEntitiesPerChunk() {
        return maxEntitiesPerChunk;
    }
    
    
    public void setMaxEntitiesPerChunk(int maxEntitiesPerChunk) {
        this.maxEntitiesPerChunk = maxEntitiesPerChunk;
    }
    
    
    public int getMaxTileEntitiesPerChunk() {
        return maxTileEntitiesPerChunk;
    }
    
    
    public void setMaxTileEntitiesPerChunk(int maxTileEntitiesPerChunk) {
        this.maxTileEntitiesPerChunk = maxTileEntitiesPerChunk;
    }
    
    
    public int getEntityLimitPerType() {
        return entityLimitPerType;
    }
    
    
    public void setEntityLimitPerType(int entityLimitPerType) {
        this.entityLimitPerType = entityLimitPerType;
    }
    
    
    public boolean isMemoryProtectionEnabled() {
        return memoryProtectionEnabled;
    }
    
    
    public void setMemoryProtectionEnabled(boolean memoryProtectionEnabled) {
        this.memoryProtectionEnabled = memoryProtectionEnabled;
    }
    
    
    public int getMemoryThreshold() {
        return memoryThreshold;
    }
    
    
    public void setMemoryThreshold(int memoryThreshold) {
        this.memoryThreshold = memoryThreshold;
    }
    
    
    public boolean isAntifarmProtection() {
        return antifarmProtection;
    }
    
    
    public void setAntifarmProtection(boolean antifarmProtection) {
        this.antifarmProtection = antifarmProtection;
    }
    
    
    public int getMaxFallingBlocks() {
        return maxFallingBlocks;
    }
    
    
    public void setMaxFallingBlocks(int maxFallingBlocks) {
        this.maxFallingBlocks = maxFallingBlocks;
    }
    
    
    public int getMaxTntDetonations() {
        return maxTntDetonations;
    }
    
    
    public void setMaxTntDetonations(int maxTntDetonations) {
        this.maxTntDetonations = maxTntDetonations;
    }
    
    
    public boolean isEntityTypeProtected(EntityType entityType) {
        return protectedEntityTypes.contains(entityType);
    }
    
    
    public void addProtectedEntityType(EntityType entityType) {
        protectedEntityTypes.add(entityType);
    }
    
    
    public void removeProtectedEntityType(EntityType entityType) {
        protectedEntityTypes.remove(entityType);
    }
    
    
    public Set<EntityType> getProtectedEntityTypes() {
        return new HashSet<>(protectedEntityTypes);
    }
} 