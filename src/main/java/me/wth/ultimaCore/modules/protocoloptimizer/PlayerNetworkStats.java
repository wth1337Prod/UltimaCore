package me.wth.ultimaCore.modules.protocoloptimizer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class PlayerNetworkStats {
    private final UUID playerUUID;
    private long totalBytesSent = 0;
    private long totalBytesReceived = 0;
    private long totalPacketsSent = 0;
    private long totalPacketsReceived = 0;
    private long totalBytesSaved = 0;
    private long totalPacketsOptimized = 0;
    
        private final Map<String, PacketTypeStats> outboundPacketStats = new HashMap<>();
    private final Map<String, PacketTypeStats> inboundPacketStats = new HashMap<>();
    
        private final long[] recentPacketRates = new long[10];     private final long[] recentByteRates = new long[10];     private int currentRateIndex = 0;
    
        private final AtomicInteger currentSecondPackets = new AtomicInteger(0);
    private final AtomicInteger currentSecondBytes = new AtomicInteger(0);
    
        private long lastRateReset = System.currentTimeMillis();
    
    
    public PlayerNetworkStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }
    
    
    public synchronized void registerOutboundPacket(String packetType, int size) {
        totalBytesSent += size;
        totalPacketsSent++;
        
        currentSecondPackets.incrementAndGet();
        currentSecondBytes.addAndGet(size);
        
        outboundPacketStats.computeIfAbsent(packetType, k -> new PacketTypeStats())
                .registerPacket(size);
    }
    
    
    public synchronized void registerInboundPacket(String packetType, int size) {
        totalBytesReceived += size;
        totalPacketsReceived++;
        
        currentSecondPackets.incrementAndGet();
        currentSecondBytes.addAndGet(size);
        
        inboundPacketStats.computeIfAbsent(packetType, k -> new PacketTypeStats())
                .registerPacket(size);
    }
    
    
    public synchronized void registerOptimizedPacket(int originalSize, int optimizedSize) {
        int bytesSaved = originalSize - optimizedSize;
        if (bytesSaved > 0) {
            totalBytesSaved += bytesSaved;
            totalPacketsOptimized++;
        }
    }
    
    
    public void updateRates() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRateReset;
        
        if (elapsed >= 1000) {             recentPacketRates[currentRateIndex] = currentSecondPackets.getAndSet(0);
            recentByteRates[currentRateIndex] = currentSecondBytes.getAndSet(0);
            
            currentRateIndex = (currentRateIndex + 1) % 10;
            lastRateReset = now;
        }
    }
    
    
    public double getAveragePacketRate() {
        long sum = 0;
        for (long rate : recentPacketRates) {
            sum += rate;
        }
        return sum / 10.0;
    }
    
    
    public double getAverageByteRate() {
        long sum = 0;
        for (long rate : recentByteRates) {
            sum += rate;
        }
        return sum / 10.0;
    }
    
    
    public long getTotalBytesSent() {
        return totalBytesSent;
    }
    
    
    public long getTotalBytesReceived() {
        return totalBytesReceived;
    }
    
    
    public long getTotalPacketsSent() {
        return totalPacketsSent;
    }
    
    
    public long getTotalPacketsReceived() {
        return totalPacketsReceived;
    }
    
    
    public long getTotalBytesSaved() {
        return totalBytesSaved;
    }
    
    
    public long getTotalPacketsOptimized() {
        return totalPacketsOptimized;
    }
    
    
    public double getSavedBytesPercentage() {
        if (totalBytesSent + totalBytesReceived == 0) {
            return 0.0;
        }
        return (double) totalBytesSaved / (totalBytesSent + totalBytesReceived) * 100.0;
    }
    
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    
    public Map<String, PacketTypeStats> getOutboundPacketStats() {
        return new HashMap<>(outboundPacketStats);
    }
    
    
    public Map<String, PacketTypeStats> getInboundPacketStats() {
        return new HashMap<>(inboundPacketStats);
    }
    
    
    public static class PacketTypeStats {
        private long packetCount = 0;
        private long totalBytes = 0;
        private int minSize = Integer.MAX_VALUE;
        private int maxSize = 0;
        
        
        public void registerPacket(int size) {
            packetCount++;
            totalBytes += size;
            minSize = Math.min(minSize, size);
            maxSize = Math.max(maxSize, size);
        }
        
        
        public long getPacketCount() {
            return packetCount;
        }
        
        
        public long getTotalBytes() {
            return totalBytes;
        }
        
        
        public int getMinSize() {
            return minSize == Integer.MAX_VALUE ? 0 : minSize;
        }
        
        
        public int getMaxSize() {
            return maxSize;
        }
        
        
        public double getAverageSize() {
            return packetCount > 0 ? (double) totalBytes / packetCount : 0;
        }
    }
} 