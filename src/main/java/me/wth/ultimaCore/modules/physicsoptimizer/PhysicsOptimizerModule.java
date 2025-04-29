package me.wth.ultimaCore.modules.physicsoptimizer;

import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.config.Config;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class PhysicsOptimizerModule extends AbstractModule implements Listener {
    private PhysicsOptimizerSettings settings;
    private final Random random = new Random();
    
        private BukkitTask fluidLimiterTask;
    private BukkitTask fallingBlocksLimiterTask;
    private BukkitTask physicsRateTask;
    
        private final Map<String, PhysicsEventCounter> eventCounters = new ConcurrentHashMap<>();
    
        private final Map<String, Integer> chunkCooldowns = new ConcurrentHashMap<>();
    
        private final Map<World, Integer> fallingBlockCounters = new ConcurrentHashMap<>();
    private final Set<Location> queuedPhysicsUpdates = new HashSet<>();
    
        private final Map<World, AtomicInteger> fluidUpdateCounters = new ConcurrentHashMap<>();
    
        private final Set<Material> physicalMaterials = new HashSet<>();
    private final Map<EntityType, Integer> entityExplosionPower = new EnumMap<>(EntityType.class);
    
    
    public PhysicsOptimizerModule() {
        super();
        initPhysicalMaterials();
        initExplosionPowers();
    }
    
    
    private void initPhysicalMaterials() {
                physicalMaterials.add(Material.SAND);
        physicalMaterials.add(Material.RED_SAND);
        physicalMaterials.add(Material.GRAVEL);
        physicalMaterials.add(Material.ANVIL);
        physicalMaterials.add(Material.DRAGON_EGG);
        try {
            physicalMaterials.add(Material.valueOf("CONCRETE_POWDER"));
        } catch (IllegalArgumentException ex) {
                        try {
                                physicalMaterials.add(Material.WHITE_CONCRETE_POWDER);
                physicalMaterials.add(Material.ORANGE_CONCRETE_POWDER);
                physicalMaterials.add(Material.MAGENTA_CONCRETE_POWDER);
                physicalMaterials.add(Material.LIGHT_BLUE_CONCRETE_POWDER);
                physicalMaterials.add(Material.YELLOW_CONCRETE_POWDER);
                physicalMaterials.add(Material.LIME_CONCRETE_POWDER);
                physicalMaterials.add(Material.PINK_CONCRETE_POWDER);
                physicalMaterials.add(Material.GRAY_CONCRETE_POWDER);
                physicalMaterials.add(Material.LIGHT_GRAY_CONCRETE_POWDER);
                physicalMaterials.add(Material.CYAN_CONCRETE_POWDER);
                physicalMaterials.add(Material.PURPLE_CONCRETE_POWDER);
                physicalMaterials.add(Material.BLUE_CONCRETE_POWDER);
                physicalMaterials.add(Material.BROWN_CONCRETE_POWDER);
                physicalMaterials.add(Material.GREEN_CONCRETE_POWDER);
                physicalMaterials.add(Material.RED_CONCRETE_POWDER);
                physicalMaterials.add(Material.BLACK_CONCRETE_POWDER);
            } catch (IllegalArgumentException e) {
                LoggerUtil.info("Материал CONCRETE_POWDER не найден, пропускаю");
            }
        }
        
                physicalMaterials.add(Material.WATER);
        physicalMaterials.add(Material.LAVA);
    }
    
    
    private void initExplosionPowers() {
        entityExplosionPower.put(EntityType.CREEPER, 3);
        try {
                        entityExplosionPower.put(EntityType.valueOf("PRIMED_TNT"), 4);
        } catch (IllegalArgumentException e) {
                        try {
                entityExplosionPower.put(EntityType.valueOf("TNT"), 4);
            } catch (IllegalArgumentException ex) {
                LoggerUtil.info("Тип сущности TNT/PRIMED_TNT не найден, пропускаю");
            }
        }
        
        try {
                        entityExplosionPower.put(EntityType.valueOf("MINECART_TNT"), 4);
        } catch (IllegalArgumentException e) {
                        try {
                entityExplosionPower.put(EntityType.valueOf("MINECART"), 4);
            } catch (IllegalArgumentException ex) {
                LoggerUtil.info("Тип сущности MINECART_TNT не найден, пропускаю");
            }
        }
        
        entityExplosionPower.put(EntityType.FIREBALL, 1);
        entityExplosionPower.put(EntityType.SMALL_FIREBALL, 1);
        entityExplosionPower.put(EntityType.WITHER_SKULL, 1);
        entityExplosionPower.put(EntityType.WITHER, 8);
        
        try {
                        entityExplosionPower.put(EntityType.valueOf("ENDER_CRYSTAL"), 6);
        } catch (IllegalArgumentException e) {
                        try {
                entityExplosionPower.put(EntityType.valueOf("ENDER_CRYSTAL"), 6);
            } catch (IllegalArgumentException ex) {
                LoggerUtil.info("Тип сущности ENDER_CRYSTAL не найден, пропускаю");
            }
        }
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                this.settings = new PhysicsOptimizerSettings();
        loadConfig();
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
        
                startSchedulers();
        
                try {
            PhysicsOptimizerCommand commandHandler = new PhysicsOptimizerCommand(this);
            if (plugin.getCommand("physics") != null) {
                plugin.getCommand("physics").setExecutor(commandHandler);
                plugin.getCommand("physics").setTabCompleter(commandHandler);
            } else {
                LoggerUtil.warning("Команда 'physics' не найдена в plugin.yml. Команда не будет зарегистрирована.");
            }
        } catch (Exception e) {
            LoggerUtil.warning("Не удалось зарегистрировать команду для модуля PhysicsOptimizer", e);
        }
        
        LoggerUtil.info("Модуль PhysicsOptimizer успешно включен!");
    }
    
    @Override
    public void onDisable() {
                stopSchedulers();
        
                eventCounters.clear();
        chunkCooldowns.clear();
        fallingBlockCounters.clear();
        queuedPhysicsUpdates.clear();
        fluidUpdateCounters.clear();
        
        super.onDisable();
        LoggerUtil.info("Модуль PhysicsOptimizer выключен.");
    }
    
    
    public void loadConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("physicsoptimizer");
        
        if (section == null) {
            section = config.createSection("physicsoptimizer");
        }
        
        settings.loadFromConfig(section);
        config.save();
    }
    
    
    public void saveConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("physicsoptimizer");
        
        if (section == null) {
            section = config.createSection("physicsoptimizer");
        }
        
        settings.saveToConfig(section);
        config.save();
    }
    
    
    private void startSchedulers() {
                fluidLimiterTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            fluidUpdateCounters.forEach((world, counter) -> counter.set(0));
            chunkCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
            chunkCooldowns.replaceAll((k, v) -> v - 1);
        }, 1L, 20L);
        
                fallingBlocksLimiterTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            fallingBlockCounters.clear();
            processQueuedPhysicsUpdates();
        }, 1L, 20L);
        
                physicsRateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processMergedPhysics, 1L, 
                settings.getPhysicsProcessingInterval());
    }
    
    
    private void stopSchedulers() {
        if (fluidLimiterTask != null) {
            fluidLimiterTask.cancel();
            fluidLimiterTask = null;
        }
        
        if (fallingBlocksLimiterTask != null) {
            fallingBlocksLimiterTask.cancel();
            fallingBlocksLimiterTask = null;
        }
        
        if (physicsRateTask != null) {
            physicsRateTask.cancel();
            physicsRateTask = null;
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        
                if (!settings.isEnabled()) {
            return;
        }
        
                incrementEventCounter("block_physics");
        
                if (!physicalMaterials.contains(block.getType())) {
                        if (!settings.isLimitAllPhysics()) {
                return;
            }
        }
        
        World world = block.getWorld();
        String chunkKey = getChunkKey(block.getLocation());
        
                if (chunkCooldowns.containsKey(chunkKey)) {
                        if (random.nextDouble() < settings.getChunkCooldownCancelChance()) {
                event.setCancelled(true);
                return;
            }
        }
        
                if (settings.isEnablePhysicsRateLimit()) {
                        if (settings.getPhysicsProcessingInterval() > 1) {
                queuedPhysicsUpdates.add(block.getLocation());
                event.setCancelled(true);
                return;
            }
        }
        
                if (getTotalEventCount() > settings.getPhysicsEventThreshold()) {
                        chunkCooldowns.put(chunkKey, settings.getChunkCooldownTicks());
            
                        if (random.nextDouble() < settings.getEventThresholdCancelChance()) {
                event.setCancelled(true);
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        Block block = event.getBlock();
        
                if (!settings.isLimitFluidFlow()) {
            return;
        }
        
                Material type = block.getType();
        if (type != Material.WATER && type != Material.LAVA) {
                        try {
                Material stationaryWater = Material.valueOf("STATIONARY_WATER");
                Material stationaryLava = Material.valueOf("STATIONARY_LAVA");
                if (type != stationaryWater && type != stationaryLava) {
                    return;
                }
            } catch (IllegalArgumentException e) {
                                return;
            }
        }
        
                incrementEventCounter("fluid_flow");
        
        World world = block.getWorld();
        
                AtomicInteger counter = fluidUpdateCounters.computeIfAbsent(world, k -> new AtomicInteger());
        
                if (counter.incrementAndGet() > settings.getMaxFluidUpdatesPerSecond()) {
                        if (random.nextDouble() < settings.getFluidLimitationChance()) {
                event.setCancelled(true);
                return;
            }
        }
        
                if (type == Material.LAVA) {
                        if (random.nextDouble() < settings.getLavaFlowReduceChance()) {
                event.setCancelled(true);
                return;
            }
        }
        
                try {
            Material stationaryLava = Material.valueOf("STATIONARY_LAVA");
            if (type == stationaryLava) {
                                if (random.nextDouble() < settings.getLavaFlowReduceChance()) {
                    event.setCancelled(true);
                }
            }
        } catch (IllegalArgumentException e) {
                    }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        
                if (entity instanceof FallingBlock) {
            handleFallingBlock(event);
        }
    }
    
    
    private void handleFallingBlock(EntityChangeBlockEvent event) {
                if (!settings.isLimitFallingBlocks()) {
            return;
        }
        
        FallingBlock fallingBlock = (FallingBlock) event.getEntity();
        World world = fallingBlock.getWorld();
        
                incrementEventCounter("falling_block");
        
                int count = fallingBlockCounters.getOrDefault(world, 0) + 1;
        fallingBlockCounters.put(world, count);
        
                if (count > settings.getMaxFallingBlocksPerWorld()) {
                        if (random.nextDouble() < settings.getFallingBlockLimitationChance()) {
                event.setCancelled(true);
                fallingBlock.remove();             }
        }
        
                if (settings.isMergeFallingBlocks() && count > settings.getFallingBlocksMergeThreshold()) {
                        if (random.nextDouble() < settings.getFallingBlocksMergeChance()) {
                                List<Entity> nearbyFallingBlocks = fallingBlock.getNearbyEntities(3.0, 3.0, 3.0).stream()
                        .filter(e -> e instanceof FallingBlock && e != fallingBlock)
                        .toList();
                
                                if (!nearbyFallingBlocks.isEmpty()) {
                    int mergeCount = Math.min(nearbyFallingBlocks.size(), 5);
                    for (int i = 0; i < mergeCount; i++) {
                        nearbyFallingBlocks.get(i).remove();
                    }
                }
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
                if (!settings.isOptimizeExplosions()) {
            return;
        }
        
                incrementEventCounter("explosion");
        
                List<Block> blockList = event.blockList();
        int originalBlockCount = blockList.size();
        
                if (originalBlockCount == 0) {
            return;
        }
        
                int maxBlocks = settings.getMaxBlocksPerExplosion();
        if (originalBlockCount > maxBlocks && settings.isLimitExplosionBlocks()) {
                        List<Block> newBlockList = new ArrayList<>();
            for (int i = 0; i < Math.min(originalBlockCount, maxBlocks); i++) {
                int index = random.nextInt(blockList.size());
                newBlockList.add(blockList.get(index));
                blockList.remove(index);
            }
                        event.blockList().clear();
            event.blockList().addAll(newBlockList);
        }
        
                if (settings.isReduceExplosionFire() && event.getEntity() != null) {
            boolean isTnt = false;
            
            try {
                EntityType primedTnt = EntityType.valueOf("PRIMED_TNT");
                isTnt = event.getEntity().getType() == primedTnt;
            } catch (IllegalArgumentException e) {
                                try {
                    EntityType tnt = EntityType.valueOf("TNT");
                    isTnt = event.getEntity().getType() == tnt;
                } catch (IllegalArgumentException ex) {
                                    }
            }
            
            if (isTnt || event.getEntity().getType() == EntityType.CREEPER) {
                event.setYield((float)(event.getYield() * (1.0f - settings.getFireSpreadReduction())));
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
                if (!settings.isOptimizeExplosions()) {
            return;
        }
        
                incrementEventCounter("explosion_prime");
        
        Entity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        
                if (settings.isReduceExplosionRadius() && entityExplosionPower.containsKey(entity.getType())) {
            float reductionFactor = (float) settings.getExplosionRadiusReduction();
            float newRadius = event.getRadius() * (1.0f - reductionFactor);
            event.setRadius(Math.max(newRadius, 1.0f));         }
        
                if (settings.isReduceExplosionFire()) {
            event.setFire(false);         }
    }
    
    
    private void processQueuedPhysicsUpdates() {
                int processLimit = settings.getMaxQueuedPhysicsUpdates();
        List<Location> toProcess = new ArrayList<>(queuedPhysicsUpdates);
        
        int processed = 0;
        for (Location loc : toProcess) {
            if (processed >= processLimit) {
                break;
            }
            
                        if (!queuedPhysicsUpdates.contains(loc)) {
                continue;
            }
            
                        queuedPhysicsUpdates.remove(loc);
            
                        Block block = loc.getBlock();
            if (block != null && physicalMaterials.contains(block.getType())) {
                block.getState().update(true, true);
            }
            
            processed++;
        }
    }
    
    
    private void processMergedPhysics() {
                        Set<String> processedChunks = new HashSet<>();
        List<Location> toProcess = new ArrayList<>(queuedPhysicsUpdates);
        
        Map<String, List<Location>> chunkBlocks = new HashMap<>();
        
                for (Location loc : toProcess) {
            String chunkKey = getChunkKey(loc);
            chunkBlocks.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(loc);
        }
        
                for (Map.Entry<String, List<Location>> entry : chunkBlocks.entrySet()) {
            String chunkKey = entry.getKey();
            List<Location> locations = entry.getValue();
            
                        if (chunkCooldowns.containsKey(chunkKey)) {
                continue;
            }
            
                        int chunkLimit = settings.getMaxPhysicsPerChunk();
            int processed = 0;
            
            for (Location loc : locations) {
                if (processed >= chunkLimit) {
                    break;
                }
                
                                queuedPhysicsUpdates.remove(loc);
                
                                Block block = loc.getBlock();
                if (block != null) {
                    block.getState().update(true, true);
                }
                
                processed++;
            }
            
                        if (locations.size() > chunkLimit) {
                chunkCooldowns.put(chunkKey, settings.getChunkCooldownTicks());
            }
        }
    }
    
    
    private String getChunkKey(Location location) {
        return location.getWorld().getName() + ":" + (location.getBlockX() >> 4) + ":" + (location.getBlockZ() >> 4);
    }
    
    
    private void incrementEventCounter(String eventType) {
        PhysicsEventCounter counter = eventCounters.computeIfAbsent(eventType, 
                k -> new PhysicsEventCounter(eventType));
        counter.increment();
    }
    
    
    private int getTotalEventCount() {
        int total = 0;
        for (PhysicsEventCounter counter : eventCounters.values()) {
            total += counter.getCount();
        }
        return total;
    }
    
    
    public void resetEventCounters() {
        eventCounters.clear();
    }
    
    
    public Map<String, PhysicsEventCounter> getEventCounters() {
        return new HashMap<>(eventCounters);
    }
    
    
    public PhysicsOptimizerSettings getSettings() {
        return settings;
    }
    
    
    public void setEnabled(boolean enabled) {
                if (settings == null) {
            settings = new PhysicsOptimizerSettings();
            loadConfig();
        }
        
        if (settings.isEnabled() != enabled) {
            settings.setEnabled(enabled);
            saveConfig();
            
            if (enabled) {
                LoggerUtil.info("Модуль PhysicsOptimizer включен.");
            } else {
                LoggerUtil.info("Модуль PhysicsOptimizer выключен.");
            }
        }
    }
    
    @Override
    public void onTick() {
            }
    
    @Override
    public String getName() {
        return "PhysicsOptimizer";
    }
    
    @Override
    public String getDescription() {
        return "Модуль для оптимизации физических процессов на сервере";
    }
    
    
    public static class PhysicsEventCounter {
        private final String eventType;
        private int count;
        private long lastUpdate;
        
        public PhysicsEventCounter(String eventType) {
            this.eventType = eventType;
            this.count = 0;
            this.lastUpdate = System.currentTimeMillis();
        }
        
        public void increment() {
            count++;
            lastUpdate = System.currentTimeMillis();
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public int getCount() {
            return count;
        }
        
        public long getLastUpdate() {
            return lastUpdate;
        }
        
        public void reset() {
            count = 0;
        }
    }
} 