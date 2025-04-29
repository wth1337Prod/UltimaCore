package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.modules.protocoloptimizer.commands.ProtocolOptimizerCommand;
import me.wth.ultimaCore.modules.protocoloptimizer.listeners.PacketListeners;
import me.wth.ultimaCore.modules.protocoloptimizer.listeners.ProtocolListeners;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class ProtocolOptimizerModule extends AbstractModule {
    
    private ProtocolOptimizerSettings settings;
    private PacketFilter packetFilter;
    private PacketLatencyAnalyzer latencyAnalyzer;
    public BlockPacketOptimizer blockPacketOptimizer;
    public EntityPacketOptimizer entityPacketOptimizer;
    
        private ProtocolListeners protocolListeners;
    private PacketListeners packetListeners;
    
        private final Map<UUID, Map<String, Long>> playerActionTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> movementOptimization = new ConcurrentHashMap<>();
    
    
    private final AtomicInteger packetsProcessed = new AtomicInteger(0);
    
    
    private PacketCompressionManager compressionManager;
    
    
    private PacketCacheManager cacheManager;
    
    
    private PacketRateLimiter rateLimiter;
    
    
    public ProtocolOptimizerModule(UltimaCore plugin) {
        super.plugin = plugin;
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                settings = new ProtocolOptimizerSettings(getPlugin(), this);
        settings.loadConfig();
        
                packetFilter = new PacketFilter(this);
        latencyAnalyzer = new PacketLatencyAnalyzer(this);
        blockPacketOptimizer = new BlockPacketOptimizer(this);
        entityPacketOptimizer = new EntityPacketOptimizer(this);
        
                protocolListeners = new ProtocolListeners(this);
        packetListeners = new PacketListeners(this);
        
        Bukkit.getPluginManager().registerEvents(protocolListeners, getPlugin());
        Bukkit.getPluginManager().registerEvents(packetListeners, getPlugin());
        Bukkit.getPluginManager().registerEvents(entityPacketOptimizer, getPlugin());
        Bukkit.getPluginManager().registerEvents(blockPacketOptimizer, getPlugin());
        
                registerCommand();
        
                Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), this::collectNetworkStats, 
                20L, settings.getStatsCollectionInterval());
        Bukkit.getScheduler().runTaskTimerAsynchronously(getPlugin(), this::updateOptimizationState, 
                40L, settings.getOptimizationUpdateInterval());
        
        LoggerUtil.info("&aМодуль оптимизации протокола активирован");
    }
    
    @Override
    public void onDisable() {
                if (latencyAnalyzer != null) {
            latencyAnalyzer.shutdown();
        }
        
        if (blockPacketOptimizer != null) {
            blockPacketOptimizer.clearAllUpdates();
        }
        
        if (entityPacketOptimizer != null) {
            entityPacketOptimizer.shutdown();
        }
        
                playerActionTimes.clear();
        movementOptimization.clear();
        
        LoggerUtil.info("&cМодуль оптимизации протокола деактивирован");
        
        super.onDisable();
    }
    
    
    private void registerCommand() {
        try {
            PluginCommand command = getPlugin().getCommand("protocol");
            if (command != null) {
                ProtocolOptimizerCommand commandExecutor = new ProtocolOptimizerCommand(this);
                command.setExecutor(commandExecutor);
                command.setTabCompleter(commandExecutor);
                LoggerUtil.info("&aКоманда /protocol успешно зарегистрирована");
            } else {
                LoggerUtil.warning("&cНе удалось найти команду 'protocol' для регистрации");
            }
        } catch (Exception e) {
            LoggerUtil.error("&cОшибка при регистрации команды /protocol: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public String getName() {
        return "ProtocolOptimizer";
    }
    
    @Override
    public String getDescription() {
        return "Модуль оптимизации сетевого протокола и пакетов";
    }
    
    
    public void initializePlayerData(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        playerActionTimes.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        movementOptimization.put(playerUUID, false);
    }
    
    
    public void cleanupPlayerData(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        playerActionTimes.remove(playerUUID);
        movementOptimization.remove(playerUUID);
        
        if (packetFilter != null) {
            packetFilter.clearPlayerStats(playerUUID);
        }
    }
    
    
    public void delayInitialPacketsFor(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        long now = System.currentTimeMillis();
        
                playerActionTimes.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                .put("JOIN_TIME", now);
        
                Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
            if (player.isOnline()) {
                                sendInitialPackets(player);
            }
        }, settings.getInitialPacketDelay());
        
        LoggerUtil.debug("Настроена задержка начальных пакетов для " + player.getName());
    }
    
    
    private void sendInitialPackets(Player player) {
        if (player == null || !player.isOnline()) return;
        
                if (entityPacketOptimizer != null) {
            entityPacketOptimizer.prioritizeNearbyEntities(player);
        }
        
        if (blockPacketOptimizer != null) {
            blockPacketOptimizer.sendQueuedUpdatesFor(player);
        }
    }
    
    
    public void optimizeEntityPackets(Entity entity) {
        if (entity == null || entityPacketOptimizer == null) return;
        
                entityPacketOptimizer.registerEntity(entity);
        
                if (entity instanceof Player) {
            Player player = (Player) entity;
                        if (settings.isOptimizeMovementPackets()) {
                markMovementPacketsForOptimization(player);
            }
            
                        entityPacketOptimizer.setUpdateFrequency(player, settings.getEntityUpdateRate());
        }
    }
    
    
    public void optimizeChunkPackets(Chunk chunk) {
        if (chunk == null || blockPacketOptimizer == null) return;
        
                blockPacketOptimizer.registerChunk(chunk);
        
                blockPacketOptimizer.batchBlockUpdates(chunk);
        
                for (Player player : Bukkit.getOnlinePlayers()) {
            if (isChunkInViewDistance(player, chunk)) {
                blockPacketOptimizer.prioritizeUpdatesForPlayer(player, chunk);
            }
        }
    }
    
    
    private boolean isChunkInViewDistance(Player player, Chunk chunk) {
        int viewDistance = getViewDistance();
        Chunk playerChunk = player.getLocation().getChunk();
        
        int distanceX = Math.abs(playerChunk.getX() - chunk.getX());
        int distanceZ = Math.abs(playerChunk.getZ() - chunk.getZ());
        
        return distanceX <= viewDistance && distanceZ <= viewDistance;
    }
    
    
    public void clearChunkData(Chunk chunk) {
        if (chunk == null) return;
        
                if (blockPacketOptimizer != null) {
            blockPacketOptimizer.clearChunkData(chunk);
        }
        
                if (entityPacketOptimizer != null) {
            for (Entity entity : chunk.getEntities()) {
                entityPacketOptimizer.unregisterEntity(entity.getUniqueId());
            }
        }
        
                LoggerUtil.debug("Очищены данные для чанка [" + chunk.getX() + ", " + chunk.getZ() + "]");
    }
    
    
    public void collectNetworkStats() {
        Map<String, Integer> packetTypeCount = new HashMap<>();
        int totalPlayers = Bukkit.getOnlinePlayers().size();
        
        if (packetFilter != null) {
            packetTypeCount = packetFilter.getPacketTypeCounts();
        }
        
                if (latencyAnalyzer != null) {
            latencyAnalyzer.updateNetworkStats(totalPlayers, packetsProcessed.get(), packetTypeCount);
        }
        
                packetsProcessed.set(0);
        
                Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), 
                this::collectNetworkStats, settings.getStatsCollectionInterval());
    }
    
    
    public void updateOptimizationState() {
                double tps = getServerTPS();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        
                if (tps < 18.0) {                         if (rateLimiter != null) {
                rateLimiter.setPacketThreshold("DEFAULT", getRateLimitThreshold() / 2);
            }
            
            if (blockPacketOptimizer != null) {
                blockPacketOptimizer.setMaxBatchSize(Math.max(16, getMaxQueuedBlockUpdates() / 2));
            }
            
            if (entityPacketOptimizer != null) {
                entityPacketOptimizer.setGlobalUpdateFrequency(Math.max(3, settings.getEntityUpdateRate() * 2));
            }
            
            LoggerUtil.debug("Активирован режим высокой оптимизации из-за низкого TPS: " + tps);
        } else if (tps > 19.5 && onlinePlayers < 20) {                         if (rateLimiter != null) {
                rateLimiter.setPacketThreshold("DEFAULT", getRateLimitThreshold());
            }
            
            if (blockPacketOptimizer != null) {
                blockPacketOptimizer.setMaxBatchSize(getMaxQueuedBlockUpdates());
            }
            
            if (entityPacketOptimizer != null) {
                entityPacketOptimizer.setGlobalUpdateFrequency(settings.getEntityUpdateRate());
            }
            
            LoggerUtil.debug("Восстановлены стандартные настройки оптимизации, TPS: " + tps);
        }
        
                Bukkit.getScheduler().runTaskLaterAsynchronously(getPlugin(), 
                this::updateOptimizationState, settings.getOptimizationUpdateInterval());
    }
    
    
    public double getTPS() {
        try {
                        if (Bukkit.getServer() instanceof org.bukkit.Server) {
                try {
                    Class<?> spigotServerClass = Class.forName("org.bukkit.Server");
                    Method getSpigotMethod = spigotServerClass.getDeclaredMethod("spigot");
                    Object spigotServer = getSpigotMethod.invoke(Bukkit.getServer());
                    
                    Method getTpsMethod = spigotServer.getClass().getDeclaredMethod("getTPS");
                    double[] tps = (double[]) getTpsMethod.invoke(spigotServer);
                    
                    return tps[0];                 } catch (Exception e) {
                                        LoggerUtil.debug("Ошибка получения TPS через Spigot API: " + e.getMessage());
                }
            }
            
                        Object server = Bukkit.getServer();
            Method getServerMethod = server.getClass().getMethod("getServer");
            Object minecraftServer = getServerMethod.invoke(server);
            
            Field recentTpsField = minecraftServer.getClass().getField("recentTps");
            double[] tps = (double[]) recentTpsField.get(minecraftServer);
            
            return tps[0];         } catch (Exception e) {
                        LoggerUtil.debug("Ошибка получения TPS: " + e.getMessage());
            return 20.0;
        }
    }
    
    
    private double getServerTPS() {
        try {
            return getTPS();
        } catch (Exception e) {
                        return 20.0;
        }
    }
    
    
    public void markMovementPacketsForOptimization(Player player) {
        if (player == null) return;
        
        movementOptimization.put(player.getUniqueId(), true);
    }
    
    
    public void resetMovementPacketsOptimization(Player player) {
        if (player == null) return;
        
        movementOptimization.put(player.getUniqueId(), false);
    }
    
    
    public void flushPacketsQueue(Player player) {
        if (player == null) return;
        
                if (blockPacketOptimizer != null) {
            blockPacketOptimizer.sendQueuedUpdatesFor(player);
        }
        
                if (entityPacketOptimizer != null) {
            entityPacketOptimizer.sendQueuedUpdatesFor(player);
        }
        
        LoggerUtil.debug("Сброшена очередь пакетов для " + player.getName());
    }
    
    
    public void resetAllPacketsOptimization(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        movementOptimization.put(playerUUID, false);
        playerActionTimes.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).clear();
    }
    
    
    public void optimizeChunkLoadAfterTeleport(Player player, Location location) {
        if (player == null || location == null) return;
        
                Chunk targetChunk = location.getChunk();
        
                int viewDistance = getViewDistance();
        int centerX = targetChunk.getX();
        int centerZ = targetChunk.getZ();
        
                for (int x = centerX - 1; x <= centerX + 1; x++) {
            for (int z = centerZ - 1; z <= centerZ + 1; z++) {
                if (location.getWorld().isChunkLoaded(x, z)) {
                    Chunk chunk = location.getWorld().getChunkAt(x, z);
                    optimizeChunkPackets(chunk);
                }
            }
        }
        
                Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            for (int x = centerX - viewDistance; x <= centerX + viewDistance; x++) {
                for (int z = centerZ - viewDistance; z <= centerZ + viewDistance; z++) {
                                        if (Math.abs(x - centerX) <= 1 && Math.abs(z - centerZ) <= 1) continue;
                    
                                        int distance = Math.max(Math.abs(x - centerX), Math.abs(z - centerZ));
                    
                                        final int chunkX = x;
                    final int chunkZ = z;
                    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
                        if (player.isOnline() && location.getWorld().isChunkLoaded(chunkX, chunkZ)) {
                            Chunk chunk = location.getWorld().getChunkAt(chunkX, chunkZ);
                            optimizeChunkPackets(chunk);
                        }
                    }, distance * 2);                 }
            }
        });
        
        LoggerUtil.debug("Оптимизирована загрузка чанков для телепортации " + player.getName());
    }
    
    
    public boolean isActionSpamming(Player player, String action) {
        if (player == null || action == null) return false;
        
        UUID playerUUID = player.getUniqueId();
        Map<String, Long> actionTimes = playerActionTimes.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
        long now = System.currentTimeMillis();
        Long lastTime = actionTimes.get(action);
        
        if (lastTime == null) {
            actionTimes.put(action, now);
            return false;
        }
        
        long timeDiff = now - lastTime;
        actionTimes.put(action, now);
        
                return timeDiff < 200;
    }
    
    
    public void optimizeActionPackets(Player player, String action) {
        if (player == null || action == null) return;
        UUID playerUUID = player.getUniqueId();
        
                if (isActionSpamming(player, action)) {
                        long now = System.currentTimeMillis();
            Map<String, Long> actionTimes = playerActionTimes.get(playerUUID);
            
            if (actionTimes != null) {
                Long lastOptimizedTime = actionTimes.getOrDefault(action + "_OPTIMIZED", 0L);
                
                                if (now - lastOptimizedTime > settings.getActionOptimizationInterval()) {
                                        if (action.contains("MOVE") || action.contains("LOOK")) {
                                                markMovementPacketsForOptimization(player);
                    } else if (action.contains("INTERACT") || action.contains("USE")) {
                                                getRateLimiter().setPacketThreshold("INTERACTION", 5);
                    } else if (action.contains("CHAT")) {
                                                getRateLimiter().setPacketThreshold("CHAT", 3);
                    }
                    
                                        actionTimes.put(action + "_OPTIMIZED", now);
                    LoggerUtil.debug("Применена оптимизация действия " + action + " для " + player.getName());
                }
            }
        } else {
                        if (action.contains("MOVE") || action.contains("LOOK")) {
                resetMovementPacketsOptimization(player);
            }
        }
    }
    
    
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("&7=== &eСтатистика оптимизации протокола &7===\n");
        
        if (blockPacketOptimizer != null) {
            stats.append("&7Оптимизация блоков:\n");
            stats.append(blockPacketOptimizer.getStats()).append("\n");
        }
        
        if (entityPacketOptimizer != null) {
            stats.append("&7Оптимизация сущностей:\n");
            stats.append(entityPacketOptimizer.getStats()).append("\n");
        }
        
        if (packetFilter != null) {
            stats.append(packetFilter.getStats()).append("\n");
        }
        
        return stats.toString();
    }
    
    
    public String getLatencyStats() {
        if (latencyAnalyzer == null) {
            return "&cАнализатор задержек не инициализирован.";
        }
        
        return latencyAnalyzer.getOverallStats();
    }
    
    
    public String getPlayerLatencyStats(UUID playerUUID) {
        if (latencyAnalyzer == null) {
            return "&cАнализатор задержек не инициализирован.";
        }
        
        return latencyAnalyzer.getPlayerLatencyStats(playerUUID);
    }
    
    
    public String getFilterStats() {
        if (packetFilter == null) {
            return "&cФильтр пакетов не инициализирован.";
        }
        
        return packetFilter.getStats();
    }
    
    
    public void addBlockedPacketType(String packetType) {
        if (packetFilter != null) {
            packetFilter.addBlockedPacketType(packetType);
        }
    }
    
    
    public void removeBlockedPacketType(String packetType) {
        if (packetFilter != null) {
            packetFilter.removeBlockedPacketType(packetType);
        }
    }
    
    
    public void clearBlockedPacketTypes() {
        if (packetFilter != null) {
            packetFilter.clearBlockedPacketTypes();
            LoggerUtil.info("Очищен черный список типов пакетов");
        }
    }
    
    
    public void runManualOptimization() {
                if (compressionManager != null) {
            compressionManager.clearCache();
        }
        
        if (cacheManager != null) {
                        for (UUID playerUUID : cacheManager.getAllPlayerUUIDs()) {
                if (Bukkit.getPlayer(playerUUID) == null) {
                    cacheManager.clearPlayerCache(playerUUID);
                }
            }
        }
        
                if (entityPacketOptimizer != null) {
            entityPacketOptimizer.optimizeAllEntities();
        }
        
                if (blockPacketOptimizer != null) {
            blockPacketOptimizer.sendAllQueuedUpdates();
        }
        
                updateOptimizationState();
        
        LoggerUtil.info("Выполнена ручная оптимизация протокола");
    }
    
    
    public void incrementTrafficSaved(int bytes) {
        if (bytes <= 0) return;
        
                Map<String, Long> stats = new HashMap<>();
        
        if (latencyAnalyzer != null) {
            stats = latencyAnalyzer.getTrafficStats();
        }
        
                long currentSaved = stats.getOrDefault("TRAFFIC_SAVED", 0L);
        stats.put("TRAFFIC_SAVED", currentSaved + bytes);
        
                if (latencyAnalyzer != null) {
            latencyAnalyzer.updateTrafficStats(stats);
        }
        
                if ((currentSaved + bytes) % 1_000_000 < currentSaved % 1_000_000) {             LoggerUtil.info(String.format("Сэкономлено %.2f МБ трафика благодаря оптимизации протокола", 
                    (currentSaved + bytes) / 1_000_000.0));
        }
    }
    
    
    public void incrementPacketsOptimized(int count) {
        if (count <= 0) return;
        
                Map<String, Long> stats = new HashMap<>();
        
        if (latencyAnalyzer != null) {
            stats = latencyAnalyzer.getTrafficStats();
        }
        
                long currentOptimized = stats.getOrDefault("PACKETS_OPTIMIZED", 0L);
        stats.put("PACKETS_OPTIMIZED", currentOptimized + count);
        
                if (latencyAnalyzer != null) {
            latencyAnalyzer.updateTrafficStats(stats);
        }
    }
    
    
    public int getEntityUpdateDistance() {
        return settings != null ? settings.getEntityViewDistance() : 32;
    }
    
    
    public void incrementPacketsProcessed(int count) {
        packetsProcessed.addAndGet(count);
    }
    
    
    public boolean isAsyncProcessing() {
        return settings != null && settings.isOptimizeEntityPackets();
    }
    
    
    public PacketRateLimiter getRateLimiter() {
        if (rateLimiter == null) {
            rateLimiter = new PacketRateLimiter(this);
        }
        return rateLimiter;
    }
    
    
    public boolean isCompressPackets() {
        return settings != null && settings.isOptimizeEntityPackets();
    }
    
    
    public PacketCompressionManager getCompressionManager() {
        if (compressionManager == null) {
            compressionManager = new PacketCompressionManager(this);
        }
        return compressionManager;
    }
    
    
    public PacketCacheManager getCacheManager() {
        if (cacheManager == null) {
            cacheManager = new PacketCacheManager(this);
        }
        return cacheManager;
    }
    
    
    public int getRateLimitThreshold() {
        return settings != null ? 100 : 50;     }
    
    
    public boolean shouldLimit(UUID playerUUID, String packetType) {
        if (rateLimiter != null) {
            return rateLimiter.shouldLimit(playerUUID, packetType);
        }
        return false;
    }
    
    
    public Object compressPacket(String packetType, Object packetData) {
        if (compressionManager != null) {
            return compressionManager.compressPacket(packetType, packetData);
        }
        return packetData;
    }
    
    
    public Object cacheOutboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (cacheManager != null) {
            return cacheManager.cacheOutboundPacket(playerUUID, packetType, packetData);
        }
        return packetData;
    }
    
    
    public Object cacheInboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (cacheManager != null) {
            return cacheManager.cacheInboundPacket(playerUUID, packetType, packetData);
        }
        return packetData;
    }
    
    
    public boolean isPrioritizePackets() {
                        return settings != null && settings.isOptimizeEntityPackets();
    }
    
    
    public int getViewDistance() {
        return settings.getViewDistance();
    }
    
    
    public int getBlockPacketsProcessed() {
        return blockPacketOptimizer != null ? blockPacketOptimizer.getProcessedPackets() : 0;
    }
    
    
    public int getEntityPacketsProcessed() {
        return entityPacketOptimizer != null ? entityPacketOptimizer.getProcessedPackets() : 0;
    }
    
    
    public double getBandwidthSavingPercent() {
        long totalPackets = getBlockPacketsProcessed() + getEntityPacketsProcessed();
        if (totalPackets == 0) return 0.0;
        
        long optimizedPackets = blockPacketOptimizer.getOptimizedPackets() + entityPacketOptimizer.getOptimizedPackets();
        return (double) optimizedPackets / totalPackets * 100.0;
    }
    
    
    public int getSpawnViewDistance() {
        return settings.getEntityViewDistance();
    }
    
    
    public int getMaxQueuedBlockUpdates() {
        return settings != null ? settings.getMaxQueuedBlockUpdates() : 64;
    }
    
    
    public ProtocolOptimizerSettings getSettings() {
        return settings;
    }
    
    
    public Set<UUID> getAllPlayerUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            uuids.add(player.getUniqueId());
        }
        return uuids;
    }
    
    
    public int getCompressionLevel() {
        return settings != null ? settings.getCompressionLevel() : 6;     }
    
    
    public boolean isCachePackets() {
        return settings != null && settings.isCompressPackets();
    }
    
    
    public void clearAllData() {
                playerActionTimes.clear();
        movementOptimization.clear();
        
                if (packetFilter != null) {
            packetFilter.clearBlockedPacketTypes();
        }
        
        if (entityPacketOptimizer != null) {
            entityPacketOptimizer.clearAllData();
        }
        
        if (blockPacketOptimizer != null) {
            blockPacketOptimizer.clearAllUpdates();
        }
        
        if (compressionManager != null) {
            compressionManager.clearCache();
        }
        
        if (cacheManager != null) {
            cacheManager.clearAllCaches();
        }
        
        if (latencyAnalyzer != null) {
            latencyAnalyzer.resetStats();
        }
        
                packetsProcessed.set(0);
        
        LoggerUtil.info("&aВсе данные модуля оптимизации протокола очищены");
    }
} 