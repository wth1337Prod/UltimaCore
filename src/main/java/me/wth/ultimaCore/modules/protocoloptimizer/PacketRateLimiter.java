package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class PacketRateLimiter {
    
    private final ProtocolOptimizerModule module;
    
        private final Map<UUID, Map<String, AtomicInteger>> packetCounters;
    
        private final Map<UUID, Long> lastResetTime;
    
        private final Map<String, Integer> packetThresholds;
    
        private static final long RESET_INTERVAL = 1000;     
    
    public PacketRateLimiter(ProtocolOptimizerModule module) {
        this.module = module;
        this.packetCounters = new ConcurrentHashMap<>();
        this.lastResetTime = new ConcurrentHashMap<>();
        this.packetThresholds = initPacketThresholds();
    }
    
    
    private Map<String, Integer> initPacketThresholds() {
        Map<String, Integer> thresholds = new ConcurrentHashMap<>();
        
                thresholds.put("MOVEMENT", 20);               thresholds.put("INTERACTION", 10);            thresholds.put("CHAT", 5);                    thresholds.put("INVENTORY", 15);              
                thresholds.put("DEFAULT", module.getRateLimitThreshold());
        
        return thresholds;
    }
    
    
    public boolean shouldLimit(UUID playerUUID, String packetType) {
        if (playerUUID == null || packetType == null) {
            return false;
        }
        
                checkAndResetCounters(playerUUID);
        
                packetCounters.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
                packetCounters.get(playerUUID).computeIfAbsent(packetType, k -> new AtomicInteger(0));
        
                int count = packetCounters.get(playerUUID).get(packetType).incrementAndGet();
        
                int threshold = getThresholdForPacketType(packetType);
        
                if (count > threshold) {
            LoggerUtil.debug("Обнаружено превышение скорости пакетов: " + playerUUID + ", тип: " + 
                    packetType + ", количество: " + count + ", порог: " + threshold);
            return true;
        }
        
        return false;
    }
    
    
    private void checkAndResetCounters(UUID playerUUID) {
        long currentTime = System.currentTimeMillis();
        long lastReset = lastResetTime.getOrDefault(playerUUID, 0L);
        
                if (currentTime - lastReset > RESET_INTERVAL) {
                        if (packetCounters.containsKey(playerUUID)) {
                packetCounters.get(playerUUID).forEach((type, counter) -> counter.set(0));
            }
            
                        lastResetTime.put(playerUUID, currentTime);
        }
    }
    
    
    private int getThresholdForPacketType(String packetType) {
                if (packetThresholds.containsKey(packetType)) {
            return packetThresholds.get(packetType);
        }
        
                if (packetType.contains("MOVE") || packetType.contains("POSITION") || packetType.contains("LOOK")) {
            return packetThresholds.get("MOVEMENT");
        } else if (packetType.contains("INTERACT") || packetType.contains("USE")) {
            return packetThresholds.get("INTERACTION");
        } else if (packetType.contains("CHAT") || packetType.contains("MESSAGE")) {
            return packetThresholds.get("CHAT");
        } else if (packetType.contains("INVENTORY") || packetType.contains("WINDOW") || packetType.contains("ITEM")) {
            return packetThresholds.get("INVENTORY");
        }
        
                return packetThresholds.get("DEFAULT");
    }
    
    
    public void setPacketThreshold(String packetType, int threshold) {
        if (packetType != null && threshold > 0) {
            packetThresholds.put(packetType, threshold);
        }
    }
    
    
    public void clearPlayerData(UUID playerUUID) {
        if (playerUUID != null) {
            packetCounters.remove(playerUUID);
            lastResetTime.remove(playerUUID);
        }
    }
    
    
    public void clearAllData() {
        packetCounters.clear();
        lastResetTime.clear();
    }
    
    
    public int getCurrentCount(UUID playerUUID, String packetType) {
        if (playerUUID == null || packetType == null) {
            return 0;
        }
        
        if (!packetCounters.containsKey(playerUUID) || !packetCounters.get(playerUUID).containsKey(packetType)) {
            return 0;
        }
        
        return packetCounters.get(playerUUID).get(packetType).get();
    }
    
    
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("&7=== &eСтатистика ограничителя пакетов &7===\n");
        
                int activePlayerCount = packetCounters.size();
        stats.append("&7Игроков с активными ограничениями: &e").append(activePlayerCount).append("\n");
        
                Map<String, Integer> limitedByType = new ConcurrentHashMap<>();
        
        packetCounters.forEach((uuid, typeMap) -> {
            typeMap.forEach((type, count) -> {
                int threshold = getThresholdForPacketType(type);
                if (count.get() > threshold) {
                    limitedByType.compute(type, (k, v) -> (v == null) ? 1 : v + 1);
                }
            });
        });
        
        if (!limitedByType.isEmpty()) {
            stats.append("&7Ограниченные типы пакетов:\n");
            limitedByType.forEach((type, count) -> {
                stats.append("  &e").append(type).append("&7: &c").append(count).append("\n");
            });
        }
        
        return stats.toString();
    }
} 