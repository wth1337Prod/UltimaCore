package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class PacketManager {
    private final ProtocolOptimizerModule module;
    private ExecutorService packetExecutor;
    private final Map<UUID, PlayerPacketProcessor> playerProcessors = new ConcurrentHashMap<>();
    private final Map<String, PacketDefinition> packetDefinitions = new HashMap<>();
    private boolean initialized = false;
    
    
    public PacketManager(ProtocolOptimizerModule module) {
        this.module = module;
        initPacketDefinitions();
        initExecutorService();
    }
    
    
    private void initExecutorService() {
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        
        ThreadFactory factory = new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "UltimaCore-PacketThread-" + counter.getAndIncrement());
                thread.setDaemon(true);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };
        
        packetExecutor = Executors.newFixedThreadPool(threads, factory);
        LoggerUtil.info("Инициализирован пул потоков для пакетов: " + threads + " потоков");
    }
    
    
    private void initPacketDefinitions() {
                        addPacketDefinition("PLAY_OUT_ENTITY", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_ENTITY_LOOK", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_ENTITY_HEAD_ROTATION", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_ENTITY_METADATA", PacketPriority.HIGH, true, true);
        addPacketDefinition("PLAY_OUT_ENTITY_TELEPORT", PacketPriority.HIGH, false, true);
        addPacketDefinition("PLAY_OUT_POSITION", PacketPriority.CRITICAL, false, false);
        addPacketDefinition("PLAY_OUT_MAP_CHUNK", PacketPriority.HIGH, true, false);
        addPacketDefinition("PLAY_OUT_MAP_CHUNK_BULK", PacketPriority.HIGH, true, false);
        addPacketDefinition("PLAY_OUT_BLOCK_CHANGE", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_MULTI_BLOCK_CHANGE", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_ANIMATION", PacketPriority.LOW, true, true);
        addPacketDefinition("PLAY_OUT_CHAT", PacketPriority.MEDIUM, true, true);
        addPacketDefinition("PLAY_OUT_KEEP_ALIVE", PacketPriority.CRITICAL, false, false);
        
                addPacketDefinition("PLAY_IN_POSITION", PacketPriority.HIGH, false, false);
        addPacketDefinition("PLAY_IN_POSITION_LOOK", PacketPriority.HIGH, false, false);
        addPacketDefinition("PLAY_IN_LOOK", PacketPriority.MEDIUM, false, false);
        addPacketDefinition("PLAY_IN_FLYING", PacketPriority.LOW, true, false);
        addPacketDefinition("PLAY_IN_KEEP_ALIVE", PacketPriority.CRITICAL, false, false);
        addPacketDefinition("PLAY_IN_CHAT", PacketPriority.MEDIUM, false, false);
        addPacketDefinition("PLAY_IN_BLOCK_DIG", PacketPriority.HIGH, false, false);
        addPacketDefinition("PLAY_IN_BLOCK_PLACE", PacketPriority.HIGH, false, false);
        addPacketDefinition("PLAY_IN_ARM_ANIMATION", PacketPriority.LOW, true, false);
        
        initialized = true;
        LoggerUtil.info("Инициализировано " + packetDefinitions.size() + " определений пакетов");
    }
    
    
    public void addPacketDefinition(String packetType, PacketPriority priority, boolean canBulk, boolean canCache) {
        packetDefinitions.put(packetType, new PacketDefinition(packetType, priority, canBulk, canCache));
    }
    
    
    public PacketDefinition getPacketDefinition(String packetType) {
        return packetDefinitions.getOrDefault(packetType, 
                new PacketDefinition(packetType, PacketPriority.MEDIUM, false, false));
    }
    
    
    public Object processOutboundPacket(Player player, String packetType, Object packetData) {
        if (!initialized) return packetData;
        
        UUID playerUUID = player.getUniqueId();
        PlayerPacketProcessor processor = getPlayerProcessor(playerUUID);
        PacketDefinition definition = getPacketDefinition(packetType);
        
        module.incrementPacketsProcessed(1);
        
                if (module.isAsyncProcessing() && definition.getPriority() != PacketPriority.CRITICAL) {
                        packetExecutor.submit(() -> {
                try {
                    Object processedData = processor.processOutbound(packetType, packetData, definition);
                                    } catch (Exception e) {
                    LoggerUtil.warning("Ошибка при асинхронной обработке исходящего пакета " + packetType, e);
                }
            });
            return packetData;
        } else {
                        try {
                return processor.processOutbound(packetType, packetData, definition);
            } catch (Exception e) {
                LoggerUtil.warning("Ошибка при обработке исходящего пакета " + packetType, e);
                return packetData;
            }
        }
    }
    
    
    public Object processInboundPacket(Player player, String packetType, Object packetData) {
        if (!initialized) return packetData;
        
        UUID playerUUID = player.getUniqueId();
        PlayerPacketProcessor processor = getPlayerProcessor(playerUUID);
        PacketDefinition definition = getPacketDefinition(packetType);
        
        module.incrementPacketsProcessed(1);
        
                if (module.shouldLimit(playerUUID, packetType)) {
            return null;         }
        
                try {
            return processor.processInbound(packetType, packetData, definition);
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при обработке входящего пакета " + packetType, e);
            return packetData;
        }
    }
    
    
    private PlayerPacketProcessor getPlayerProcessor(UUID playerUUID) {
        return playerProcessors.computeIfAbsent(playerUUID, id -> new PlayerPacketProcessor(module, id));
    }
    
    
    public void removePlayerProcessor(UUID playerUUID) {
        playerProcessors.remove(playerUUID);
    }
    
    
    public void shutdown() {
        if (packetExecutor != null && !packetExecutor.isShutdown()) {
            packetExecutor.shutdown();
        }
        playerProcessors.clear();
    }
    
    
    private class PlayerPacketProcessor {
        private final ProtocolOptimizerModule module;
        private final UUID playerUUID;
        private final Map<String, Long> lastPacketTimes = new HashMap<>();
        
        
        public PlayerPacketProcessor(ProtocolOptimizerModule module, UUID playerUUID) {
            this.module = module;
            this.playerUUID = playerUUID;
        }
        
        
        public Object processOutbound(String packetType, Object packetData, PacketDefinition definition) {
                        if (isDuplicatePacket(packetType, System.currentTimeMillis())) {
                return null;
            }
            
                        Object optimizedData = packetData;
            
                        if (module.isCompressPackets() && definition.isCanBulk()) {
                optimizedData = module.compressPacket(packetType, optimizedData);
            }
            
                        if (module.isCachePackets() && definition.isCanCache()) {
                optimizedData = module.cacheOutboundPacket(playerUUID, packetType, optimizedData);
            }
            
            return optimizedData;
        }
        
        
        public Object processInbound(String packetType, Object packetData, PacketDefinition definition) {
                        if (isDuplicatePacket(packetType, System.currentTimeMillis())) {
                return null;
            }
            
                        if (module.isCachePackets() && definition.isCanCache()) {
                packetData = module.cacheInboundPacket(playerUUID, packetType, packetData);
            }
            
            return packetData;
        }
        
        
        private boolean isDuplicatePacket(String packetType, long time) {
                        if (!packetType.contains("ENTITY") && !packetType.contains("FLYING")) {
                return false;
            }
            
            Long lastTime = lastPacketTimes.get(packetType);
            
            if (lastTime != null) {
                long diff = time - lastTime;
                                if (diff < 10) {
                    return true;
                }
            }
            
            lastPacketTimes.put(packetType, time);
            return false;
        }
    }
} 