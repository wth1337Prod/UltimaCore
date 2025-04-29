package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class EntityUpdateOptimizer {
    private final ProtocolOptimizerModule module;
    
        private final Map<Integer, Location> entityLocations = new ConcurrentHashMap<>();
    
        private final Map<Integer, Map<UUID, Long>> lastUpdateTimes = new ConcurrentHashMap<>();
    
        private final Map<UUID, Map<Integer, Double>> entityDistances = new ConcurrentHashMap<>();
    
        private final Set<Integer> priorityEntities = new HashSet<>();
    
        private int skippedUpdates = 0;
    private int processedUpdates = 0;
    
    
    public EntityUpdateOptimizer(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    public void processTick() {
                long currentTick = getCurrentTick();
        if (currentTick % 5 == 0) {
            updateEntityDistances();
        }
    }
    
    
    private long getCurrentTick() {
                return System.currentTimeMillis() / 50;
    }
    
    
    private void updateEntityDistances() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location playerLocation = player.getLocation();
            UUID playerUUID = player.getUniqueId();
            Map<Integer, Double> distances = entityDistances.computeIfAbsent(
                    playerUUID, k -> new HashMap<>());
            
                        distances.clear();
            
                        for (Entity entity : player.getNearbyEntities(100, 100, 100)) {
                int entityId = entity.getEntityId();
                double distance = playerLocation.distance(entity.getLocation());
                distances.put(entityId, distance);
                
                                entityLocations.put(entityId, entity.getLocation().clone());
            }
        }
    }
    
    
    public void optimizeUpdates() {
                        identifyPriorityEntities();
        cleanupStaleData();
    }
    
    
    private void identifyPriorityEntities() {
        priorityEntities.clear();
        
        for (Entity entity : Bukkit.getWorlds().get(0).getEntities()) {
            int entityId = entity.getEntityId();
            
                        Location currentLocation = entity.getLocation();
            Location lastLocation = entityLocations.get(entityId);
            
            if (lastLocation != null) {
                double distance = currentLocation.distance(lastLocation);
                
                                if (distance > 0.5) {
                    priorityEntities.add(entityId);
                }
            }
            
                        entityLocations.put(entityId, currentLocation.clone());
        }
    }
    
    
    private void cleanupStaleData() {
        long now = System.currentTimeMillis();
        
                entityLocations.entrySet().removeIf(entry -> {
            Map<UUID, Long> updates = lastUpdateTimes.get(entry.getKey());
            if (updates == null || updates.isEmpty()) {
                return true;
            }
            
                        for (Long time : updates.values()) {
                if (now - time < 30000) {
                    return false;
                }
            }
            
            return true;
        });
        
                entityDistances.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }
    
    
    public boolean shouldUpdateEntity(int entityId, UUID playerUUID) {
                if (priorityEntities.contains(entityId)) {
            return true;
        }
        
                Map<Integer, Double> distances = entityDistances.get(playerUUID);
        if (distances == null) {
            return true;         }
        
        Double distance = distances.get(entityId);
        if (distance == null) {
            return true;         }
        
                Map<UUID, Long> updates = lastUpdateTimes.computeIfAbsent(
                entityId, k -> new ConcurrentHashMap<>());
        Long lastUpdate = updates.get(playerUUID);
        
        long now = System.currentTimeMillis();
        
                if (lastUpdate == null) {
            updates.put(playerUUID, now);
            return true;
        }
        
                long updateInterval;
        
        if (distance <= 10) {
                        updateInterval = 50;
        } else if (distance <= 20) {
                        updateInterval = 250;
        } else if (distance <= getEntityViewDistance()) {
                        updateInterval = 500;
        } else {
                        updateInterval = 1000;
        }
        
                if (now - lastUpdate >= updateInterval) {
            updates.put(playerUUID, now);
            processedUpdates++;
            return true;
        } else {
            skippedUpdates++;
            return false;
        }
    }
    
    
    public void markEntityAsPriority(int entityId) {
        priorityEntities.add(entityId);
    }
    
    
    public void clearPlayerData(UUID playerUUID) {
        entityDistances.remove(playerUUID);
        
        for (Map<UUID, Long> updates : lastUpdateTimes.values()) {
            updates.remove(playerUUID);
        }
    }
    
    
    public int getSkippedUpdates() {
        return skippedUpdates;
    }
    
    
    public int getProcessedUpdates() {
        return processedUpdates;
    }
    
    
    public double getOptimizationPercentage() {
        int total = skippedUpdates + processedUpdates;
        if (total == 0) {
            return 0.0;
        }
        
        return 100.0 * skippedUpdates / total;
    }
    
    
    private int getEntityViewDistance() {
                if (module != null && module.getSettings() != null) {
            return module.getSettings().getEntityViewDistance();
        }
        return 32;     }
} 