package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class BlockUpdateOptimizer {
    private final ProtocolOptimizerModule module;
    private final Map<Player, Set<Location>> pendingBlockUpdates;
    private final Map<Player, Long> lastUpdateTime;
    
    
    public BlockUpdateOptimizer(ProtocolOptimizerModule module) {
        this.module = module;
        this.pendingBlockUpdates = new ConcurrentHashMap<>();
        this.lastUpdateTime = new ConcurrentHashMap<>();
    }
    
    
    public void processQueue() {
        if (!module.getSettings().isEnableBlockUpdateOptimization()) {
            return;
        }
        
        for (Player player : new ArrayList<>(pendingBlockUpdates.keySet())) {
            if (!player.isOnline()) {
                pendingBlockUpdates.remove(player);
                lastUpdateTime.remove(player);
                continue;
            }
            
            Set<Location> updates = pendingBlockUpdates.get(player);
            if (updates == null || updates.isEmpty()) {
                pendingBlockUpdates.remove(player);
                continue;
            }
            
            int maxUpdatesPerTick = module.getSettings().getMaxBlockUpdatesPerTick();
            int batchSize = module.getSettings().getBatchSize();
            int processed = 0;
            
                        List<Location> batch = new ArrayList<>();
            Iterator<Location> iterator = updates.iterator();
            
            while (iterator.hasNext() && processed < maxUpdatesPerTick) {
                batch.add(iterator.next());
                iterator.remove();
                processed++;
                
                                if (batch.size() >= batchSize || !iterator.hasNext()) {
                    sendBlockUpdateBatch(player, batch);
                    batch.clear();
                }
            }
            
                        if (updates.isEmpty()) {
                pendingBlockUpdates.remove(player);
            }
            
                        lastUpdateTime.put(player, System.currentTimeMillis());
        }
    }
    
    
    public void queueBlockUpdate(Player player, Block block) {
        if (!module.getSettings().isEnableBlockUpdateOptimization() || player == null || block == null) {
            return;
        }
        
                if (!isWithinViewDistance(player, block.getLocation())) {
            return;
        }
        
                Set<Location> updates = pendingBlockUpdates.computeIfAbsent(player, k -> new HashSet<>());
        
                if (updates.size() >= module.getSettings().getMaxQueuedBlockUpdates()) {
                        sendBlockUpdateBatch(player, new ArrayList<>(updates));
            updates.clear();
        }
        
                updates.add(block.getLocation());
        
                lastUpdateTime.put(player, System.currentTimeMillis());
    }
    
    
    private void sendBlockUpdateBatch(Player player, List<Location> locations) {
        if (player == null || !player.isOnline() || locations.isEmpty()) {
            return;
        }
        
        try {
            for (Location location : locations) {
                player.sendBlockChange(location, location.getBlock().getBlockData());
            }
            
                        if (locations.size() > 50) {
                LoggerUtil.debug("[ProtocolOptimizer] Отправлено " + locations.size() + 
                        " обновлений блоков игроку " + player.getName());
            }
        } catch (Exception e) {
            LoggerUtil.warning("&c[ProtocolOptimizer] Ошибка при отправке обновлений блоков: " + e.getMessage());
        }
    }
    
    
    private boolean isWithinViewDistance(Player player, Location location) {
        if (player == null || location == null || player.getWorld() != location.getWorld()) {
            return false;
        }
        
        int viewDistance = module.getSettings().getViewDistance() * 16;         double distanceSquared = player.getLocation().distanceSquared(location);
        
        return distanceSquared <= (viewDistance * viewDistance);
    }
    
    
    public void clearUpdatesForPlayer(Player player) {
        if (player != null) {
            pendingBlockUpdates.remove(player);
            lastUpdateTime.remove(player);
        }
    }
    
    
    public void clearAllUpdates() {
        pendingBlockUpdates.clear();
        lastUpdateTime.clear();
    }
    
    
    public int getPendingUpdateCount(Player player) {
        if (player == null || !pendingBlockUpdates.containsKey(player)) {
            return 0;
        }
        
        return pendingBlockUpdates.get(player).size();
    }
    
    
    public int getTotalPendingUpdateCount() {
        int total = 0;
        
        for (Set<Location> updates : pendingBlockUpdates.values()) {
            total += updates.size();
        }
        
        return total;
    }
    
    
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("enabled", module.getSettings().isEnableBlockUpdateOptimization());
        stats.put("totalPendingUpdates", getTotalPendingUpdateCount());
        stats.put("playersInQueue", pendingBlockUpdates.size());
        stats.put("maxQueuedUpdates", module.getSettings().getMaxQueuedBlockUpdates());
        stats.put("maxUpdatesPerTick", module.getSettings().getMaxBlockUpdatesPerTick());
        stats.put("batchSize", module.getSettings().getBatchSize());
        
        return stats;
    }
} 