package me.wth.ultimaCore.modules.redstonegenius;

import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class RedstoneGeniusModule extends AbstractModule implements Listener {
        private final Map<Long, Integer> redstoneUpdateCountPerChunk = new ConcurrentHashMap<>();
    
        private final Map<Location, Long> lastRedstoneActivation = new ConcurrentHashMap<>();
    
        private final Map<Location, Integer> redstoneLoopDetection = new ConcurrentHashMap<>();
    
        private final Map<Location, Long> throttledDevices = new ConcurrentHashMap<>();
    
        private final Set<RedstoneClock> detectedClocks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
        private int maxRedstoneUpdatesPerChunk = 100;
    private int clockThrottleDelay = 2;
    private int maxRedstoneChainLength = 100;
    private boolean detectAndOptimizeClocks = true;
    private boolean optimizeRedstoneDust = true;
    private int redstoneCheckInterval = 20;
    
    private BukkitTask cleanupTask;
    private BukkitTask clockDetectionTask;
    
    private boolean enabled = true;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                loadConfiguration();
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
        
                startCleanupTask();
        
                if (detectAndOptimizeClocks) {
            startClockDetectionTask();
        }
        
                try {
            RedstoneGeniusCommand commandHandler = new RedstoneGeniusCommand(this);
            if (plugin.getCommand("redstone") != null) {
                plugin.getCommand("redstone").setExecutor(commandHandler);
                plugin.getCommand("redstone").setTabCompleter(commandHandler);
                LoggerUtil.info("Команда /redstone успешно зарегистрирована.");
            } else {
                LoggerUtil.warning("Команда 'redstone' не найдена в plugin.yml. Команда не будет зарегистрирована.");
            }
        } catch (Exception e) {
            LoggerUtil.warning("Не удалось зарегистрировать команду для модуля RedstoneGenius", e);
        }
        
        LoggerUtil.info("Модуль RedstoneGenius успешно инициализирован");
    }
    
    @Override
    public void onDisable() {
                if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        if (clockDetectionTask != null) {
            clockDetectionTask.cancel();
        }
        
                redstoneUpdateCountPerChunk.clear();
        lastRedstoneActivation.clear();
        redstoneLoopDetection.clear();
        throttledDevices.clear();
        detectedClocks.clear();
        
        super.onDisable();
    }
    
    @Override
    public String getName() {
        return "RedstoneGenius";
    }
    
    @Override
    public String getDescription() {
        return "Модуль оптимизации редстоуновых механизмов и схем";
    }
    
    @Override
    public void onTick() {
            }
    
    
    private void loadConfiguration() {
        plugin.reloadConfig();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modules.redstonegenius");
        
        if (config != null) {
            enabled = config.getBoolean("enabled", enabled);
            detectAndOptimizeClocks = config.getBoolean("detect_and_optimize_clocks", detectAndOptimizeClocks);
            optimizeRedstoneDust = config.getBoolean("optimize_redstone_dust", optimizeRedstoneDust);
            maxRedstoneUpdatesPerChunk = config.getInt("max_redstone_updates_per_chunk", maxRedstoneUpdatesPerChunk);
            maxRedstoneChainLength = config.getInt("max_redstone_chain_length", maxRedstoneChainLength);
            redstoneCheckInterval = config.getInt("redstone_check_interval", redstoneCheckInterval);
        } else {
                        saveConfiguration();
        }
    }
    
    
    private void startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
                        redstoneUpdateCountPerChunk.clear();
            
                        throttledDevices.entrySet().removeIf(entry -> entry.getValue() < currentTime);
            
                        redstoneLoopDetection.clear();
            
        }, redstoneCheckInterval, redstoneCheckInterval);
    }
    
    
    private void startClockDetectionTask() {
        clockDetectionTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            
                        for (Map.Entry<Location, Long> entry : lastRedstoneActivation.entrySet()) {
                Location location = entry.getKey();
                Block block = location.getBlock();
                
                                if (isRedstoneComponent(block.getType())) {
                                        if (!isPartOfDetectedClock(location)) {
                        RedstoneClock clock = new RedstoneClock(location);
                        
                                                addConnectedComponents(block, clock, new HashSet<>());
                        
                                                if (clock.getComponents().size() >= 3) {
                            detectedClocks.add(clock);
                            LoggerUtil.debug("Обнаружены редстоуновые часы размером " + 
                                    clock.getComponents().size() + " блоков в координатах " + 
                                    location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
                        }
                    }
                }
            }
            
                        lastRedstoneActivation.entrySet().removeIf(entry -> 
                    currentTime - entry.getValue() > 10000);             
                        detectedClocks.removeIf(clock -> !clock.isActive(currentTime, 10000));
            
        }, redstoneCheckInterval * 5, redstoneCheckInterval * 5);     }
    
    
    private boolean isRedstoneComponent(Material type) {
        return type == Material.REDSTONE_WIRE || 
               type == Material.REPEATER || 
               type == Material.COMPARATOR || 
               type == Material.REDSTONE_TORCH || 
               type == Material.REDSTONE_WALL_TORCH || 
               type == Material.REDSTONE_LAMP || 
               type == Material.OBSERVER || 
               type == Material.LEVER || 
               type == Material.PISTON || 
               type == Material.STICKY_PISTON || 
               type == Material.DISPENSER || 
               type == Material.DROPPER;
    }
    
    
    private boolean isPartOfDetectedClock(Location location) {
        for (RedstoneClock clock : detectedClocks) {
            if (clock.containsComponent(location)) {
                return true;
            }
        }
        return false;
    }
    
    
    private void addConnectedComponents(Block block, RedstoneClock clock, Set<Location> visited) {
        if (visited.size() >= 20) {             return;
        }
        
        Location location = block.getLocation();
        if (visited.contains(location)) {
            return;
        }
        
        visited.add(location);
        
                clock.addComponent(location);
        
                for (BlockFace face : BlockFace.values()) {
            if (face == BlockFace.SELF) continue;
            
            Block neighbor = block.getRelative(face);
            if (isRedstoneComponent(neighbor.getType())) {
                addConnectedComponents(neighbor, clock, visited);
            }
        }
    }
    
    
    private long getChunkKey(Chunk chunk) {
        return ((long) chunk.getX() << 32) | (chunk.getZ() & 0xFFFFFFFFL);
    }
    
    
    private int incrementRedstoneUpdateCount(Chunk chunk) {
        long key = getChunkKey(chunk);
        int count = redstoneUpdateCountPerChunk.getOrDefault(key, 0) + 1;
        redstoneUpdateCountPerChunk.put(key, count);
        return count;
    }
    
    
    private boolean isRedstoneUpdateLimitExceeded(Chunk chunk) {
        int count = redstoneUpdateCountPerChunk.getOrDefault(getChunkKey(chunk), 0);
        return count > maxRedstoneUpdatesPerChunk;
    }
    
    
    private boolean isThrottled(Location location) {
        Long throttleTime = throttledDevices.get(location);
        return throttleTime != null && throttleTime > System.currentTimeMillis();
    }
    
    
    private void applyThrottle(Location location, int durationTicks) {
        long expiryTime = System.currentTimeMillis() + (durationTicks * 50);         throttledDevices.put(location, expiryTime);
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        
                if (isThrottled(block.getLocation())) {
            event.setNewCurrent(event.getOldCurrent());             return;
        }
        
                if (isRedstoneUpdateLimitExceeded(block.getChunk())) {
            event.setNewCurrent(event.getOldCurrent());             return;
        }
        
                incrementRedstoneUpdateCount(block.getChunk());
        
                if (detectAndOptimizeClocks) {
            lastRedstoneActivation.put(block.getLocation(), System.currentTimeMillis());
        }
        
                if (isPartOfDetectedClock(block.getLocation())) {
                        applyThrottle(block.getLocation(), clockThrottleDelay);
        }
        
                if (optimizeRedstoneDust && block.getType() == Material.REDSTONE_WIRE) {
                        if (event.getNewCurrent() < 2) {
                                event.setNewCurrent(0);
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        
                if (!isRedstoneComponent(block.getType())) {
            return;
        }
        
                if (isThrottled(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        
                if (isRedstoneUpdateLimitExceeded(block.getChunk())) {
            event.setCancelled(true);
            return;
        }
        
                Location location = block.getLocation();
        int callCount = redstoneLoopDetection.getOrDefault(location, 0) + 1;
        
        if (callCount > maxRedstoneChainLength) {
                        applyThrottle(location, clockThrottleDelay * 2);
            event.setCancelled(true);
            LoggerUtil.debug("Обнаружена редстоуновая петля в координатах " + 
                    location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            return;
        }
        
        redstoneLoopDetection.put(location, callCount);
        incrementRedstoneUpdateCount(block.getChunk());
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        
                if (isThrottled(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        
                if (isRedstoneUpdateLimitExceeded(block.getChunk())) {
            event.setCancelled(true);
            return;
        }
        
                incrementRedstoneUpdateCount(block.getChunk());
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getBlock();
        
                if (isThrottled(block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        
                if (isRedstoneUpdateLimitExceeded(block.getChunk())) {
            event.setCancelled(true);
            return;
        }
        
                incrementRedstoneUpdateCount(block.getChunk());
    }
    
    
    public void reloadConfiguration() {
        loadConfiguration();
        
                if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        if (clockDetectionTask != null) {
            clockDetectionTask.cancel();
        }
        
        startCleanupTask();
        
        if (detectAndOptimizeClocks) {
            startClockDetectionTask();
        }
        
        LoggerUtil.info("Конфигурация RedstoneGenius перезагружена");
    }
    
    
    public boolean isEnabled() {
        return enabled;
    }
    
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        
                saveConfiguration();
        
                if (enabled) {
            if (cleanupTask == null) {
                startCleanupTask();
            }
            
            if (detectAndOptimizeClocks && clockDetectionTask == null) {
                startClockDetectionTask();
            }
        } else {
            if (cleanupTask != null) {
                cleanupTask.cancel();
                cleanupTask = null;
            }
            
            if (clockDetectionTask != null) {
                clockDetectionTask.cancel();
                clockDetectionTask = null;
            }
        }
    }
    
    
    public boolean isClockDetectionEnabled() {
        return detectAndOptimizeClocks;
    }
    
    
    public void setClockDetectionEnabled(boolean enabled) {
        this.detectAndOptimizeClocks = enabled;
        
                saveConfiguration();
        
                if (enabled && this.enabled) {
            if (clockDetectionTask == null) {
                startClockDetectionTask();
            }
        } else {
            if (clockDetectionTask != null) {
                clockDetectionTask.cancel();
                clockDetectionTask = null;
            }
        }
    }
    
    
    public boolean isRedstoneDustOptimizationEnabled() {
        return optimizeRedstoneDust;
    }
    
    
    public void setRedstoneDustOptimizationEnabled(boolean enabled) {
        this.optimizeRedstoneDust = enabled;
        
                saveConfiguration();
    }
    
    
    public int getMaxRedstoneUpdatesPerChunk() {
        return maxRedstoneUpdatesPerChunk;
    }
    
    
    public void setMaxRedstoneUpdatesPerChunk(int limit) {
        this.maxRedstoneUpdatesPerChunk = limit;
        
                saveConfiguration();
    }
    
    
    public int getMaxRedstoneChainLength() {
        return maxRedstoneChainLength;
    }
    
    
    public void setMaxRedstoneChainLength(int limit) {
        this.maxRedstoneChainLength = limit;
        
                saveConfiguration();
    }
    
    
    public int getRedstoneCheckInterval() {
        return redstoneCheckInterval;
    }
    
    
    public void setRedstoneCheckInterval(int interval) {
        this.redstoneCheckInterval = interval;
        
                saveConfiguration();
        
                if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        if (clockDetectionTask != null) {
            clockDetectionTask.cancel();
        }
        
        if (this.enabled) {
            startCleanupTask();
            
            if (detectAndOptimizeClocks) {
                startClockDetectionTask();
            }
        }
    }
    
    
    public Set<RedstoneClock> getDetectedClocks() {
        return new HashSet<>(detectedClocks);
    }
    
    
    public int getEventCount(String eventType) {
        return redstoneLoopDetection.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    
    private void saveConfiguration() {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modules.redstonegenius");
        
        if (config == null) {
            config = plugin.getConfig().createSection("modules.redstonegenius");
        }
        
        config.set("enabled", enabled);
        config.set("detect_and_optimize_clocks", detectAndOptimizeClocks);
        config.set("optimize_redstone_dust", optimizeRedstoneDust);
        config.set("max_redstone_updates_per_chunk", maxRedstoneUpdatesPerChunk);
        config.set("max_redstone_chain_length", maxRedstoneChainLength);
        config.set("redstone_check_interval", redstoneCheckInterval);
        
        plugin.saveConfig();
    }
} 