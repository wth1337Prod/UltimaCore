package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class BlockPacketOptimizer implements Listener {
    private final ProtocolOptimizerModule module;

        private final Map<UUID, Queue<BlockUpdate>> playerBlockUpdateQueues = new ConcurrentHashMap<>();
    private final Map<Long, Set<BlockUpdate>> chunkBlockUpdates = new ConcurrentHashMap<>();
    
        private final Set<Long> registeredChunks = new HashSet<>();
    
        private int maxBatchSize;
    private int maxQueuedUpdates;
    
        private final AtomicInteger totalBlockUpdates = new AtomicInteger(0);
    private final AtomicInteger batchedBlockUpdates = new AtomicInteger(0);
    
    private BukkitTask cleanupTask;

    
    private static class BlockUpdate {
        private final Location location;
        private final Object updateData;
        private final long timestamp;
        
        public BlockUpdate(Location location, Object updateData) {
            this.location = location;
            this.updateData = updateData;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Location getLocation() {
            return location;
        }
        
        public Object getUpdateData() {
            return updateData;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockUpdate that = (BlockUpdate) o;
            return location.equals(that.location);
        }
        
        @Override
        public int hashCode() {
            return location.hashCode();
        }
    }

    
    public BlockPacketOptimizer(ProtocolOptimizerModule module) {
        this.module = module;
        this.maxBatchSize = module.getSettings().getMaxQueuedBlockUpdates();
        this.maxQueuedUpdates = module.getSettings().getMaxQueuedBlockUpdates() * 2;
        
                Bukkit.getScheduler().runTaskTimer(module.getPlugin(), 
                this::processBlockUpdateQueues, 
                module.getSettings().getBlockUpdateInterval(), 
                module.getSettings().getBlockUpdateInterval());
        
                this.cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            module.getPlugin(),
            this::cleanupExpiredData,
            1200L,             1200L          );
    }

    
    private void cleanupExpiredData() {
        try {
                        Set<Long> inactiveChunks = new HashSet<>();
            
            for (Map.Entry<Long, Set<BlockUpdate>> entry : chunkBlockUpdates.entrySet()) {
                long chunkKey = entry.getKey();
                Set<BlockUpdate> updates = entry.getValue();
                
                                updates.removeIf(update -> System.currentTimeMillis() - update.getTimestamp() > 60000);
                
                                if (updates.isEmpty()) {
                    inactiveChunks.add(chunkKey);
                }
            }
            
                        for (Long chunkKey : inactiveChunks) {
                chunkBlockUpdates.remove(chunkKey);
                playerBlockUpdateQueues.remove(chunkKey);
            }
        } catch (Exception e) {
            LoggerUtil.severe("&cОшибка при очистке данных оптимизатора пакетов блоков: " + e.getMessage());
        }
    }

    
    private void processBlockUpdateQueues() {
                for (UUID playerUUID : playerBlockUpdateQueues.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                sendQueuedUpdatesFor(player);
            }
        }
    }

    
    public void registerChunk(Chunk chunk) {
        if (chunk == null) return;
        
        long chunkKey = getChunkKey(chunk);
        registeredChunks.add(chunkKey);
        
                chunkBlockUpdates.computeIfAbsent(chunkKey, k -> new HashSet<>());
    }

    
    public void batchBlockUpdates(Chunk chunk) {
        if (chunk == null) return;
        
        long chunkKey = getChunkKey(chunk);
        Set<BlockUpdate> updates = chunkBlockUpdates.get(chunkKey);
        
        if (updates == null || updates.isEmpty()) return;
        
                List<BlockUpdate> updatesList = new ArrayList<>(updates);
        int totalUpdates = updatesList.size();
        
                totalBlockUpdates.addAndGet(totalUpdates);
        int batchCount = (int) Math.ceil((double) totalUpdates / maxBatchSize);
        batchedBlockUpdates.addAndGet(batchCount);
        
                updates.clear();
    }

    
    public void prioritizeUpdatesForPlayer(Player player, Chunk chunk) {
        if (player == null || chunk == null) return;
        
        UUID playerUUID = player.getUniqueId();
        long chunkKey = getChunkKey(chunk);
        Set<BlockUpdate> updates = chunkBlockUpdates.get(chunkKey);
        
        if (updates == null || updates.isEmpty()) return;
        
                Queue<BlockUpdate> playerQueue = playerBlockUpdateQueues.computeIfAbsent(playerUUID, k -> new LinkedList<>());
        
                while (playerQueue.size() > maxQueuedUpdates) {
            playerQueue.poll();
        }
        
                for (BlockUpdate update : updates) {
            if (!playerQueue.contains(update)) {
                playerQueue.offer(update);
            }
        }
    }

    
    public void sendQueuedUpdatesFor(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        Queue<BlockUpdate> playerQueue = playerBlockUpdateQueues.get(playerUUID);
        
        if (playerQueue == null || playerQueue.isEmpty()) return;
        
                List<BlockUpdate> batch = new ArrayList<>(maxBatchSize);
        int count = 0;
        
        while (!playerQueue.isEmpty() && count < maxBatchSize) {
            BlockUpdate update = playerQueue.poll();
            batch.add(update);
            count++;
        }
        
        if (count > 0) {
                                    LoggerUtil.debug("Отправлено " + count + " обновлений блоков для " + player.getName());
        }
    }

    
    public int getProcessedPackets() {
        return totalBlockUpdates.get();
    }
    
    
    public int getOptimizedPackets() {
        return batchedBlockUpdates.get();
    }

    
    public void clearChunkData(Chunk chunk) {
        if (chunk == null) return;
        
        long chunkKey = getChunkKey(chunk);
        chunkBlockUpdates.remove(chunkKey);
        playerBlockUpdateQueues.remove(chunkKey);
        registeredChunks.remove(chunkKey);
    }

    
    public void clearAllUpdates() {
        chunkBlockUpdates.clear();
        playerBlockUpdateQueues.clear();
        
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
    }

    
    public void sendAllQueuedUpdates() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendQueuedUpdatesFor(player);
        }
        
                for (Set<BlockUpdate> updates : chunkBlockUpdates.values()) {
            updates.clear();
        }
    }

    
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    
    private long getChunkKey(Chunk chunk) {
        int x = chunk.getX();
        int z = chunk.getZ();
        return ((long) x << 32) | (z & 0xffffffffL);
    }

    
    public void queueBlockUpdate(Block block, Object updateData) {
        if (block == null) return;
        
        Location location = block.getLocation();
        Chunk chunk = location.getChunk();
        long chunkKey = getChunkKey(chunk);
        
                if (!registeredChunks.contains(chunkKey)) {
            registerChunk(chunk);
        }
        
                BlockUpdate update = new BlockUpdate(location, updateData);
        
                Set<BlockUpdate> updates = chunkBlockUpdates.computeIfAbsent(chunkKey, k -> new HashSet<>());
        updates.add(update);
        
                if (updates.size() >= maxBatchSize) {
            batchBlockUpdates(chunk);
        }
        
                for (Player player : Bukkit.getOnlinePlayers()) {
            if (isChunkVisibleForPlayer(chunk, player)) {
                Queue<BlockUpdate> playerQueue = playerBlockUpdateQueues.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>());
                
                                while (playerQueue.size() >= maxQueuedUpdates) {
                    playerQueue.poll();
                }
                
                playerQueue.offer(update);
            }
        }
    }

    
    private boolean isChunkVisibleForPlayer(Chunk chunk, Player player) {
                if (chunk.getWorld() != player.getWorld()) {
            return false;
        }
        
                Chunk playerChunk = player.getLocation().getChunk();
        
                int viewDistance = module.getSettings().getBlockViewDistance();
        int dx = Math.abs(chunk.getX() - playerChunk.getX());
        int dz = Math.abs(chunk.getZ() - playerChunk.getZ());
        
        return dx <= viewDistance && dz <= viewDistance;
    }

    
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        
        int totalUpdates = totalBlockUpdates.get();
        int batchedUpdates = batchedBlockUpdates.get();
        double ratio = totalUpdates > 0 ? (double) totalUpdates / batchedUpdates : 0;
        
        stats.append("&7Всего обновлений блоков: &e").append(totalUpdates).append("\n");
        stats.append("&7Отправлено пакетов: &e").append(batchedUpdates).append("\n");
        
        if (ratio > 0) {
            stats.append("&7Среднее количество обновлений в пакете: &e").append(String.format("%.2f", ratio)).append("\n");
        }
        
        stats.append("&7Отслеживаемых чанков: &e").append(registeredChunks.size()).append("\n");
        stats.append("&7Макс. размер пакета: &e").append(maxBatchSize).append("\n");
        
        return stats.toString();
    }

    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        
                for (Set<BlockUpdate> updates : chunkBlockUpdates.values()) {
            updates.removeIf(update -> update.getLocation().getBlockX() == event.getPlayer().getLocation().getBlockX() &&
                                     update.getLocation().getBlockY() == event.getPlayer().getLocation().getBlockY() &&
                                     update.getLocation().getBlockZ() == event.getPlayer().getLocation().getBlockZ());
        }
    }

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
                queueBlockUpdate(event.getBlock(), "PLACE");
    }

    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
                queueBlockUpdate(event.getBlock(), "BREAK");
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
                registerChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
                clearChunkData(event.getChunk());
    }
} 