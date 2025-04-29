package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.HashSet;


public class EntityPacketOptimizer implements Listener {
    private final ProtocolOptimizerModule module;
    private final ProtocolOptimizerSettings settings;
    
        private final Map<UUID, Map<Integer, Long>> lastUpdateTimes = new ConcurrentHashMap<>();
    
        private final Map<Integer, Location> lastEntityLocations = new ConcurrentHashMap<>();
    
        private final AtomicInteger packetsSent = new AtomicInteger(0);
    private final AtomicInteger packetsSkipped = new AtomicInteger(0);
    
        private BukkitTask cleanupTask;
    private BukkitTask entityCheckTask;
    
        private final Map<UUID, Entity> trackedEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> playerVisibleEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> entityUpdateFrequency = new ConcurrentHashMap<>();
    
        private final AtomicInteger optimizedEntityUpdates = new AtomicInteger(0);
    private final AtomicInteger totalEntityUpdates = new AtomicInteger(0);
    private int globalUpdateFrequency = 4;     
    
    public EntityPacketOptimizer(ProtocolOptimizerModule module) {
        this.module = module;
        this.settings = module.getSettings();
        
                this.cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            module.getPlugin(), 
            this::cleanup, 
            1200L,             1200L          );
        
                this.entityCheckTask = Bukkit.getScheduler().runTaskTimer(
            module.getPlugin(),
            this::checkEntityMovements,
            20L,             10L          );
        
        this.globalUpdateFrequency = module.getSettings().getEntityUpdateRate();
        
                Bukkit.getScheduler().runTaskTimerAsynchronously(module.getPlugin(), 
                this::processEntityUpdates, 20L, 20L);
        
        LoggerUtil.info("&aОптимизатор пакетов сущностей инициализирован. Интервал обновления: " + 
                        settings.getEntityUpdateInterval() + "мс");
    }
    
    
    private void checkEntityMovements() {
        if (!settings.isEnableEntityPacketOptimization() || !settings.isOptimizeEntityPackets()) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
                for (World world : Bukkit.getWorlds()) {
                        for (Entity entity : world.getEntities()) {
                if (entity == null || entity instanceof Player) {
                    continue;                 }
                
                int entityId = entity.getEntityId();
                Location currentLocation = entity.getLocation();
                Location lastLocation = lastEntityLocations.get(entityId);
                
                                if (lastLocation == null || !locationEquals(lastLocation, currentLocation)) {
                                        lastEntityLocations.put(entityId, currentLocation.clone());
                    
                                        for (Player player : Bukkit.getOnlinePlayers()) {
                                                if (!player.getWorld().equals(entity.getWorld())) {
                            continue;
                        }
                        
                                                if (!isEntityVisible(player, entity)) {
                            continue;
                        }
                        
                                                Map<Integer, Long> entityUpdateTimes = lastUpdateTimes
                            .computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>());
                        
                        Long lastUpdate = entityUpdateTimes.get(entityId);
                        
                                                if (lastUpdate == null || (currentTime - lastUpdate) >= settings.getEntityUpdateInterval()) {
                                                        entityUpdateTimes.put(entityId, currentTime);
                            
                                                                                                                
                            packetsSent.incrementAndGet();
                        } else {
                                                        packetsSkipped.incrementAndGet();
                        }
                    }
                }
            }
        }
    }
    
    
    private boolean locationEquals(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && 
               loc1.getBlockY() == loc2.getBlockY() && 
               loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    
    private boolean isEntityVisible(Player player, Entity entity) {
        Location playerLoc = player.getLocation();
        Location entityLoc = entity.getLocation();
        
                double distance = playerLoc.distance(entityLoc);
        return distance <= module.getEntityUpdateDistance();
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        lastUpdateTimes.remove(playerUUID);
    }
    
    
    private void cleanup() {
        try {
                        for (Map.Entry<UUID, Map<Integer, Long>> entry : lastUpdateTimes.entrySet()) {
                UUID playerUUID = entry.getKey();
                Map<Integer, Long> entityTimes = entry.getValue();
                
                                Player player = Bukkit.getPlayer(playerUUID);
                if (player == null || !player.isOnline()) {
                    lastUpdateTimes.remove(playerUUID);
                    continue;
                }
                
                                Map<Integer, Entity> worldEntities = new HashMap<>();
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        worldEntities.put(entity.getEntityId(), entity);
                    }
                }
                
                                entityTimes.entrySet().removeIf(entityEntry -> {
                    int entityId = entityEntry.getKey();
                    return !worldEntities.containsKey(entityId);
                });
            }
            
                        Map<Integer, Entity> worldEntities = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    worldEntities.put(entity.getEntityId(), entity);
                }
            }
            
            lastEntityLocations.entrySet().removeIf(entry -> !worldEntities.containsKey(entry.getKey()));
            
        } catch (Exception e) {
            LoggerUtil.error("&cОшибка при очистке данных оптимизатора пакетов сущностей: " + e.getMessage());
        }
    }
    
    
    public int getProcessedPackets() {
        return totalEntityUpdates.get();
    }
    
    
    public int getOptimizedPackets() {
        return optimizedEntityUpdates.get();
    }
    
    
    public String getStatistics() {
        StringBuilder stats = new StringBuilder();
        
        int sent = packetsSent.get();
        int skipped = packetsSkipped.get();
        int total = sent + skipped;
        
        double skipPercent = total > 0 ? (skipped * 100.0 / total) : 0;
        
        stats.append("&7=== Статистика оптимизации пакетов сущностей ===\n");
        stats.append("&7Текущие настройки: интервал обновления &e").append(settings.getEntityUpdateInterval()).append("мс");
        stats.append("&7, дистанция видимости &e").append(settings.getEntityViewDistance()).append(" &7блоков\n");
        stats.append("&7- Отправлено пакетов: &a").append(sent).append("\n");
        stats.append("&7- Пропущено пакетов: &a").append(skipped).append(" &7(").append(String.format("%.2f", skipPercent)).append("%)\n");
        stats.append("&7- Всего пакетов: &e").append(total).append("\n");
        stats.append("&7- Активных отслеживаний: &e").append(countTracking()).append("\n");
        stats.append("&7- Отслеживаемых сущностей: &e").append(lastEntityLocations.size()).append("\n");
        
        return stats.toString();
    }
    
    
    private int countTracking() {
        int count = 0;
        for (Map<Integer, Long> entityMap : lastUpdateTimes.values()) {
            count += entityMap.size();
        }
        return count;
    }
    
    
    public void resetStatistics() {
        packetsSent.set(0);
        packetsSkipped.set(0);
    }
    
    
    public void clearAllData() {
                lastUpdateTimes.clear();
        lastEntityLocations.clear();
        trackedEntities.clear();
        playerVisibleEntities.clear();
        entityUpdateFrequency.clear();
        
                packetsSent.set(0);
        packetsSkipped.set(0);
        optimizedEntityUpdates.set(0);
        totalEntityUpdates.set(0);
        
        LoggerUtil.debug("Очищены все данные оптимизатора пакетов сущностей");
    }
    
    
    public void clearCaches() {
        lastUpdateTimes.clear();
        lastEntityLocations.clear();
        resetStatistics();
        LoggerUtil.info("&aКэши оптимизатора пакетов сущностей очищены");
    }
    
    
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        
        if (entityCheckTask != null) {
            entityCheckTask.cancel();
            entityCheckTask = null;
        }
        
        clearAllData();
    }
    
    
    public void queueBlockUpdate(Block block) {
                    }
    
    
    public String getStats() {
        return getStatistics();
    }
    
    
    private void processEntityUpdates() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Set<UUID> visibleEntities = getVisibleEntitiesForPlayer(player);
            
            for (UUID entityUUID : visibleEntities) {
                Entity entity = trackedEntities.get(entityUUID);
                if (entity != null && entity.isValid()) {
                                        if (shouldSendEntityUpdate(entity)) {
                                                totalEntityUpdates.incrementAndGet();
                    } else {
                        optimizedEntityUpdates.incrementAndGet();
                    }
                }
            }
        }
    }
    
    
    private boolean shouldSendEntityUpdate(Entity entity) {
                return (System.currentTimeMillis() % (entityUpdateFrequency.getOrDefault(entity.getUniqueId(), globalUpdateFrequency) * 50)) == 0;
    }
    
    
    private Set<UUID> getVisibleEntitiesForPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        playerVisibleEntities.putIfAbsent(playerUUID, new HashSet<>());
        return playerVisibleEntities.get(playerUUID);
    }
    
    
    public void registerEntity(Entity entity) {
        if (entity == null) return;
        
        UUID entityUUID = entity.getUniqueId();
        trackedEntities.put(entityUUID, entity);
        
                if (!entityUpdateFrequency.containsKey(entityUUID)) {
            entityUpdateFrequency.put(entityUUID, globalUpdateFrequency);
        }
        
                for (Player player : Bukkit.getOnlinePlayers()) {
            if (isEntityVisibleForPlayer(entity, player)) {
                UUID playerUUID = player.getUniqueId();
                playerVisibleEntities.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(entityUUID);
            }
        }
    }
    
    
    public void unregisterEntity(UUID entityUUID) {
        if (entityUUID == null) return;
        
        trackedEntities.remove(entityUUID);
        entityUpdateFrequency.remove(entityUUID);
        
                for (Set<UUID> visibleEntities : playerVisibleEntities.values()) {
            visibleEntities.remove(entityUUID);
        }
    }
    
    
    private boolean isEntityVisibleForPlayer(Entity entity, Player player) {
                return entity.getWorld().equals(player.getWorld()) && 
               entity.getLocation().distance(player.getLocation()) <= module.getEntityUpdateDistance();
    }
    
    
    public void setUpdateFrequency(Entity entity, int frequency) {
        if (entity == null) return;
        entityUpdateFrequency.put(entity.getUniqueId(), frequency);
    }
    
    
    public void setGlobalUpdateFrequency(int frequency) {
        this.globalUpdateFrequency = frequency;
    }
    
    
    public void prioritizeNearbyEntities(Player player) {
        if (player == null) return;
        
                for (Entity entity : player.getNearbyEntities(16, 16, 16)) {
            setUpdateFrequency(entity, 1);             
                        UUID playerUUID = player.getUniqueId();
            playerVisibleEntities.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(entity.getUniqueId());
        }
    }
    
    
    public void sendQueuedUpdatesFor(Player player) {
        if (player == null) return;
        
                LoggerUtil.debug("Отправлены накопленные обновления сущностей для " + player.getName());
    }
    
    
    public void optimizeAllEntities() {
        for (Entity entity : trackedEntities.values()) {
            if (entity != null && entity.isValid()) {
                                int frequency = determineOptimalUpdateFrequency(entity);
                setUpdateFrequency(entity, frequency);
            }
        }
        
        LoggerUtil.debug("Оптимизировано " + trackedEntities.size() + " сущностей");
    }
    
    
    private int determineOptimalUpdateFrequency(Entity entity) {
                        switch (entity.getType()) {
            case PLAYER:
                return 1;             case ZOMBIE:
            case SKELETON:
            case CREEPER:
                return 2;             default:
                return globalUpdateFrequency;         }
    }
    
        
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        registerEntity(event.getEntity());
    }
    
    @EventHandler
    public void onEntityDespawn(EntityDeathEvent event) {
        unregisterEntity(event.getEntity().getUniqueId());
    }
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            registerEntity(entity);
        }
    }
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            unregisterEntity(entity.getUniqueId());
        }
    }
    
    
    public Entity getEntity(UUID uuid) {
        return trackedEntities.get(uuid);
    }
} 