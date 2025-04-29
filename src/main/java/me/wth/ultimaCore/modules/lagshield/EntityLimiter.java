package me.wth.ultimaCore.modules.lagshield;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EntityLimiter {
    private final LagShieldModule module;
    
    
    public EntityLimiter(LagShieldModule module) {
        this.module = module;
    }
    
    
    public int cleanupEntities() {
        LagShieldSettings settings = module.getSettings();
        int removed = 0;
        
        for (World world : Bukkit.getWorlds()) {
                        removed += cleanupWorldEntities(world);
            
                        for (Chunk chunk : world.getLoadedChunks()) {
                removed += cleanupChunkEntities(chunk);
            }
        }
        
        return removed;
    }
    
    
    private int cleanupWorldEntities(World world) {
        LagShieldSettings settings = module.getSettings();
        int removed = 0;
        
                Map<EntityType, List<Entity>> entitiesByType = new HashMap<>();
        
        for (Entity entity : world.getEntities()) {
                        if (entity instanceof Player || settings.isEntityTypeProtected(entity.getType())) {
                continue;
            }
            
            entitiesByType.computeIfAbsent(entity.getType(), k -> new ArrayList<>()).add(entity);
        }
        
                for (Map.Entry<EntityType, List<Entity>> entry : entitiesByType.entrySet()) {
            List<Entity> entities = entry.getValue();
            int limit = settings.getEntityLimitPerType();
            
            if (entities.size() > limit) {
                                entities.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());
                
                                for (int i = 0; i < entities.size() - limit; i++) {
                    entities.get(i).remove();
                    removed++;
                }
            }
        }
        
        return removed;
    }
    
    
    private int cleanupChunkEntities(Chunk chunk) {
        LagShieldSettings settings = module.getSettings();
        Entity[] entities = chunk.getEntities();
        int removed = 0;
        
                if (entities.length <= settings.getMaxEntitiesPerChunk()) {
            return 0;
        }
        
                List<Entity> monsters = new ArrayList<>();
        List<Entity> animals = new ArrayList<>();
        List<Entity> items = new ArrayList<>();
        List<Entity> other = new ArrayList<>();
        
        for (Entity entity : entities) {
            if (entity instanceof Player || settings.isEntityTypeProtected(entity.getType())) {
                continue;
            }
            
            if (entity instanceof Monster) {
                monsters.add(entity);
            } else if (entity instanceof Animals) {
                animals.add(entity);
            } else if (entity.getType() == EntityType.ITEM) {
                items.add(entity);
            } else if (!(entity instanceof Painting) && !(entity instanceof ItemFrame)) {
                other.add(entity);
            }
        }
        
                monsters.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());
        animals.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());
        items.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());
        other.sort(Comparator.comparingInt(Entity::getTicksLived).reversed());
        
                int toRemove = entities.length - settings.getMaxEntitiesPerChunk();
        
                removed += removeEntities(items, Math.min(toRemove, items.size()));
        toRemove -= Math.min(toRemove, items.size());
        
        if (toRemove > 0) {
            removed += removeEntities(monsters, Math.min(toRemove, monsters.size()));
            toRemove -= Math.min(toRemove, monsters.size());
        }
        
        if (toRemove > 0) {
            removed += removeEntities(other, Math.min(toRemove, other.size()));
            toRemove -= Math.min(toRemove, other.size());
        }
        
        if (toRemove > 0) {
            removed += removeEntities(animals, Math.min(toRemove, animals.size()));
        }
        
        return removed;
    }
    
    
    private int removeEntities(List<Entity> entities, int count) {
        int removed = 0;
        
        for (int i = 0; i < count && i < entities.size(); i++) {
            entities.get(i).remove();
            removed++;
        }
        
        return removed;
    }
    
    
    public Map<EntityType, Integer> countEntitiesByType(World world) {
        Map<EntityType, Integer> counts = new HashMap<>();
        
        for (Entity entity : world.getEntities()) {
            counts.put(entity.getType(), counts.getOrDefault(entity.getType(), 0) + 1);
        }
        
        return counts;
    }
    
    
    public int countTotalEntities(World world) {
        return world.getEntities().size();
    }
    
    
    public List<ChunkEntityCount> findMostPopulatedChunks(World world, int limit) {
        Map<Chunk, Integer> chunkCounts = new HashMap<>();
        
        for (Chunk chunk : world.getLoadedChunks()) {
            chunkCounts.put(chunk, chunk.getEntities().length);
        }
        
        List<ChunkEntityCount> result = new ArrayList<>();
        
        chunkCounts.entrySet().stream()
                .sorted(Map.Entry.<Chunk, Integer>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> result.add(new ChunkEntityCount(entry.getKey(), entry.getValue())));
        
        return result;
    }
    
    
    public static class ChunkEntityCount {
        private final Chunk chunk;
        private final int entityCount;
        
        
        public ChunkEntityCount(Chunk chunk, int entityCount) {
            this.chunk = chunk;
            this.entityCount = entityCount;
        }
        
        
        public Chunk getChunk() {
            return chunk;
        }
        
        
        public int getX() {
            return chunk.getX();
        }
        
        
        public int getZ() {
            return chunk.getZ();
        }
        
        
        public int getEntityCount() {
            return entityCount;
        }
    }
} 