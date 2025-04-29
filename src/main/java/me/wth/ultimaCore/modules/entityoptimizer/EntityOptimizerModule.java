package me.wth.ultimaCore.modules.entityoptimizer;

import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;


public class EntityOptimizerModule extends AbstractModule implements Listener {
        private final Map<EntityType, Integer> entityLimits = new HashMap<>();
    
        private final Map<EntityType, AIOptimizationLevel> entityAILevels = new HashMap<>();
    
        private final Map<AIOptimizationLevel, Integer> aiUpdateIntervals = new HashMap<>();
    
        private final Map<Entity, Integer> entityTickCounters = new HashMap<>();
    
        private final Map<Chunk, Integer> chunkEntityLoadMap = new HashMap<>();
    
        private final Map<Chunk, List<Entity>> chunkEntityCache = new HashMap<>();
    
        private boolean dynamicLimitsEnabled = true;
    private boolean mergeAIEnabled = true;
    private boolean farmOptimizationEnabled = true;
    private int maxEntitiesPerChunk = 50;
    private int farmDetectionThreshold = 30;
    private int entityScanInterval = 100;
    private int entityScanCounter = 0;
    private int maxDuplicateEntitiesPerArea = 20;
    
    private BukkitTask optimizationTask;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                loadConfiguration();
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
        
                initDefaultAILevels();
        
                startOptimizationTask();
        
        LoggerUtil.info("Модуль EntityOptimizer успешно инициализирован");
    }
    
    @Override
    public void onDisable() {
        if (optimizationTask != null) {
            optimizationTask.cancel();
        }
        
                entityTickCounters.clear();
        chunkEntityLoadMap.clear();
        chunkEntityCache.clear();
        
        super.onDisable();
    }
    
    @Override
    public String getName() {
        return "EntityOptimizer";
    }
    
    @Override
    public String getDescription() {
        return "Модуль оптимизации сущностей и управления их поведением";
    }
    
    @Override
    public void onTick() {
                entityScanCounter++;
        if (entityScanCounter >= entityScanInterval) {
            scanAndOptimizeEntities();
            entityScanCounter = 0;
        }
    }
    
    
    private void loadConfiguration() {
        plugin.reloadConfig();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modules.entityoptimizer");
        
        if (config != null) {
            dynamicLimitsEnabled = config.getBoolean("dynamic_limits_enabled", true);
            mergeAIEnabled = config.getBoolean("merge_ai_enabled", true);
            farmOptimizationEnabled = config.getBoolean("farm_optimization_enabled", true);
            maxEntitiesPerChunk = config.getInt("max_entities_per_chunk", 50);
            farmDetectionThreshold = config.getInt("farm_detection_threshold", 30);
            entityScanInterval = config.getInt("entity_scan_interval", 100);
            maxDuplicateEntitiesPerArea = config.getInt("max_duplicate_entities_per_area", 20);
            
                        ConfigurationSection limitsSection = config.getConfigurationSection("entity_limits");
            if (limitsSection != null) {
                for (String key : limitsSection.getKeys(false)) {
                    try {
                        EntityType type = EntityType.valueOf(key.toUpperCase());
                        entityLimits.put(type, limitsSection.getInt(key));
                    } catch (IllegalArgumentException e) {
                        LoggerUtil.warning("Неизвестный тип сущности в конфигурации: " + key);
                    }
                }
            }
            
                        ConfigurationSection intervalsSection = config.getConfigurationSection("ai_update_intervals");
            if (intervalsSection != null) {
                for (String key : intervalsSection.getKeys(false)) {
                    try {
                        AIOptimizationLevel level = AIOptimizationLevel.valueOf(key.toUpperCase());
                        aiUpdateIntervals.put(level, intervalsSection.getInt(key));
                    } catch (IllegalArgumentException e) {
                        LoggerUtil.warning("Неизвестный уровень AI в конфигурации: " + key);
                    }
                }
            }
        }
        
                if (aiUpdateIntervals.isEmpty()) {
            aiUpdateIntervals.put(AIOptimizationLevel.FULL, 1);
            aiUpdateIntervals.put(AIOptimizationLevel.HIGH, 2);
            aiUpdateIntervals.put(AIOptimizationLevel.MEDIUM, 5);
            aiUpdateIntervals.put(AIOptimizationLevel.LOW, 10);
            aiUpdateIntervals.put(AIOptimizationLevel.MINIMAL, 20);
        }
    }
    
    
    private void initDefaultAILevels() {
                
                entityAILevels.put(EntityType.ENDER_DRAGON, AIOptimizationLevel.FULL);
        entityAILevels.put(EntityType.WITHER, AIOptimizationLevel.FULL);
        entityAILevels.put(EntityType.ELDER_GUARDIAN, AIOptimizationLevel.FULL);
        
                entityAILevels.put(EntityType.ZOMBIE, AIOptimizationLevel.HIGH);
        entityAILevels.put(EntityType.SKELETON, AIOptimizationLevel.HIGH);
        entityAILevels.put(EntityType.CREEPER, AIOptimizationLevel.HIGH);
        entityAILevels.put(EntityType.SPIDER, AIOptimizationLevel.HIGH);
        
                entityAILevels.put(EntityType.WOLF, AIOptimizationLevel.MEDIUM);
        entityAILevels.put(EntityType.IRON_GOLEM, AIOptimizationLevel.MEDIUM);
        entityAILevels.put(EntityType.VILLAGER, AIOptimizationLevel.MEDIUM);
        
                entityAILevels.put(EntityType.COW, AIOptimizationLevel.LOW);
        entityAILevels.put(EntityType.SHEEP, AIOptimizationLevel.LOW);
        entityAILevels.put(EntityType.PIG, AIOptimizationLevel.LOW);
        entityAILevels.put(EntityType.CHICKEN, AIOptimizationLevel.LOW);
        
                entityAILevels.put(EntityType.ITEM_FRAME, AIOptimizationLevel.MINIMAL);
        entityAILevels.put(EntityType.PAINTING, AIOptimizationLevel.MINIMAL);
        entityAILevels.put(EntityType.ARMOR_STAND, AIOptimizationLevel.MINIMAL);
        entityAILevels.put(EntityType.ITEM, AIOptimizationLevel.MINIMAL);
    }
    
    
    private void startOptimizationTask() {
        optimizationTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity livingEntity) {
                        processEntityAI(livingEntity);
                    }
                }
            }
            
                        updateEntityCache();
            
                        if (farmOptimizationEnabled) {
                detectAndOptimizeFarms();
            }
            
                        if (entityScanCounter % 20 == 0) {                 detectAndRemoveDuplicates();
            }
        }, 20L, 20L);     }
    
    
    private void processEntityAI(LivingEntity entity) {
                AIOptimizationLevel level = entityAILevels.getOrDefault(entity.getType(), AIOptimizationLevel.MEDIUM);
        
                int updateInterval = aiUpdateIntervals.getOrDefault(level, 1);
        
                int counter = entityTickCounters.getOrDefault(entity, 0) + 1;
        entityTickCounters.put(entity, counter);
        
                if (counter >= updateInterval) {
            entityTickCounters.put(entity, 0);
            
                                } else {
                                            }
    }
    
    
    private void updateEntityCache() {
        chunkEntityCache.clear();
        chunkEntityLoadMap.clear();
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                Chunk chunk = entity.getLocation().getChunk();
                
                                List<Entity> entities = chunkEntityCache.computeIfAbsent(chunk, k -> new ArrayList<>());
                entities.add(entity);
                
                                int load = calculateEntityLoad(entity);
                
                                chunkEntityLoadMap.merge(chunk, load, Integer::sum);
            }
        }
    }
    
    
    private int calculateEntityLoad(Entity entity) {
        if (entity instanceof EnderDragon || entity instanceof Wither) {
            return 10;         } else if (entity instanceof Monster) {
            return 2;         } else if (entity instanceof LivingEntity) {
            return 1;         } else {
            return 0;         }
    }
    
    
    private void scanAndOptimizeEntities() {
        LoggerUtil.debug("Запуск полного сканирования сущностей...");
        
                int beforeCount = 0;
        for (World world : Bukkit.getWorlds()) {
            beforeCount += world.getEntities().size();
        }
        
                for (Map.Entry<Chunk, List<Entity>> entry : chunkEntityCache.entrySet()) {
            Chunk chunk = entry.getKey();
            List<Entity> entities = entry.getValue();
            
                        if (entities.size() > maxEntitiesPerChunk) {
                optimizeChunkEntities(chunk, entities);
            }
            
                        Map<EntityType, Integer> typeCount = new HashMap<>();
            for (Entity entity : entities) {
                typeCount.merge(entity.getType(), 1, Integer::sum);
            }
            
                        for (Map.Entry<EntityType, Integer> typeEntry : typeCount.entrySet()) {
                EntityType type = typeEntry.getKey();
                int count = typeEntry.getValue();
                
                Integer limit = entityLimits.get(type);
                if (limit != null && count > limit) {
                    optimizeEntitiesByType(chunk, type, entities, limit);
                }
            }
        }
        
                int afterCount = 0;
        for (World world : Bukkit.getWorlds()) {
            afterCount += world.getEntities().size();
        }
        
        LoggerUtil.debug("Сканирование завершено. Оптимизировано " + (beforeCount - afterCount) + " сущностей.");
    }
    
    
    private void optimizeChunkEntities(Chunk chunk, List<Entity> entities) {
                entities.sort((e1, e2) -> {
                        if (e1 instanceof Player) return 1;
            if (e2 instanceof Player) return -1;
            
                        if (e1 instanceof LivingEntity le1 && e2 instanceof LivingEntity le2) {
                boolean e1Named = le1.getCustomName() != null;
                boolean e2Named = le2.getCustomName() != null;
                if (e1Named && !e2Named) return 1;
                if (!e1Named && e2Named) return -1;
            }
            
                        AIOptimizationLevel l1 = entityAILevels.getOrDefault(e1.getType(), AIOptimizationLevel.MEDIUM);
            AIOptimizationLevel l2 = entityAILevels.getOrDefault(e2.getType(), AIOptimizationLevel.MEDIUM);
            return l2.getValue() - l1.getValue();         });
        
                int toRemove = entities.size() - maxEntitiesPerChunk;
        int removed = 0;
        
        for (Entity entity : entities) {
            if (removed >= toRemove) break;
            
                        if (entity instanceof Player) continue;
            if (entity instanceof LivingEntity le && le.getCustomName() != null) continue;
            
            entity.remove();
            removed++;
        }
    }
    
    
    private void optimizeEntitiesByType(Chunk chunk, EntityType type, List<Entity> entities, int limit) {
                List<Entity> typeEntities = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getType() == type) {
                typeEntities.add(entity);
            }
        }
        
                typeEntities.sort((e1, e2) -> {
                        if (e1 instanceof LivingEntity le1 && e2 instanceof LivingEntity le2) {
                boolean e1Named = le1.getCustomName() != null;
                boolean e2Named = le2.getCustomName() != null;
                if (e1Named && !e2Named) return 1;
                if (!e1Named && e2Named) return -1;
            }
            
                        if (e1 instanceof LivingEntity le1 && e2 instanceof LivingEntity le2) {
                return Double.compare(le2.getHealth(), le1.getHealth());
            }
            
            return 0;
        });
        
                int toRemove = typeEntities.size() - limit;
        for (int i = 0; i < toRemove; i++) {
            if (i < typeEntities.size()) {
                typeEntities.get(i).remove();
            }
        }
    }
    
    
    private void detectAndOptimizeFarms() {
        for (Map.Entry<Chunk, List<Entity>> entry : chunkEntityCache.entrySet()) {
            Chunk chunk = entry.getKey();
            List<Entity> entities = entry.getValue();
            
                        Map<EntityType, Integer> typeCount = new HashMap<>();
            for (Entity entity : entities) {
                typeCount.merge(entity.getType(), 1, Integer::sum);
            }
            
                        for (Map.Entry<EntityType, Integer> typeEntry : typeCount.entrySet()) {
                if (typeEntry.getValue() > farmDetectionThreshold) {
                                        optimizeFarm(chunk, typeEntry.getKey(), entities);
                }
            }
        }
    }
    
    
    private void optimizeFarm(Chunk chunk, EntityType type, List<Entity> entities) {
        LoggerUtil.debug("Обнаружена ферма " + type.name() + " в чанке " + chunk.getX() + "," + chunk.getZ());
        
                List<Entity> farmEntities = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity.getType() == type) {
                farmEntities.add(entity);
            }
        }
        
                        int optimalCount = Math.min(farmEntities.size(), farmDetectionThreshold / 2);
        
                if (farmEntities.size() > optimalCount) {
            for (int i = optimalCount; i < farmEntities.size(); i++) {
                farmEntities.get(i).remove();
            }
        }
        
                if (mergeAIEnabled && type != EntityType.PLAYER) {
            mergeEntityAI(farmEntities.subList(0, optimalCount));
        }
    }
    
    
    private void mergeEntityAI(List<Entity> entities) {
                List<List<Entity>> clusters = new ArrayList<>();
        
        for (Entity entity : entities) {
                        boolean added = false;
            
            for (List<Entity> cluster : clusters) {
                if (canAddToCluster(entity, cluster)) {
                    cluster.add(entity);
                    added = true;
                    break;
                }
            }
            
                        if (!added) {
                List<Entity> newCluster = new ArrayList<>();
                newCluster.add(entity);
                clusters.add(newCluster);
            }
        }
        
                for (List<Entity> cluster : clusters) {
            if (cluster.size() <= 1) continue;
            
                        for (int i = 1; i < cluster.size(); i++) {
                Entity entity = cluster.get(i);
                
                                                entityAILevels.put(entity.getType(), AIOptimizationLevel.MINIMAL);
            }
        }
    }
    
    
    private boolean canAddToCluster(Entity entity, List<Entity> cluster) {
        if (cluster.isEmpty()) return true;
        
                EntityType type = entity.getType();
        if (type != cluster.get(0).getType()) return false;
        
                double maxDistance = 8.0;         
        for (Entity clusterEntity : cluster) {
            if (entity.getLocation().distance(clusterEntity.getLocation()) <= maxDistance) {
                return true;
            }
        }
        
        return false;
    }
    
    
    private void detectAndRemoveDuplicates() {
                Map<String, List<Entity>> potentialDuplicates = new HashMap<>();
        
                for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                                if (entity instanceof Player) continue;
                
                                String key = createEntityKey(entity);
                potentialDuplicates.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
            }
        }
        
                for (Map.Entry<String, List<Entity>> entry : potentialDuplicates.entrySet()) {
            List<Entity> duplicates = entry.getValue();
            
            if (duplicates.size() > maxDuplicateEntitiesPerArea) {
                                for (int i = maxDuplicateEntitiesPerArea; i < duplicates.size(); i++) {
                    duplicates.get(i).remove();
                }
            }
        }
    }
    
    
    private String createEntityKey(Entity entity) {
                int x = (int) (entity.getLocation().getX() / 8);
        int y = (int) (entity.getLocation().getY() / 4);
        int z = (int) (entity.getLocation().getZ() / 8);
        
        return entity.getType().name() + ":" + x + ":" + y + ":" + z;
    }
    
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.isCancelled()) return;
        
                Chunk chunk = event.getLocation().getChunk();
        EntityType type = event.getEntityType();
        
                List<Entity> chunkEntities = chunkEntityCache.getOrDefault(chunk, Collections.emptyList());
        int typeCount = 0;
        
        for (Entity entity : chunkEntities) {
            if (entity.getType() == type) {
                typeCount++;
            }
        }
        
                Integer limit = entityLimits.get(type);
        if (limit != null && typeCount >= limit) {
            event.setCancelled(true);
            return;
        }
        
                if (chunkEntities.size() >= maxEntitiesPerChunk) {
            event.setCancelled(true);
        }
    }
    
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.isCancelled()) return;
        
                Entity entity = event.getEntity();
        Chunk chunk = entity.getLocation().getChunk();
        
        List<Entity> entities = chunkEntityCache.computeIfAbsent(chunk, k -> new ArrayList<>());
        entities.add(entity);
        
                int load = calculateEntityLoad(entity);
        chunkEntityLoadMap.merge(chunk, load, Integer::sum);
    }
    
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
                Chunk chunk = event.getChunk();
        Entity[] entities = chunk.getEntities();
        
                List<Entity> entityList = new ArrayList<>(Arrays.asList(entities));
        chunkEntityCache.put(chunk, entityList);
        
                int totalLoad = 0;
        for (Entity entity : entities) {
            totalLoad += calculateEntityLoad(entity);
        }
        chunkEntityLoadMap.put(chunk, totalLoad);
        
                if (entities.length > maxEntitiesPerChunk) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                optimizeChunkEntities(chunk, entityList);
            }, 1L);
        }
    }
} 