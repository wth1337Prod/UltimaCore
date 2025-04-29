package me.wth.ultimaCore.modules.lagshield;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class ChunkMonitor {
    private final LagShieldModule module;
    private final Map<String, Map<ChunkCoord, Long>> chunkLastAccess = new HashMap<>();
    
    
    public ChunkMonitor(LagShieldModule module) {
        this.module = module;
    }
    
    
    public void registerChunkLoad(Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        ChunkCoord coord = new ChunkCoord(chunk.getX(), chunk.getZ());
        
                chunkLastAccess.computeIfAbsent(worldName, k -> new HashMap<>())
                .put(coord, System.currentTimeMillis());
    }
    
    
    public void registerChunkUnload(Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        ChunkCoord coord = new ChunkCoord(chunk.getX(), chunk.getZ());
        
                Map<ChunkCoord, Long> worldChunks = chunkLastAccess.get(worldName);
        if (worldChunks != null) {
            worldChunks.remove(coord);
        }
    }
    
    
    public void updateChunkAccess(Chunk chunk) {
        String worldName = chunk.getWorld().getName();
        ChunkCoord coord = new ChunkCoord(chunk.getX(), chunk.getZ());
        
                chunkLastAccess.computeIfAbsent(worldName, k -> new HashMap<>())
                .put(coord, System.currentTimeMillis());
    }
    
    
    public int unloadInactiveChunks() {
        LagShieldSettings settings = module.getSettings();
        int unloaded = 0;
        long threshold = System.currentTimeMillis() - (settings.getChunkUnloadTime() * 60 * 1000);
        
                Set<ChunkCoord> playerChunks = new HashSet<>();
        Map<String, Set<ChunkCoord>> playerChunksByWorld = new HashMap<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            String worldName = player.getWorld().getName();
            Chunk chunk = player.getLocation().getChunk();
            ChunkCoord coord = new ChunkCoord(chunk.getX(), chunk.getZ());
            
                        playerChunks.add(coord);
            playerChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>()).add(coord);
            
                        int viewDistance = Bukkit.getViewDistance();
            for (int x = -viewDistance; x <= viewDistance; x++) {
                for (int z = -viewDistance; z <= viewDistance; z++) {
                    ChunkCoord nearbyCoord = new ChunkCoord(chunk.getX() + x, chunk.getZ() + z);
                    playerChunksByWorld.computeIfAbsent(worldName, k -> new HashSet<>()).add(nearbyCoord);
                }
            }
        }
        
                for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            Set<ChunkCoord> worldPlayerChunks = playerChunksByWorld.getOrDefault(worldName, new HashSet<>());
            Map<ChunkCoord, Long> worldChunks = chunkLastAccess.getOrDefault(worldName, new HashMap<>());
            
            for (Chunk chunk : world.getLoadedChunks()) {
                ChunkCoord coord = new ChunkCoord(chunk.getX(), chunk.getZ());
                
                                if (worldPlayerChunks.contains(coord)) {
                    continue;
                }
                
                                Long lastAccess = worldChunks.get(coord);
                if (lastAccess == null || lastAccess < threshold) {
                                        world.unloadChunk(chunk.getX(), chunk.getZ(), true);
                    worldChunks.remove(coord);
                    unloaded++;
                }
            }
        }
        
        return unloaded;
    }
    
    
    public int getLoadedChunkCount(World world) {
        return world.getLoadedChunks().length;
    }
    
    
    public int getTotalLoadedChunkCount() {
        int total = 0;
        
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        
        return total;
    }
    
    
    public Set<ChunkCoord> getChunksAroundPlayer(Player player, int radius) {
        Set<ChunkCoord> chunks = new HashSet<>();
        Chunk centerChunk = player.getLocation().getChunk();
        
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                chunks.add(new ChunkCoord(centerChunk.getX() + x, centerChunk.getZ() + z));
            }
        }
        
        return chunks;
    }
    
    
    public static class ChunkCoord {
        private final int x;
        private final int z;
        
        
        public ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }
        
        
        public int getX() {
            return x;
        }
        
        
        public int getZ() {
            return z;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            ChunkCoord other = (ChunkCoord) obj;
            return x == other.x && z == other.z;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + z;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + z + ")";
        }
    }
} 