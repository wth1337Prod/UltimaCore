package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class PacketRateLimiter {
    private final ProtocolOptimizerModule module;
    
        private final Map<UUID, Map<String, RateInfo>> playerPacketRates = new ConcurrentHashMap<>();
    
        private final Map<String, Integer> packetLimits = new HashMap<>();
    
        private int limitedPackets = 0;
    
    
    public PacketRateLimiter(ProtocolOptimizerModule module) {
        this.module = module;
        initPacketLimits();
    }
    
    
    private void initPacketLimits() {
                packetLimits.put("PLAY_IN_POSITION", 30);                packetLimits.put("PLAY_IN_POSITION_LOOK", 30);           packetLimits.put("PLAY_IN_LOOK", 30);                    packetLimits.put("PLAY_IN_ARM_ANIMATION", 20);           packetLimits.put("PLAY_IN_BLOCK_DIG", 15);               packetLimits.put("PLAY_IN_BLOCK_PLACE", 15);             packetLimits.put("PLAY_IN_CHAT", 5);                     
        LoggerUtil.debug("Инициализированы лимиты для " + packetLimits.size() + " типов пакетов");
    }
    
    
    public boolean shouldLimit(UUID playerUUID, String packetType) {
                Integer limit = packetLimits.get(packetType);
        if (limit == null) {
                        return false;
        }
        
                Map<String, RateInfo> playerRates = playerPacketRates.computeIfAbsent(
                playerUUID, k -> new ConcurrentHashMap<>());
        
                RateInfo rateInfo = playerRates.computeIfAbsent(
                packetType, k -> new RateInfo());
        
                boolean shouldLimit = rateInfo.incrementAndCheck(limit);
        
        if (shouldLimit) {
            limitedPackets++;
                        LoggerUtil.debug("Ограничен пакет " + packetType + " для игрока " + playerUUID);
        }
        
        return shouldLimit;
    }
    
    
    public void clearPlayerData(UUID playerUUID) {
        playerPacketRates.remove(playerUUID);
    }
    
    
    public void clearAllData() {
        playerPacketRates.clear();
        limitedPackets = 0;
    }
    
    
    public int getLimitedPacketsCount() {
        return limitedPackets;
    }
    
    
    private static class RateInfo {
        private int count = 0;
        private long lastReset = System.currentTimeMillis();
        
        
        public boolean incrementAndCheck(int limit) {
            long now = System.currentTimeMillis();
            
                        if (now - lastReset >= 1000) {
                count = 0;
                lastReset = now;
            }
            
                        count++;
            
                        return count > limit;
        }
    }
} 