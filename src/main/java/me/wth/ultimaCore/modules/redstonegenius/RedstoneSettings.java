package me.wth.ultimaCore.modules.redstonegenius;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RedstoneSettings {

        private static final int DEFAULT_MAX_ACTIVATIONS_PER_BLOCK = 20;
    private static final int DEFAULT_MAX_ACTIVATIONS_PER_CHUNK = 200;
    private static final int DEFAULT_MAX_CLOCK_FREQUENCY = 10;
    private static final int DEFAULT_THROTTLE_DURATION = 5;     private static final boolean DEFAULT_DISABLE_CLOCKS = true;
    private static final boolean DEFAULT_OPTIMIZE_UPDATES = true;
    private static final boolean DEFAULT_THROTTLING_ENABLED = true;
    private static final boolean DEFAULT_CLOCK_DETECTION_ENABLED = true;
    private static final List<String> DEFAULT_IGNORED_MATERIALS = Arrays.asList(
            "LEVER", "STONE_BUTTON", "WOODEN_BUTTON", "ACACIA_BUTTON", 
            "BIRCH_BUTTON", "DARK_OAK_BUTTON", "JUNGLE_BUTTON", "OAK_BUTTON", 
            "SPRUCE_BUTTON", "CRIMSON_BUTTON", "WARPED_BUTTON"
    );
    
        private int maxActivationsPerBlock;
    private int maxActivationsPerChunk;
    private int maxClockFrequency;
    private int throttleDuration;
    private boolean disableClocks;
    private boolean optimizeUpdates;
    private boolean throttlingEnabled;
    private boolean clockDetectionEnabled;
    private final Set<Material> ignoredMaterials = new HashSet<>();
    
    
    public RedstoneSettings() {
        this.maxActivationsPerBlock = DEFAULT_MAX_ACTIVATIONS_PER_BLOCK;
        this.maxActivationsPerChunk = DEFAULT_MAX_ACTIVATIONS_PER_CHUNK;
        this.maxClockFrequency = DEFAULT_MAX_CLOCK_FREQUENCY;
        this.throttleDuration = DEFAULT_THROTTLE_DURATION;
        this.disableClocks = DEFAULT_DISABLE_CLOCKS;
        this.optimizeUpdates = DEFAULT_OPTIMIZE_UPDATES;
        this.throttlingEnabled = DEFAULT_THROTTLING_ENABLED;
        this.clockDetectionEnabled = DEFAULT_CLOCK_DETECTION_ENABLED;
        
                for (String materialName : DEFAULT_IGNORED_MATERIALS) {
            try {
                Material material = Material.valueOf(materialName);
                ignoredMaterials.add(material);
            } catch (IllegalArgumentException e) {
                            }
        }
    }
    
    
    public void loadFromConfig(ConfigurationSection config) {
        if (config == null) return;
        
        maxActivationsPerBlock = config.getInt("maxActivationsPerBlock", DEFAULT_MAX_ACTIVATIONS_PER_BLOCK);
        maxActivationsPerChunk = config.getInt("maxActivationsPerChunk", DEFAULT_MAX_ACTIVATIONS_PER_CHUNK);
        maxClockFrequency = config.getInt("maxClockFrequency", DEFAULT_MAX_CLOCK_FREQUENCY);
        throttleDuration = config.getInt("throttleDuration", DEFAULT_THROTTLE_DURATION);
        disableClocks = config.getBoolean("disableClocks", DEFAULT_DISABLE_CLOCKS);
        optimizeUpdates = config.getBoolean("optimizeUpdates", DEFAULT_OPTIMIZE_UPDATES);
        throttlingEnabled = config.getBoolean("throttlingEnabled", DEFAULT_THROTTLING_ENABLED);
        clockDetectionEnabled = config.getBoolean("clockDetectionEnabled", DEFAULT_CLOCK_DETECTION_ENABLED);
        
                ignoredMaterials.clear();
        List<String> ignoredMaterialNames = config.getStringList("ignoredMaterials");
        if (ignoredMaterialNames.isEmpty()) {
            ignoredMaterialNames = DEFAULT_IGNORED_MATERIALS;
        }
        
        for (String materialName : ignoredMaterialNames) {
            try {
                Material material = Material.valueOf(materialName);
                ignoredMaterials.add(material);
            } catch (IllegalArgumentException e) {
                            }
        }
    }
    
    
    public void saveToConfig(ConfigurationSection config) {
        if (config == null) return;
        
        config.set("maxActivationsPerBlock", maxActivationsPerBlock);
        config.set("maxActivationsPerChunk", maxActivationsPerChunk);
        config.set("maxClockFrequency", maxClockFrequency);
        config.set("throttleDuration", throttleDuration);
        config.set("disableClocks", disableClocks);
        config.set("optimizeUpdates", optimizeUpdates);
        config.set("throttlingEnabled", throttlingEnabled);
        config.set("clockDetectionEnabled", clockDetectionEnabled);
        
                List<String> ignoredMaterialNames = ignoredMaterials.stream()
                .map(Material::name)
                .toList();
        config.set("ignoredMaterials", ignoredMaterialNames);
    }
    
    
    public int getMaxActivationsPerBlock() {
        return maxActivationsPerBlock;
    }
    
    
    public void setMaxActivationsPerBlock(int maxActivationsPerBlock) {
        this.maxActivationsPerBlock = maxActivationsPerBlock;
    }
    
    
    public int getMaxActivationsPerChunk() {
        return maxActivationsPerChunk;
    }
    
    
    public void setMaxActivationsPerChunk(int maxActivationsPerChunk) {
        this.maxActivationsPerChunk = maxActivationsPerChunk;
    }
    
    
    public int getMaxClockFrequency() {
        return maxClockFrequency;
    }
    
    
    public void setMaxClockFrequency(int maxClockFrequency) {
        this.maxClockFrequency = maxClockFrequency;
    }
    
    
    public int getThrottleDuration() {
        return throttleDuration;
    }
    
    
    public void setThrottleDuration(int throttleDuration) {
        this.throttleDuration = throttleDuration;
    }
    
    
    public boolean isDisableClocks() {
        return disableClocks;
    }
    
    
    public void setDisableClocks(boolean disableClocks) {
        this.disableClocks = disableClocks;
    }
    
    
    public boolean isOptimizeUpdates() {
        return optimizeUpdates;
    }
    
    
    public void setOptimizeUpdates(boolean optimizeUpdates) {
        this.optimizeUpdates = optimizeUpdates;
    }
    
    
    public boolean isThrottlingEnabled() {
        return throttlingEnabled;
    }
    
    
    public void setThrottlingEnabled(boolean throttlingEnabled) {
        this.throttlingEnabled = throttlingEnabled;
    }
    
    
    public boolean isClockDetectionEnabled() {
        return clockDetectionEnabled;
    }
    
    
    public void setClockDetectionEnabled(boolean clockDetectionEnabled) {
        this.clockDetectionEnabled = clockDetectionEnabled;
    }
    
    
    public boolean isAutoDisableClocks() {
        return disableClocks;
    }
    
    
    public boolean isMaterialIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }
    
    
    public boolean isMaterialIgnored(String material) {
        return ignoredMaterials.contains(material.toUpperCase());
    }
    
    
    public void addIgnoredMaterial(Material material) {
        ignoredMaterials.add(material);
    }
    
    
    public void removeIgnoredMaterial(Material material) {
        ignoredMaterials.remove(material);
    }
    
    
    public Set<Material> getIgnoredMaterials() {
        return new HashSet<>(ignoredMaterials);
    }
}