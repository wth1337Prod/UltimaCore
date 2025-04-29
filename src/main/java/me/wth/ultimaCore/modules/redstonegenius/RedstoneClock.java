package me.wth.ultimaCore.modules.redstonegenius;

import org.bukkit.Location;

import java.util.*;


public class RedstoneClock {
    private final Set<ClockComponent> components = new HashSet<>();
    private final long creationTime;
    private long lastActivationTime;
    private int activationCount = 0;
    private final UUID id;
    private boolean active = false;
    private final Map<Long, Integer> activationsPerSecond = new HashMap<>();

    
    public RedstoneClock(Location initialLocation) {
        this.id = UUID.randomUUID();
        this.creationTime = System.currentTimeMillis();
        this.lastActivationTime = creationTime;
        addComponent(initialLocation);
    }

    
    public void addComponent(Location location) {
        components.add(new ClockComponent(location, System.currentTimeMillis()));
    }

    
    public boolean registerActivation(Location location, long currentTime) {
        boolean wasPartOfClock = false;
        
                for (ClockComponent component : components) {
            if (component.isSameLocation(location)) {
                component.registerActivation(currentTime);
                wasPartOfClock = true;
                break;
            }
        }
        
                if (wasPartOfClock) {
            activationCount++;
            lastActivationTime = currentTime;
        }
        
        return wasPartOfClock;
    }

    
    public boolean containsComponent(Location location) {
        for (ClockComponent component : components) {
            if (component.isSameLocation(location)) {
                return true;
            }
        }
        return false;
    }

    
    public boolean isAdjacentToAnyComponent(Location location) {
        for (ClockComponent component : components) {
            if (isAdjacent(component.getLocation(), location)) {
                return true;
            }
        }
        return false;
    }

    
    private boolean isAdjacent(Location loc1, Location loc2) {
                if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        
                int dx = Math.abs(loc1.getBlockX() - loc2.getBlockX());
        int dy = Math.abs(loc1.getBlockY() - loc2.getBlockY());
        int dz = Math.abs(loc1.getBlockZ() - loc2.getBlockZ());
        
                        return dx + dy + dz == 1;
    }

    
    public boolean isActive(long currentTime, long timeThreshold) {
        return currentTime - lastActivationTime < timeThreshold;
    }

    
    public double getFrequency(long currentTime) {
        long timeElapsed = currentTime - creationTime;
        
                if (timeElapsed == 0) {
            return 0;
        }
        
                return (double) activationCount / (timeElapsed / 1000.0);
    }

    
    public Set<ClockComponent> getComponents() {
        return Collections.unmodifiableSet(components);
    }

    
    public int getComponentCount() {
        return components.size();
    }

    
    public int getActivationCount() {
        return activationCount;
    }

    
    public long getLastActivationTime() {
        return lastActivationTime;
    }

    
    public long getCreationTime() {
        return creationTime;
    }

    
    public UUID getId() {
        return id;
    }

    
    public boolean isActive() {
        long currentTime = System.currentTimeMillis();
                if (currentTime - lastActivationTime > 5000) {
            active = false;
        }
        return active;
    }

    
    public static class ClockComponent {
        private final Location location;
        private long lastActivationTime;
        private int activationCount = 0;

        
        public ClockComponent(Location location, long creationTime) {
                        this.location = location.clone();
            this.lastActivationTime = creationTime;
        }

        
        public void registerActivation(long currentTime) {
            activationCount++;
            lastActivationTime = currentTime;
        }

        
        public boolean isSameLocation(Location otherLocation) {
            return location.getWorld().equals(otherLocation.getWorld()) &&
                   location.getBlockX() == otherLocation.getBlockX() &&
                   location.getBlockY() == otherLocation.getBlockY() &&
                   location.getBlockZ() == otherLocation.getBlockZ();
        }

        
        public Location getLocation() {
            return location.clone();
        }

        
        public long getLastActivationTime() {
            return lastActivationTime;
        }

        
        public int getActivationCount() {
            return activationCount;
        }
    }
} 