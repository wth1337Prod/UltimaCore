package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;


public class PacketCacheManager {
    
    private final ProtocolOptimizerModule module;
    private final Map<UUID, Map<String, Object>> outboundPacketCache;
    private final Map<UUID, Map<String, Object>> inboundPacketCache;
    private final Map<String, Integer> cacheHits;
    private final Map<String, Integer> cacheMisses;
    
        private static final int MAX_CACHE_SIZE_PER_PLAYER = 1000;
    
    
    public PacketCacheManager(ProtocolOptimizerModule module) {
        this.module = module;
        this.outboundPacketCache = new ConcurrentHashMap<>();
        this.inboundPacketCache = new ConcurrentHashMap<>();
        this.cacheHits = new ConcurrentHashMap<>();
        this.cacheMisses = new ConcurrentHashMap<>();
    }
    
    
    public Object cacheOutboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (playerUUID == null || packetType == null || packetData == null) {
            return packetData;
        }
        
                outboundPacketCache.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
                String cacheKey = generateCacheKey(packetType, packetData);
        
                if (outboundPacketCache.get(playerUUID).containsKey(cacheKey)) {
            incrementCacheHits(packetType);
            return outboundPacketCache.get(playerUUID).get(cacheKey);
        }
        
                incrementCacheMisses(packetType);
        
                if (outboundPacketCache.get(playerUUID).size() >= MAX_CACHE_SIZE_PER_PLAYER) {
            cleanCache(playerUUID, true);
        }
        
        outboundPacketCache.get(playerUUID).put(cacheKey, packetData);
        return packetData;
    }
    
    
    public Object cacheInboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (playerUUID == null || packetType == null || packetData == null) {
            return packetData;
        }
        
                inboundPacketCache.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        
                String cacheKey = generateCacheKey(packetType, packetData);
        
                if (inboundPacketCache.get(playerUUID).containsKey(cacheKey)) {
            incrementCacheHits(packetType);
            return inboundPacketCache.get(playerUUID).get(cacheKey);
        }
        
                incrementCacheMisses(packetType);
        
                if (inboundPacketCache.get(playerUUID).size() >= MAX_CACHE_SIZE_PER_PLAYER) {
            cleanCache(playerUUID, false);
        }
        
        inboundPacketCache.get(playerUUID).put(cacheKey, packetData);
        return packetData;
    }
    
    
    private String generateCacheKey(String packetType, Object packetData) {
        return packetType + "_" + (packetData.hashCode());
    }
    
    
    private void cleanCache(UUID playerUUID, boolean outbound) {
        Map<UUID, Map<String, Object>> cacheToClean = outbound ? outboundPacketCache : inboundPacketCache;
        
        if (cacheToClean.containsKey(playerUUID)) {
                                                int cacheSize = cacheToClean.get(playerUUID).size();
            int itemsToRemove = cacheSize / 2;
            
            int removed = 0;
            for (String key : cacheToClean.get(playerUUID).keySet()) {
                cacheToClean.get(playerUUID).remove(key);
                removed++;
                if (removed >= itemsToRemove) break;
            }
            
            LoggerUtil.debug("Очищена половина кэша пакетов для игрока " + playerUUID);
        }
    }
    
    
    private void incrementCacheHits(String packetType) {
        cacheHits.compute(packetType, (k, v) -> (v == null) ? 1 : v + 1);
    }
    
    
    private void incrementCacheMisses(String packetType) {
        cacheMisses.compute(packetType, (k, v) -> (v == null) ? 1 : v + 1);
    }
    
    
    public void clearPlayerCache(UUID playerUUID) {
        if (playerUUID != null) {
            outboundPacketCache.remove(playerUUID);
            inboundPacketCache.remove(playerUUID);
        }
    }
    
    
    public void clearAllCaches() {
        outboundPacketCache.clear();
        inboundPacketCache.clear();
        cacheHits.clear();
        cacheMisses.clear();
    }
    
    
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("&7=== &eСтатистика кэширования пакетов &7===\n");
        
        int totalHits = cacheHits.values().stream().mapToInt(Integer::intValue).sum();
        int totalMisses = cacheMisses.values().stream().mapToInt(Integer::intValue).sum();
        int totalRequests = totalHits + totalMisses;
        
        stats.append("&7Всего запросов: &e").append(totalRequests).append("\n");
        stats.append("&7Попаданий в кэш: &a").append(totalHits).append(" (");
        
        if (totalRequests > 0) {
            stats.append(String.format("%.2f", (double) totalHits / totalRequests * 100)).append("%)\n");
        } else {
            stats.append("0%)\n");
        }
        
        stats.append("&7Промахов кэша: &c").append(totalMisses).append("\n");
        stats.append("&7Размер кэша исходящих пакетов: &e").append(getOutboundCacheSize()).append("\n");
        stats.append("&7Размер кэша входящих пакетов: &e").append(getInboundCacheSize()).append("\n");
        
        return stats.toString();
    }
    
    
    private int getOutboundCacheSize() {
        return outboundPacketCache.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
    
    
    private int getInboundCacheSize() {
        return inboundPacketCache.values().stream()
                .mapToInt(Map::size)
                .sum();
    }
    
    
    public Set<UUID> getAllPlayerUUIDs() {
        Set<UUID> allPlayers = new HashSet<>();
        allPlayers.addAll(outboundPacketCache.keySet());
        allPlayers.addAll(inboundPacketCache.keySet());
        return allPlayers;
    }
} 