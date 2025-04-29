package me.wth.ultimaCore.modules.chunkmaster;

import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class ChunkMasterModule extends AbstractModule implements Listener {
    private final Map<UUID, ChunkPriority> worldChunkQuotas = new HashMap<>();
    private final Map<String, Integer> dimensionQuotas = new HashMap<>();
    private final Map<Chunk, ChunkData> chunkDataMap = new HashMap<>();
    private final PriorityQueue<ChunkTask> chunkLoadQueue = new PriorityQueue<>(
            Comparator.comparingInt(task -> task.priority.getValue()));
    
        private final Map<ChunkCoord, ChunkMetrics> chunkMetricsMap = new ConcurrentHashMap<>();
    private final Queue<StructureGenerationTask> structureGenQueue = new PriorityQueue<>(
            Comparator.comparingInt(task -> task.priority));
    
    private BukkitTask chunkProcessingTask;
    private BukkitTask heavyChunkAnalysisTask;
    private BukkitTask structureGenTask;
    
    private boolean preloadChunksOnTeleport = true;
    private int preloadRadius = 3;
    private int maxQueueSize = 500;
    private int chunksPerTick = 5;
    private int defragInterval = 6000;     private int defragCounter = 0;
    
        private int heavyChunkAnalysisInterval = 12000;     private int heavyChunkThreshold = 30;     private int structureGenPerTick = 1;     private boolean deferStructureGeneration = true;     
    @Override
    public void onEnable() {
        super.onEnable();
        
                loadConfiguration();
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
        
                startChunkProcessingTask();
        
                startHeavyChunkAnalysisTask();
        
                if (deferStructureGeneration) {
            startStructureGenTask();
        }
        
                analyzeLoadedChunks();
        
        LoggerUtil.info("Модуль ChunkMaster успешно инициализирован");
    }
    
    @Override
    public void onDisable() {
                if (chunkProcessingTask != null) {
            chunkProcessingTask.cancel();
        }
        
        if (heavyChunkAnalysisTask != null) {
            heavyChunkAnalysisTask.cancel();
        }
        
        if (structureGenTask != null) {
            structureGenTask.cancel();
        }
        
                chunkDataMap.clear();
        chunkLoadQueue.clear();
        chunkMetricsMap.clear();
        structureGenQueue.clear();
        
        super.onDisable();
    }
    
    @Override
    public String getName() {
        return "ChunkMaster";
    }
    
    @Override
    public String getDescription() {
        return "Модуль оптимизации чанков и управления их загрузкой";
    }
    
    @Override
    public void onTick() {
                defragCounter++;
        if (defragCounter >= defragInterval) {
            defragChunks();
            defragCounter = 0;
        }
    }
    
    
    private void loadConfiguration() {
        plugin.reloadConfig();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modules.chunkmaster");
        
        if (config != null) {
            preloadChunksOnTeleport = config.getBoolean("preload_chunks_on_teleport", true);
            preloadRadius = config.getInt("preload_radius", 3);
            maxQueueSize = config.getInt("max_queue_size", 500);
            chunksPerTick = config.getInt("chunks_per_tick", 5);
            defragInterval = config.getInt("defrag_interval", 6000);
            
                        heavyChunkAnalysisInterval = config.getInt("heavy_chunk_analysis_interval", 12000);
            heavyChunkThreshold = config.getInt("heavy_chunk_threshold", 30);
            deferStructureGeneration = config.getBoolean("defer_structure_generation", true);
            structureGenPerTick = config.getInt("structure_gen_per_tick", 1);
            
                        ConfigurationSection quotasSection = config.getConfigurationSection("dimension_quotas");
            if (quotasSection != null) {
                for (String key : quotasSection.getKeys(false)) {
                    dimensionQuotas.put(key, quotasSection.getInt(key));
                }
            }
        }
        
                if (!dimensionQuotas.containsKey("world")) {
            dimensionQuotas.put("world", 3000);
        }
        if (!dimensionQuotas.containsKey("world_nether")) {
            dimensionQuotas.put("world_nether", 1500);
        }
        if (!dimensionQuotas.containsKey("world_the_end")) {
            dimensionQuotas.put("world_the_end", 1000);
        }
    }
    
    
    private void startChunkProcessingTask() {
        chunkProcessingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int processed = 0;
            
                        while (!chunkLoadQueue.isEmpty() && processed < chunksPerTick) {
                ChunkTask task = chunkLoadQueue.poll();
                if (task != null && task.world.isChunkLoaded(task.x, task.z)) {
                    continue;                 }
                
                try {
                                        if (task.generateIfAbsent) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Chunk chunk = task.world.getChunkAt(task.x, task.z);
                                                        registerChunkData(chunk, task.priority);
                        });
                    } else {
                                                if (task.world.isChunkGenerated(task.x, task.z)) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                Chunk chunk = task.world.getChunkAt(task.x, task.z);
                                if (chunk != null) {
                                    registerChunkData(chunk, task.priority);
                                }
                            });
                        }
                    }
                    processed++;
                } catch (Exception e) {
                    LoggerUtil.warning("Ошибка при загрузке чанка [" + task.x + ", " + task.z + "] в мире " + task.world.getName(), e);
                }
            }
        }, 1L, 1L);
    }
    
    
    private void analyzeLoadedChunks() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                                ChunkPriority priority = determinePriority(chunk);
                
                                registerChunkData(chunk, priority);
            }
            
                        UUID worldId = world.getUID();
            String worldType = getWorldType(world);
            Integer quota = dimensionQuotas.get(worldType);
            
            if (quota != null) {
                worldChunkQuotas.put(worldId, ChunkPriority.NORMAL);
            }
            
            LoggerUtil.debug("Проанализировано " + world.getLoadedChunks().length + " чанков в мире " + world.getName());
        }
    }
    
    
    private String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NORMAL:
                return "world";
            case NETHER:
                return "world_nether";
            case THE_END:
                return "world_the_end";
            default:
                return world.getName();
        }
    }
    
    
    private ChunkPriority determinePriority(Chunk chunk) {
        World world = chunk.getWorld();
        
                for (Player player : world.getPlayers()) {
            Chunk playerChunk = player.getLocation().getChunk();
            if (playerChunk.equals(chunk)) {
                return ChunkPriority.CRITICAL;             }
            
                        int distance = (int) Math.sqrt(
                    Math.pow(playerChunk.getX() - chunk.getX(), 2) +
                    Math.pow(playerChunk.getZ() - chunk.getZ(), 2)
            );
            
            if (distance <= 5) {
                return ChunkPriority.HIGH;
            } else if (distance <= 10) {
                return ChunkPriority.NORMAL;
            }
        }
        
                if (hasImportantStructures(chunk) || hasHighActivity(chunk)) {
            return ChunkPriority.HIGH;
        }
        
        return ChunkPriority.LOW;
    }
    
    
    private boolean hasImportantStructures(Chunk chunk) {
                return false;
    }
    
    
    private boolean hasHighActivity(Chunk chunk) {
                return false;
    }
    
    
    private void registerChunkData(Chunk chunk, ChunkPriority priority) {
        ChunkData data = chunkDataMap.computeIfAbsent(chunk, k -> new ChunkData());
        data.priority = priority;
        data.lastAccessed = System.currentTimeMillis();
        data.accessCount++;
    }
    
    
    private void defragChunks() {
        LoggerUtil.debug("Запуск дефрагментации чанков...");
        
                int beforeCount = 0;
        for (World world : Bukkit.getWorlds()) {
            beforeCount += world.getLoadedChunks().length;
        }
        
                List<Chunk> chunksToUnload = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<Chunk, ChunkData> entry : chunkDataMap.entrySet()) {
            Chunk chunk = entry.getKey();
            ChunkData data = entry.getValue();
            
                        if (data.priority == ChunkPriority.LOW && 
                    (currentTime - data.lastAccessed) > 300000) {                 chunksToUnload.add(chunk);
            }
        }
        
                for (Chunk chunk : chunksToUnload) {
            World world = chunk.getWorld();
            int x = chunk.getX();
            int z = chunk.getZ();
            
                        boolean playerPresent = false;
            for (Player player : world.getPlayers()) {
                if (player.getLocation().getChunk().equals(chunk)) {
                    playerPresent = true;
                    break;
                }
            }
            
            if (!playerPresent) {
                                world.unloadChunkRequest(x, z);
                chunkDataMap.remove(chunk);
            }
        }
        
                int afterCount = 0;
        for (World world : Bukkit.getWorlds()) {
            afterCount += world.getLoadedChunks().length;
        }
        
        LoggerUtil.debug("Дефрагментация завершена. Выгружено " + (beforeCount - afterCount) + " чанков.");
    }
    
    
    public void queueChunkLoad(World world, int x, int z, ChunkPriority priority, boolean generateIfAbsent) {
                if (chunkLoadQueue.size() >= maxQueueSize) {
            return;
        }
        
        ChunkTask task = new ChunkTask(world, x, z, priority, generateIfAbsent);
        chunkLoadQueue.offer(task);
    }
    
    
    public void preloadChunks(World world, int centerX, int centerZ, int radius, ChunkPriority priority) {
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                                if (x == centerX && z == centerZ) {
                    continue;
                }
                
                                int distance = (int) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(z - centerZ, 2));
                
                if (distance <= radius) {
                                        ChunkPriority adjustedPriority = priority;
                    if (distance > radius / 2) {
                        adjustedPriority = ChunkPriority.getLower(priority);
                    }
                    
                    queueChunkLoad(world, x, z, adjustedPriority, false);
                }
            }
        }
    }
    
    
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!preloadChunksOnTeleport || event.isCancelled()) {
            return;
        }
        
                World world = event.getTo().getWorld();
        int chunkX = event.getTo().getBlockX() >> 4;
        int chunkZ = event.getTo().getBlockZ() >> 4;
        
        preloadChunks(world, chunkX, chunkZ, preloadRadius, ChunkPriority.HIGH);
    }
    
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        
                ChunkPriority priority = determinePriority(chunk);
        registerChunkData(chunk, priority);
    }
    
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
                chunkDataMap.remove(event.getChunk());
    }
    
    
    private void startHeavyChunkAnalysisTask() {
        heavyChunkAnalysisTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                                for (World world : Bukkit.getWorlds()) {
                    for (Chunk chunk : world.getLoadedChunks()) {
                        analyzeHeavyChunk(chunk);
                    }
                }
                
                                optimizeHeavyChunks();
                
                LoggerUtil.debug("Завершен анализ тяжёлых чанков. Обнаружено " + 
                        chunkMetricsMap.values().stream().filter(ChunkMetrics::isHeavy).count() + " тяжёлых чанков");
            } catch (Exception e) {
                LoggerUtil.severe("Ошибка при анализе тяжёлых чанков", e);
            }
        }, heavyChunkAnalysisInterval, heavyChunkAnalysisInterval);
    }
    
    
    private void startStructureGenTask() {
        structureGenTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                int processed = 0;
                while (!structureGenQueue.isEmpty() && processed < structureGenPerTick) {
                    StructureGenerationTask task = structureGenQueue.poll();
                    if (task != null) {
                        generateStructure(task);
                        processed++;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.severe("Ошибка при отложенной генерации структур", e);
            }
        }, 1L, 1L);
    }
    
    
    private void analyzeHeavyChunk(Chunk chunk) {
        ChunkCoord coord = new ChunkCoord(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        ChunkMetrics metrics = chunkMetricsMap.computeIfAbsent(coord, k -> new ChunkMetrics());
        
                metrics.lastAnalyzed = System.currentTimeMillis();
        
                Entity[] entities = chunk.getEntities();
        metrics.entityCount = entities.length;
        
                metrics.entityTypeCount.clear();
        
                for (Entity entity : entities) {
            metrics.entityTypeCount.merge(entity.getType().name(), 1, Integer::sum);
        }
        
                metrics.potentialFarm = metrics.entityTypeCount.values().stream()
                .anyMatch(count -> count > heavyChunkThreshold / 2);
        
                metrics.tileEntityCount = chunk.getTileEntities().length;
        
                long startTime = System.nanoTime();
        chunk.getEntities();         long endTime = System.nanoTime();
        metrics.updateTime = endTime - startTime;
        
                if (metrics.updateTimeHistory.size() >= 5) {
            metrics.updateTimeHistory.remove(0);
        }
        metrics.updateTimeHistory.add(metrics.updateTime);
        
                if (metrics.updateTimeHistory.size() > 1) {
            metrics.avgUpdateTime = (long) metrics.updateTimeHistory.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(metrics.updateTime);
            
                        long firstTime = metrics.updateTimeHistory.get(0);
            long lastTime = metrics.updateTimeHistory.get(metrics.updateTimeHistory.size() - 1);
            metrics.updateTimeTrend = (lastTime - firstTime) / (double) metrics.updateTimeHistory.size();
        }
        
                boolean heavyByEntities = metrics.entityCount > heavyChunkThreshold;
        boolean heavyByTileEntities = metrics.tileEntityCount > heavyChunkThreshold / 2;
        boolean heavyByUpdateTime = metrics.updateTime > 1_000_000;         boolean heavyByTrend = metrics.updateTimeTrend > 50_000;         
                metrics.isHeavy = heavyByEntities || heavyByTileEntities || heavyByUpdateTime || heavyByTrend;
        
                metrics.heavyReasons.clear();
        if (heavyByEntities) metrics.heavyReasons.add("много сущностей");
        if (heavyByTileEntities) metrics.heavyReasons.add("много блок-сущностей");
        if (heavyByUpdateTime) metrics.heavyReasons.add("долгое время обновления");
        if (heavyByTrend) metrics.heavyReasons.add("ухудшающийся тренд производительности");
        if (metrics.potentialFarm) metrics.heavyReasons.add("возможная ферма");
        
                chunkMetricsMap.put(coord, metrics);
    }
    
    
    private void generateStructure(StructureGenerationTask task) {
        World world = Bukkit.getWorld(task.worldId);
        if (world == null) return;
        
        try {
                        if (!world.isChunkLoaded(task.chunkX, task.chunkZ)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Chunk chunk = world.getChunkAt(task.chunkX, task.chunkZ);
                                                                                LoggerUtil.debug("Выполнена отложенная генерация структуры в чанке [" + 
                            task.chunkX + ", " + task.chunkZ + "] в мире " + world.getName());
                    
                                        ChunkCoord coord = new ChunkCoord(task.worldId, task.chunkX, task.chunkZ);
                    ChunkMetrics metrics = chunkMetricsMap.get(coord);
                    if (metrics != null) {
                        metrics.hasStructures = true;
                    }
                });
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при генерации структуры в чанке [" + 
                    task.chunkX + ", " + task.chunkZ + "]", e);
        }
    }
    
    
    public void queueStructureGeneration(World world, int chunkX, int chunkZ, String structureType) {
        if (!deferStructureGeneration) return;
        
                int priority = 2;         
                if (structureType.equals("village") || structureType.equals("temple")) {
            priority = 1;
        }
        
                StructureGenerationTask task = new StructureGenerationTask(
                world.getUID(), chunkX, chunkZ, structureType, priority);
        
        structureGenQueue.add(task);
        
        LoggerUtil.debug("Поставлена задача на отложенную генерацию структуры " + 
                structureType + " в чанке [" + chunkX + ", " + chunkZ + "] в мире " + world.getName());
    }
    
    
    public ChunkMetrics getChunkMetrics(World world, int x, int z) {
        return chunkMetricsMap.get(new ChunkCoord(world.getUID(), x, z));
    }
    
    
    public List<ChunkCoord> getHeavyChunks() {
        return chunkMetricsMap.entrySet().stream()
                .filter(entry -> entry.getValue().isHeavy)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    
    public static class ChunkCoord {
        public final UUID worldId;
        public final int x;
        public final int z;
        
        public ChunkCoord(UUID worldId, int x, int z) {
            this.worldId = worldId;
            this.x = x;
            this.z = z;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkCoord that = (ChunkCoord) o;
            return x == that.x && z == that.z && worldId.equals(that.worldId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(worldId, x, z);
        }
    }
    
    
    public static class ChunkMetrics {
        public long lastAnalyzed = 0;
        public int entityCount = 0;
        public int tileEntityCount = 0;
        public long updateTime = 0;
        public boolean isHeavy = false;
        public boolean hasStructures = false;
        public Map<String, Integer> entityTypeCount = new HashMap<>();
        public boolean potentialFarm = false;
        public long avgUpdateTime = 0;
        public double updateTimeTrend = 0;
        public List<Long> updateTimeHistory = new ArrayList<>();
        public List<String> heavyReasons = new ArrayList<>();
        
        
        public boolean isHeavy() {
            return isHeavy;
        }
        
        
        public int getHeavinessScore() {
            int score = 0;
            score += Math.min(entityCount * 2, 40);
            score += Math.min(tileEntityCount * 4, 40);
            score += Math.min((int)(updateTime / 100_000), 20);
            return Math.min(score, 100);
        }
    }
    
    
    private static class StructureGenerationTask {
        final UUID worldId;
        final int chunkX;
        final int chunkZ;
        final String structureType;
        final int priority;
        
        public StructureGenerationTask(UUID worldId, int chunkX, int chunkZ, String structureType, int priority) {
            this.worldId = worldId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.structureType = structureType;
            this.priority = priority;
        }
    }
    
    
    private static class ChunkData {
        ChunkPriority priority = ChunkPriority.NORMAL;
        long lastAccessed = System.currentTimeMillis();
        int accessCount = 0;
        int entityCount = 0;
        int tileEntityCount = 0;
        boolean hasComplexStructures = false;
        boolean heavyProcessed = false;         long lastHeavyProcessTime = 0;     }
    
    private static class ChunkTask {
        final World world;
        final int x;
        final int z;
        final ChunkPriority priority;
        final boolean generateIfAbsent;
        
        ChunkTask(World world, int x, int z, ChunkPriority priority, boolean generateIfAbsent) {
            this.world = world;
            this.x = x;
            this.z = z;
            this.priority = priority;
            this.generateIfAbsent = generateIfAbsent;
        }
    }

    
    public boolean isHeavyChunk(Chunk chunk) {
        ChunkCoord coord = new ChunkCoord(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ());
        ChunkMetrics metrics = chunkMetricsMap.get(coord);
        return metrics != null && metrics.isHeavy();
    }

    
    public int optimizeHeavyChunks() {
        int optimized = 0;
        
                List<Map.Entry<ChunkCoord, ChunkMetrics>> heavyChunks = chunkMetricsMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isHeavy())
                .sorted(Comparator.<Map.Entry<ChunkCoord, ChunkMetrics>>comparingInt(
                        entry -> entry.getValue().getHeavinessScore())
                        .reversed())
                .collect(Collectors.toList());
        
        LoggerUtil.debug("Обнаружено " + heavyChunks.size() + " тяжёлых чанков для оптимизации");
        
                for (Map.Entry<ChunkCoord, ChunkMetrics> entry : heavyChunks) {
            ChunkCoord coord = entry.getKey();
            ChunkMetrics metrics = entry.getValue();
            
            World world = Bukkit.getWorld(coord.worldId);
            if (world != null && world.isChunkLoaded(coord.x, coord.z)) {
                Chunk chunk = world.getChunkAt(coord.x, coord.z);
                
                                ChunkData data = chunkDataMap.get(chunk);
                if (data != null) {
                    long currentTime = System.currentTimeMillis();
                                        if (data.heavyProcessed && currentTime - data.lastHeavyProcessTime < 300000) {
                        continue;
                    }
                    
                                        List<String> appliedOptimizations = new ArrayList<>();
                    
                                        data.priority = ChunkPriority.LOW;
                    appliedOptimizations.add("снижен приоритет");
                    
                                        if (metrics.entityCount > heavyChunkThreshold) {
                                                Map.Entry<String, Integer> mostFrequentEntity = metrics.entityTypeCount.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .orElse(null);
                        
                        if (mostFrequentEntity != null && mostFrequentEntity.getValue() > 20) {
                                                        String entityType = mostFrequentEntity.getKey();
                            if (isOptimizableEntityType(entityType)) {
                                                                int removed = limitEntitiesByType(chunk, entityType, 20);
                                if (removed > 0) {
                                    appliedOptimizations.add("удалено " + removed + " сущностей типа " + entityType);
                                }
                            }
                        }
                    }
                    
                                        data.heavyProcessed = true;
                    data.lastHeavyProcessTime = currentTime;
                    
                                        if (!appliedOptimizations.isEmpty()) {
                        LoggerUtil.debug("Оптимизирован тяжёлый чанк [" + coord.x + ", " + coord.z + "] в мире " + 
                                world.getName() + ": " + metrics.entityCount + " сущностей, " + 
                                metrics.tileEntityCount + " тайл-сущностей, оценка тяжести: " + 
                                metrics.getHeavinessScore() + "%, оптимизации: " + String.join(", ", appliedOptimizations));
                        optimized++;
                    }
                }
            }
        }
        
        return optimized;
    }

    
    private boolean isOptimizableEntityType(String entityType) {
                return entityType.equals("ITEM") || 
               entityType.equals("EXPERIENCE_ORB") || 
               entityType.equals("ARROW") ||
               entityType.equals("DROPPED_ITEM") ||
               entityType.equals("FALLING_BLOCK") ||
               entityType.contains("ZOMBIE") ||
               entityType.contains("SKELETON") ||
               entityType.contains("SPIDER");
    }
    
    
    private int limitEntitiesByType(Chunk chunk, String entityType, int maxCount) {
        Entity[] entities = chunk.getEntities();
        List<Entity> entitiesToRemove = Arrays.stream(entities)
                .filter(e -> e.getType().name().equals(entityType))
                .collect(Collectors.toList());
        
        int toRemove = Math.max(0, entitiesToRemove.size() - maxCount);
        if (toRemove > 0) {
                        entitiesToRemove.sort(Comparator.comparingDouble(e -> {
                double centerX = chunk.getX() * 16 + 8;
                double centerZ = chunk.getZ() * 16 + 8;
                return e.getLocation().distanceSquared(new org.bukkit.Location(
                        chunk.getWorld(), centerX, e.getLocation().getY(), centerZ));
            }));
            
            for (int i = 0; i < toRemove; i++) {
                entitiesToRemove.get(i).remove();
            }
        }
        
        return toRemove;
    }
} 