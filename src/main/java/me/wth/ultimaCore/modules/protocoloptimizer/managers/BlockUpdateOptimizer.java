package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class BlockUpdateOptimizer {
    private final ProtocolOptimizerModule module;
    
        private final Map<World, Set<Location>> pendingBlockUpdates = new HashMap<>();
    
        private final Map<UUID, Deque<BlockUpdate>> playerBlockUpdateQueues = new ConcurrentHashMap<>();
    
        private final Map<UUID, ChunkRef> playerLastChunk = new ConcurrentHashMap<>();
    
        private final int MAX_UPDATES_PER_TICK = 250;
    
        private int batchedUpdates = 0;
    private int sentUpdates = 0;
    private int droppedUpdates = 0;
    
    
    public BlockUpdateOptimizer(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    public void markBlockForUpdate(Block block) {
        World world = block.getWorld();
        Set<Location> updates = pendingBlockUpdates.computeIfAbsent(world, k -> new HashSet<>());
        updates.add(block.getLocation().clone());
        batchedUpdates++;
    }
    
    
    public void markBlockForPlayerUpdate(Block block, UUID playerUUID) {
        Deque<BlockUpdate> queue = playerBlockUpdateQueues.computeIfAbsent(
                playerUUID, k -> new ArrayDeque<>());
        
                int maxQueuedUpdates = module.getMaxQueuedBlockUpdates();
        if (queue.size() >= maxQueuedUpdates) {
            queue.pollFirst();
            droppedUpdates++;
        }
        
        queue.addLast(new BlockUpdate(block.getLocation().clone(), System.currentTimeMillis()));
        batchedUpdates++;
    }
    
    
    public void processTick() {
                updatePlayerChunks();
        
                processWorldBlockUpdates();
        
                processPlayerQueues();
    }
    
    
    private void updatePlayerChunks() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            
            playerLastChunk.put(player.getUniqueId(), new ChunkRef(loc.getWorld(), chunkX, chunkZ));
        }
    }
    
    
    private void processWorldBlockUpdates() {
        for (Map.Entry<World, Set<Location>> entry : pendingBlockUpdates.entrySet()) {
            World world = entry.getKey();
            Set<Location> locations = entry.getValue();
            
            if (locations.isEmpty()) {
                continue;
            }
            
                        Map<ChunkRef, Set<Location>> chunkBlockMap = new HashMap<>();
            
            for (Location location : locations) {
                int chunkX = location.getBlockX() >> 4;
                int chunkZ = location.getBlockZ() >> 4;
                ChunkRef chunkRef = new ChunkRef(world, chunkX, chunkZ);
                
                Set<Location> chunkBlocks = chunkBlockMap.computeIfAbsent(chunkRef, k -> new HashSet<>());
                chunkBlocks.add(location);
            }
            
                        for (Map.Entry<ChunkRef, Set<Location>> chunkEntry : chunkBlockMap.entrySet()) {
                ChunkRef chunkRef = chunkEntry.getKey();
                Set<Location> chunkBlocks = chunkEntry.getValue();
                
                for (Player player : world.getPlayers()) {
                    ChunkRef playerChunk = playerLastChunk.get(player.getUniqueId());
                    
                    if (playerChunk != null && isChunkInRange(playerChunk, chunkRef, module.getViewDistance())) {
                        for (Location blockLoc : chunkBlocks) {
                                                                                    player.sendBlockChange(blockLoc, blockLoc.getBlock().getBlockData());
                            sentUpdates++;
                        }
                    }
                }
            }
            
                        locations.clear();
        }
    }
    
    
    private void processPlayerQueues() {
        for (Map.Entry<UUID, Deque<BlockUpdate>> entry : playerBlockUpdateQueues.entrySet()) {
            UUID playerUUID = entry.getKey();
            Deque<BlockUpdate> queue = entry.getValue();
            
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null || !player.isOnline()) {
                continue;
            }
            
                        int updates = 0;
            while (!queue.isEmpty() && updates < MAX_UPDATES_PER_TICK) {
                BlockUpdate update = queue.pollFirst();
                
                                if (System.currentTimeMillis() - update.timestamp > 5000) {
                                        droppedUpdates++;
                    continue;
                }
                
                                Location location = update.location;
                if (location.getWorld() != null) {
                                                            player.sendBlockChange(location, location.getBlock().getBlockData());
                    sentUpdates++;
                    updates++;
                }
            }
        }
    }
    
    
    private boolean isChunkInRange(ChunkRef center, ChunkRef target, int viewDistance) {
        if (center.world != target.world) {
            return false;
        }
        
        int dx = Math.abs(center.x - target.x);
        int dz = Math.abs(center.z - target.z);
        
        return dx <= viewDistance && dz <= viewDistance;
    }
    
    
    public void clearPlayerData(UUID playerUUID) {
        playerBlockUpdateQueues.remove(playerUUID);
        playerLastChunk.remove(playerUUID);
    }
    
    
    public int getBatchedUpdates() {
        return batchedUpdates;
    }
    
    
    public int getSentUpdates() {
        return sentUpdates;
    }
    
    
    public int getDroppedUpdates() {
        return droppedUpdates;
    }
    
    
    public double getSavingsPercentage() {
        int total = batchedUpdates;
        if (total == 0) {
            return 0.0;
        }
        
        return 100.0 * (total - sentUpdates) / total;
    }
    
    
    private static class ChunkRef {
        final World world;
        final int x;
        final int z;
        
        ChunkRef(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            ChunkRef other = (ChunkRef) obj;
            return x == other.x && z == other.z && world.equals(other.world);
        }
        
        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }
    
    
    private static class BlockUpdate {
        final Location location;
        final long timestamp;
        
        BlockUpdate(Location location, long timestamp) {
            this.location = location;
            this.timestamp = timestamp;
        }
    }
} 