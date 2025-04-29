package me.wth.ultimaCore.modules.protocoloptimizer.managers;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class PacketCacheManager {
    private final ProtocolOptimizerModule module;
    
        private final Map<UUID, Map<String, CachedPacket>> outboundCache = new ConcurrentHashMap<>();
    
        private final Map<UUID, Map<String, CachedPacket>> inboundCache = new ConcurrentHashMap<>();
    
        private int cacheHits = 0;
    private int cacheMisses = 0;
    
    
    public PacketCacheManager(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    public Object cacheOutboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (module == null || !module.isCachePackets() || packetData == null) {
            return packetData;
        }
        
        try {
                        Map<String, CachedPacket> playerCache = outboundCache.computeIfAbsent(
                    playerUUID, k -> new ConcurrentHashMap<>());
            
                        String cacheKey = generateCacheKey(packetType, packetData);
            
                        CachedPacket cached = playerCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                                cacheHits++;
                cached.updateLastUsed();
                return cached.getPacketData();
            } else {
                                cacheMisses++;
                playerCache.put(cacheKey, new CachedPacket(packetData));
                
                                if (playerCache.size() > 1000) {
                    cleanupPlayerCache(playerCache);
                }
                
                return packetData;
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при кэшировании исходящего пакета " + packetType + ": " + e.getMessage());
            return packetData;
        }
    }
    
    
    public Object cacheInboundPacket(UUID playerUUID, String packetType, Object packetData) {
        if (module == null || !module.isCachePackets() || packetData == null) {
            return packetData;
        }
        
        try {
                        Map<String, CachedPacket> playerCache = inboundCache.computeIfAbsent(
                    playerUUID, k -> new ConcurrentHashMap<>());
            
                        String cacheKey = generateCacheKey(packetType, packetData);
            
                        CachedPacket cached = playerCache.get(cacheKey);
            if (cached != null && !cached.isExpired()) {
                                cacheHits++;
                cached.updateLastUsed();
                return cached.getPacketData();
            } else {
                                cacheMisses++;
                playerCache.put(cacheKey, new CachedPacket(packetData));
                
                                if (playerCache.size() > 1000) {
                    cleanupPlayerCache(playerCache);
                }
                
                return packetData;
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при кэшировании входящего пакета " + packetType + ": " + e.getMessage());
            return packetData;
        }
    }
    
    
    private String generateCacheKey(String packetType, Object packetData) {
                return packetType + "_" + packetData.hashCode();
    }
    
    
    private void cleanupPlayerCache(Map<String, CachedPacket> cache) {
                cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
                if (cache.size() > 800) {
            Map<String, CachedPacket> sortedCache = new HashMap<>(cache);
            sortedCache.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e1.getValue().getLastUsed(), e2.getValue().getLastUsed()))
                    .limit(200)
                    .forEach(e -> cache.remove(e.getKey()));
        }
    }
    
    
    public void cleanupPlayerCache(UUID playerUUID) {
        outboundCache.remove(playerUUID);
        inboundCache.remove(playerUUID);
    }
    
    
    public void clearCache() {
        outboundCache.clear();
        inboundCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
    
    
    public void cleanupCache() {
                for (Map<String, CachedPacket> playerCache : outboundCache.values()) {
            cleanupPlayerCache(playerCache);
        }
        
        for (Map<String, CachedPacket> playerCache : inboundCache.values()) {
            cleanupPlayerCache(playerCache);
        }
    }
    
    
    public double getCacheEfficiency() {
        int totalRequests = cacheHits + cacheMisses;
        if (totalRequests == 0) {
            return 0.0;
        }
        
        return 100.0 * cacheHits / totalRequests;
    }
    
    
    public int getCacheHits() {
        return cacheHits;
    }
    
    
    public int getCacheMisses() {
        return cacheMisses;
    }
    
    
    private static class CachedPacket {
        private final Object packetData;
        private final long creationTime;
        private long lastUsed;
        private final long expirationTime;
        
        
        public CachedPacket(Object packetData) {
            this.packetData = packetData;
            this.creationTime = System.currentTimeMillis();
            this.lastUsed = this.creationTime;
            this.expirationTime = this.creationTime + 30000;         }
        
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
        
        
        public void updateLastUsed() {
            this.lastUsed = System.currentTimeMillis();
        }
        
        
        public Object getPacketData() {
            return packetData;
        }
        
        
        public long getLastUsed() {
            return lastUsed;
        }
    }
} 