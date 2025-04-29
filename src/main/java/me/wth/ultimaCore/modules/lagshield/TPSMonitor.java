package me.wth.ultimaCore.modules.lagshield;

import java.util.LinkedList;
import java.util.Queue;


public class TPSMonitor {
    private final LagShieldModule module;
    private final Queue<Double> tpsHistory = new LinkedList<>();
    private final int historySize = 20;     private long lastTickTime;
    private double currentTPS = 20.0;
    private double averageTPS = 20.0;
    
    
    public TPSMonitor(LagShieldModule module) {
        this.module = module;
        this.lastTickTime = System.currentTimeMillis();
        
                for (int i = 0; i < historySize; i++) {
            tpsHistory.add(20.0);
        }
    }
    
    
    public void update() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTickTime;
        lastTickTime = now;
        
                if (elapsed > 0) {
            currentTPS = Math.min(20.0, 1000.0 / elapsed);
        } else {
            currentTPS = 20.0;
        }
        
                tpsHistory.add(currentTPS);
        if (tpsHistory.size() > historySize) {
            tpsHistory.remove();
        }
        
                double sum = 0;
        for (double tps : tpsHistory) {
            sum += tps;
        }
        averageTPS = sum / tpsHistory.size();
    }
    
    
    public double getCurrentTPS() {
        return currentTPS;
    }
    
    
    public double getAverageTPS() {
        return averageTPS;
    }
    
    
    public String getFormattedTPS() {
        String color;
        
        if (averageTPS >= 18.0) {
            color = "§a";         } else if (averageTPS >= 15.0) {
            color = "§e";         } else {
            color = "§c";         }
        
        return color + String.format("%.2f", averageTPS);
    }
    
    
    public boolean isLagging() {
        return averageTPS < module.getSettings().getCleanupTpsThreshold();
    }
} 