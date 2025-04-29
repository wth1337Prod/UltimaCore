package me.wth.ultimaCore.modules.redstonegenius;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class RedstoneTracker {

        private class ActivationInfo {
        private int activationCount;
        private long lastActivationTime;
        private final LinkedList<Long> activationTimes;
        private final Material material;
        private boolean throttled;
        private long throttleExpirationTime;
        
        public ActivationInfo(Material material) {
            this.activationCount = 1;
            this.lastActivationTime = System.currentTimeMillis();
            this.activationTimes = new LinkedList<>();
            this.activationTimes.add(this.lastActivationTime);
            this.material = material;
            this.throttled = false;
            this.throttleExpirationTime = 0;
        }
        
        public void incrementCount() {
            activationCount++;
            long now = System.currentTimeMillis();
            lastActivationTime = now;
            activationTimes.add(now);
            
                        while (!activationTimes.isEmpty() && now - activationTimes.peekFirst() > 1000) {
                activationTimes.pollFirst();
            }
        }
        
        public int getFrequency() {
            return activationTimes.size();
        }
        
        public void throttle(long duration) {
            this.throttled = true;
            this.throttleExpirationTime = System.currentTimeMillis() + duration;
        }
        
        public boolean isThrottled() {
            if (!throttled) return false;
            
                        if (System.currentTimeMillis() > throttleExpirationTime) {
                throttled = false;
                return false;
            }
            
            return true;
        }
        
        public void reset() {
            activationCount = 0;
            activationTimes.clear();
        }
    }
    
        private final Map<String, ActivationInfo> activations;
    
        private final Map<String, Integer> chunkActivations;
    
        private final Set<String> detectedClocks;
    
        private final Map<String, List<Long>> clockCandidates;
    
        private final RedstoneSettings settings;
    
    
    public RedstoneTracker(RedstoneSettings settings) {
        this.activations = new ConcurrentHashMap<>();
        this.chunkActivations = new ConcurrentHashMap<>();
        this.detectedClocks = new HashSet<>();
        this.clockCandidates = new HashMap<>();
        this.settings = settings;
        
                startCleanupTask();
    }
    
    
    private void startCleanupTask() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanup();
            }
        }, 60000, 60000);     }
    
    
    private void cleanup() {
        long now = System.currentTimeMillis();
        
                Iterator<Map.Entry<String, ActivationInfo>> iter = activations.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ActivationInfo> entry = iter.next();
            if (now - entry.getValue().lastActivationTime > TimeUnit.MINUTES.toMillis(5)) {
                iter.remove();
            }
        }
        
                chunkActivations.clear();
        
                Iterator<Map.Entry<String, List<Long>>> clockIter = clockCandidates.entrySet().iterator();
        while (clockIter.hasNext()) {
            Map.Entry<String, List<Long>> entry = clockIter.next();
            List<Long> times = entry.getValue();
            
                        times.removeIf(time -> now - time > TimeUnit.MINUTES.toMillis(1));
            
                        if (times.isEmpty()) {
                clockIter.remove();
            }
        }
    }
    
    
    public boolean registerActivation(Location location, Material material) {
        String locKey = locationToString(location);
        String chunkKey = chunkToString(location.getChunk());
        
                ActivationInfo info = activations.get(locKey);
        if (info == null) {
            info = new ActivationInfo(material);
            activations.put(locKey, info);
        } else {
            info.incrementCount();
        }
        
                chunkActivations.put(chunkKey, chunkActivations.getOrDefault(chunkKey, 0) + 1);
        
                if (info.getFrequency() > settings.getMaxActivationsPerBlock()) {
            info.throttle(settings.getThrottleDuration());
            return true;
        }
        
                if (chunkActivations.get(chunkKey) > settings.getMaxActivationsPerChunk()) {
            info.throttle(settings.getThrottleDuration());
            return true;
        }
        
        return false;
    }
    
    
    public boolean isThrottled(Location location) {
        String locKey = locationToString(location);
        ActivationInfo info = activations.get(locKey);
        
        return info != null && info.isThrottled();
    }
    
    
    public boolean checkForClockPattern(Location location) {
        String locKey = locationToString(location);
        
                ActivationInfo info = activations.get(locKey);
        if (info == null) return false;
        
                if (info.getFrequency() > settings.getMaxClockFrequency()) {
                        List<Long> times = clockCandidates.computeIfAbsent(locKey, k -> new ArrayList<>());
            times.add(System.currentTimeMillis());
            
                        if (times.size() >= 3) {
                                detectedClocks.add(locKey);
                return true;
            }
        }
        
        return false;
    }
    
    
    private String locationToString(Location location) {
        return location.getWorld().getName() + ":" + 
               location.getBlockX() + ":" + 
               location.getBlockY() + ":" + 
               location.getBlockZ();
    }
    
    
    private String chunkToString(Chunk chunk) {
        return chunk.getWorld().getName() + ":" + 
               chunk.getX() + ":" + 
               chunk.getZ();
    }
    
    
    public void reset() {
        activations.clear();
        chunkActivations.clear();
        detectedClocks.clear();
        clockCandidates.clear();
    }
    
    
    public int getDetectedClocksCount() {
        return detectedClocks.size();
    }
    
    
    public int getTrackedLocationsCount() {
        return activations.size();
    }
    
    
    public int getThrottledLocationsCount() {
        return (int) activations.values().stream()
                .filter(ActivationInfo::isThrottled)
                .count();
    }
} 