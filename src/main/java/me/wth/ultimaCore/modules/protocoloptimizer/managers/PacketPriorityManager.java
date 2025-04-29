package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PacketPriorityManager {
    private final ProtocolOptimizerModule module;
    
        private final ConcurrentHashMap<UUID, PriorityQueues> playerQueues = new ConcurrentHashMap<>();
    
        private final ScheduledExecutorService scheduler;
    
        private boolean running = true;
    
    
    public PacketPriorityManager(ProtocolOptimizerModule module) {
        this.module = module;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "UltimaCore-PacketPriorityThread");
            thread.setDaemon(true);
            return thread;
        });
        
                startQueueProcessor();
    }
    
    
    private void startQueueProcessor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (!running || !module.isPrioritizePackets()) {
                    return;
                }
                
                                for (UUID playerUUID : playerQueues.keySet()) {
                    processPlayerQueues(playerUUID);
                }
            } catch (Exception e) {
                LoggerUtil.warning("Ошибка при обработке очередей пакетов: " + e.getMessage());
            }
        }, 50, 50, TimeUnit.MILLISECONDS);     }
    
    
    private void processPlayerQueues(UUID playerUUID) {
        PriorityQueues queues = playerQueues.get(playerUUID);
        if (queues == null) {
            return;
        }
        
                processQueue(queues.criticalQueue);
        
                processQueueLimited(queues.highQueue, 20);
        
                processQueueLimited(queues.mediumQueue, 10);
        
                processQueueLimited(queues.lowQueue, 5);
    }
    
    
    private void processQueue(ConcurrentLinkedQueue<QueuedPacket> queue) {
        QueuedPacket packet;
        while ((packet = queue.poll()) != null) {
                                    if (Math.random() < 0.01) {                 LoggerUtil.info("Обработан пакет " + packet.getPacketType() + 
                        " для игрока " + packet.getPlayerUUID());
            }
        }
    }
    
    
    private void processQueueLimited(ConcurrentLinkedQueue<QueuedPacket> queue, int limit) {
        for (int i = 0; i < limit && !queue.isEmpty(); i++) {
            QueuedPacket packet = queue.poll();
            if (packet != null) {
                                                if (Math.random() < 0.01) {                     LoggerUtil.info("Обработан пакет " + packet.getPacketType() + 
                            " для игрока " + packet.getPlayerUUID());
                }
            }
        }
    }
    
    
    public void queuePacket(UUID playerUUID, String packetType, Object packetData, PacketPriority priority) {
        if (!module.isPrioritizePackets() || packetData == null) {
            return;
        }
        
        try {
                        PriorityQueues queues = playerQueues.computeIfAbsent(
                    playerUUID, id -> new PriorityQueues());
            
                        QueuedPacket queuedPacket = new QueuedPacket(playerUUID, packetType, packetData);
            
                        switch (priority) {
                case CRITICAL:
                    queues.criticalQueue.add(queuedPacket);
                    break;
                case HIGH:
                    queues.highQueue.add(queuedPacket);
                    break;
                case MEDIUM:
                    queues.mediumQueue.add(queuedPacket);
                    break;
                case LOW:
                    queues.lowQueue.add(queuedPacket);
                    break;
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при добавлении пакета в очередь: " + e.getMessage());
        }
    }
    
    
    public void clearPlayerQueues(UUID playerUUID) {
        playerQueues.remove(playerUUID);
    }
    
    
    public void shutdown() {
        running = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        playerQueues.clear();
    }
    
    
    public int getQueueSize(UUID playerUUID) {
        PriorityQueues queues = playerQueues.get(playerUUID);
        if (queues == null) {
            return 0;
        }
        
        return queues.criticalQueue.size() + queues.highQueue.size() + 
               queues.mediumQueue.size() + queues.lowQueue.size();
    }
    
    
    public int getTotalQueueSize() {
        int total = 0;
        for (PriorityQueues queues : playerQueues.values()) {
            total += queues.criticalQueue.size() + queues.highQueue.size() + 
                     queues.mediumQueue.size() + queues.lowQueue.size();
        }
        return total;
    }
    
    
    private static class PriorityQueues {
        public final ConcurrentLinkedQueue<QueuedPacket> criticalQueue = new ConcurrentLinkedQueue<>();
        public final ConcurrentLinkedQueue<QueuedPacket> highQueue = new ConcurrentLinkedQueue<>();
        public final ConcurrentLinkedQueue<QueuedPacket> mediumQueue = new ConcurrentLinkedQueue<>();
        public final ConcurrentLinkedQueue<QueuedPacket> lowQueue = new ConcurrentLinkedQueue<>();
    }
    
    
    private static class QueuedPacket {
        private final UUID playerUUID;
        private final String packetType;
        private final Object packetData;
        private final long timestamp;
        
        
        public QueuedPacket(UUID playerUUID, String packetType, Object packetData) {
            this.playerUUID = playerUUID;
            this.packetType = packetType;
            this.packetData = packetData;
            this.timestamp = System.currentTimeMillis();
        }
        
        
        public UUID getPlayerUUID() {
            return playerUUID;
        }
        
        
        public String getPacketType() {
            return packetType;
        }
        
        
        public Object getPacketData() {
            return packetData;
        }
        
        
        public long getTimestamp() {
            return timestamp;
        }
    }
} 