package me.wth.ultimaCore.modules.memoryguard;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MemoryGuardSettings {
        private double noticeThreshold = 70.0;        private double warningThreshold = 85.0;       private double criticalThreshold = 93.0;      
        private int checkInterval = 15;               private int kickPlayersLimit = 5;             
        private boolean enableDetailedLogging = false;        private boolean enablePlayerWarnings = true;          private boolean enableKickOnCritical = false;         
        private Set<String> disabledWorldsItems = new HashSet<>();          private Set<String> disabledWorldsEntities = new HashSet<>();       private Set<String> disabledWorldsChunks = new HashSet<>();         
        private Set<EntityType> removableEntityTypes = new HashSet<>();
    
    
    public MemoryGuardSettings() {
                initDefaultRemovableEntityTypes();
    }
    
    
    private void initDefaultRemovableEntityTypes() {
                removableEntityTypes.add(EntityType.ITEM);
        removableEntityTypes.add(EntityType.EXPERIENCE_ORB);
        removableEntityTypes.add(EntityType.ARROW);
        removableEntityTypes.add(EntityType.SNOWBALL);
        removableEntityTypes.add(EntityType.FIREBALL);
        removableEntityTypes.add(EntityType.SMALL_FIREBALL);
        removableEntityTypes.add(EntityType.ENDER_PEARL);
        removableEntityTypes.add(EntityType.POTION);
        removableEntityTypes.add(EntityType.EXPERIENCE_BOTTLE);
        removableEntityTypes.add(EntityType.FALLING_BLOCK);
        removableEntityTypes.add(EntityType.TNT);
    }
    
    
    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;
        
                this.noticeThreshold = section.getDouble("noticeThreshold", noticeThreshold);
        this.warningThreshold = section.getDouble("warningThreshold", warningThreshold);
        this.criticalThreshold = section.getDouble("criticalThreshold", criticalThreshold);
        
                this.checkInterval = section.getInt("checkInterval", checkInterval);
        this.kickPlayersLimit = section.getInt("kickPlayersLimit", kickPlayersLimit);
        
                this.enableDetailedLogging = section.getBoolean("enableDetailedLogging", enableDetailedLogging);
        this.enablePlayerWarnings = section.getBoolean("enablePlayerWarnings", enablePlayerWarnings);
        this.enableKickOnCritical = section.getBoolean("enableKickOnCritical", enableKickOnCritical);
        
                if (section.contains("disabledWorldsItems")) {
            this.disabledWorldsItems = new HashSet<>(section.getStringList("disabledWorldsItems"));
        }
        
        if (section.contains("disabledWorldsEntities")) {
            this.disabledWorldsEntities = new HashSet<>(section.getStringList("disabledWorldsEntities"));
        }
        
        if (section.contains("disabledWorldsChunks")) {
            this.disabledWorldsChunks = new HashSet<>(section.getStringList("disabledWorldsChunks"));
        }
        
                if (section.contains("removableEntityTypes")) {
            List<String> entityTypeNames = section.getStringList("removableEntityTypes");
            this.removableEntityTypes = new HashSet<>();
            
            for (String typeName : entityTypeNames) {
                try {
                    EntityType type = EntityType.valueOf(typeName);
                    this.removableEntityTypes.add(type);
                } catch (IllegalArgumentException e) {
                                    }
            }
        }
    }
    
    
    public void saveToConfig(ConfigurationSection section) {
        if (section == null) return;
        
                section.set("noticeThreshold", noticeThreshold);
        section.set("warningThreshold", warningThreshold);
        section.set("criticalThreshold", criticalThreshold);
        
                section.set("checkInterval", checkInterval);
        section.set("kickPlayersLimit", kickPlayersLimit);
        
                section.set("enableDetailedLogging", enableDetailedLogging);
        section.set("enablePlayerWarnings", enablePlayerWarnings);
        section.set("enableKickOnCritical", enableKickOnCritical);
        
                section.set("disabledWorldsItems", new ArrayList<>(disabledWorldsItems));
        section.set("disabledWorldsEntities", new ArrayList<>(disabledWorldsEntities));
        section.set("disabledWorldsChunks", new ArrayList<>(disabledWorldsChunks));
        
                List<String> entityTypeNames = new ArrayList<>();
        for (EntityType type : removableEntityTypes) {
            entityTypeNames.add(type.name());
        }
        section.set("removableEntityTypes", entityTypeNames);
    }
    
        
    public double getNoticeThreshold() {
        return noticeThreshold;
    }
    
    public void setNoticeThreshold(double noticeThreshold) {
        this.noticeThreshold = noticeThreshold;
    }
    
    public double getWarningThreshold() {
        return warningThreshold;
    }
    
    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }
    
    public double getCriticalThreshold() {
        return criticalThreshold;
    }
    
    public void setCriticalThreshold(double criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }
    
    public int getCheckInterval() {
        return checkInterval;
    }
    
    public void setCheckInterval(int checkInterval) {
        this.checkInterval = checkInterval;
    }
    
    public int getKickPlayersLimit() {
        return kickPlayersLimit;
    }
    
    public void setKickPlayersLimit(int kickPlayersLimit) {
        this.kickPlayersLimit = kickPlayersLimit;
    }
    
    public boolean isEnableDetailedLogging() {
        return enableDetailedLogging;
    }
    
    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
    }
    
    public boolean isEnablePlayerWarnings() {
        return enablePlayerWarnings;
    }
    
    public void setEnablePlayerWarnings(boolean enablePlayerWarnings) {
        this.enablePlayerWarnings = enablePlayerWarnings;
    }
    
    public boolean isEnableKickOnCritical() {
        return enableKickOnCritical;
    }
    
    public void setEnableKickOnCritical(boolean enableKickOnCritical) {
        this.enableKickOnCritical = enableKickOnCritical;
    }
    
    
    public boolean isItemsRemovalEnabled(String worldName) {
        return !disabledWorldsItems.contains(worldName);
    }
    
    
    public void setItemsRemovalEnabled(String worldName, boolean enabled) {
        if (enabled) {
            disabledWorldsItems.remove(worldName);
        } else {
            disabledWorldsItems.add(worldName);
        }
    }
    
    
    public boolean isEntityRemovalEnabled(String worldName) {
        return !disabledWorldsEntities.contains(worldName);
    }
    
    
    public void setEntityRemovalEnabled(String worldName, boolean enabled) {
        if (enabled) {
            disabledWorldsEntities.remove(worldName);
        } else {
            disabledWorldsEntities.add(worldName);
        }
    }
    
    
    public boolean isChunkUnloadEnabled(String worldName) {
        return !disabledWorldsChunks.contains(worldName);
    }
    
    
    public void setChunkUnloadEnabled(String worldName, boolean enabled) {
        if (enabled) {
            disabledWorldsChunks.remove(worldName);
        } else {
            disabledWorldsChunks.add(worldName);
        }
    }
    
    
    public boolean isEntityTypeRemovable(EntityType entityType) {
        return removableEntityTypes.contains(entityType);
    }
    
    
    public void addRemovableEntityType(EntityType entityType) {
        removableEntityTypes.add(entityType);
    }
    
    
    public void removeRemovableEntityType(EntityType entityType) {
        removableEntityTypes.remove(entityType);
    }
    
    
    public Set<EntityType> getRemovableEntityTypes() {
        return new HashSet<>(removableEntityTypes);
    }
} 