package me.wth.ultimaCore.modules.networkoptimizer;

import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Queue;


public class PlayerNetworkStats {
    private static final int MAX_HISTORY_SIZE = 5;
    
    private final Queue<Integer> pingHistory = new LinkedList<>();
    private int averagePing = 0;
    private long lastUpdateTime = 0;
    private boolean optimizationsApplied = false;
    
    
    public void update(Player player) {
        int currentPing = player.getPing();
        
                pingHistory.add(currentPing);
        
                if (pingHistory.size() > MAX_HISTORY_SIZE) {
            pingHistory.poll();
        }
        
                if (!pingHistory.isEmpty()) {
            int total = 0;
            for (int ping : pingHistory) {
                total += ping;
            }
            averagePing = total / pingHistory.size();
        }
        
                lastUpdateTime = System.currentTimeMillis();
    }
    
    
    public int getAveragePing() {
        return averagePing;
    }
    
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    
    public boolean isRecentlyUpdated(long timeThreshold) {
        return System.currentTimeMillis() - lastUpdateTime < timeThreshold;
    }
    
    
    public void setOptimizationsApplied(boolean optimizationsApplied) {
        this.optimizationsApplied = optimizationsApplied;
    }
    
    
    public boolean isOptimizationsApplied() {
        return optimizationsApplied;
    }
    
    
    public boolean isPingStable() {
        if (pingHistory.size() < 3) {
            return false;
        }
        
                double mean = getAveragePing();
        double sumOfSquares = 0;
        
        for (int ping : pingHistory) {
            double diff = ping - mean;
            sumOfSquares += diff * diff;
        }
        
        double standardDeviation = Math.sqrt(sumOfSquares / pingHistory.size());
        
                return standardDeviation < (mean * 0.15);
    }
    
    
    public void reset() {
        pingHistory.clear();
        averagePing = 0;
        optimizationsApplied = false;
    }
} 