package me.wth.ultimaCore.modules.lagshield;


public class MemoryMonitor {
    private final LagShieldModule module;
    private long totalMemory;
    private long freeMemory;
    private long maxMemory;
    private long usedMemory;
    private double usedMemoryPercentage;
    
    
    public MemoryMonitor(LagShieldModule module) {
        this.module = module;
        update();
    }
    
    
    public void update() {
        Runtime runtime = Runtime.getRuntime();
        
        totalMemory = runtime.totalMemory();
        freeMemory = runtime.freeMemory();
        maxMemory = runtime.maxMemory();
        usedMemory = totalMemory - freeMemory;
        usedMemoryPercentage = (double) usedMemory / maxMemory * 100;
    }
    
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    
    public long getFreeMemory() {
        return freeMemory;
    }
    
    
    public long getMaxMemory() {
        return maxMemory;
    }
    
    
    public long getUsedMemory() {
        return usedMemory;
    }
    
    
    public double getUsedMemoryPercentage() {
        return usedMemoryPercentage;
    }
    
    
    public String getFormattedMemoryUsage() {
        String color;
        
        if (usedMemoryPercentage < 70) {
            color = "§a";         } else if (usedMemoryPercentage < 85) {
            color = "§e";         } else {
            color = "§c";         }
        
        long usedMB = usedMemory / (1024 * 1024);
        long maxMB = maxMemory / (1024 * 1024);
        
        return color + String.format("%d МБ / %d МБ (%.1f%%)", usedMB, maxMB, usedMemoryPercentage);
    }
    
    
    public boolean isMemoryHigh() {
        return usedMemoryPercentage > module.getSettings().getMemoryThreshold();
    }
    
    
    public int getRemainingMemoryMB() {
        return (int) ((maxMemory - usedMemory) / (1024 * 1024));
    }
} 