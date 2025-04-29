package me.wth.ultimaCore.modules.lagshield;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class LagShieldListener implements Listener {
    private final LagShieldModule module;
    private final Map<String, Integer> tntCounter = new HashMap<>();
    private final Map<UUID, Long> playerLastActivity = new HashMap<>();
    private final Set<UUID> warnedPlayers = new HashSet<>();
    private int fallingBlockCount = 0;
    
    
    public LagShieldListener(LagShieldModule module) {
        this.module = module;
    }
    
    
    public void unregister() {
        HandlerList.unregisterAll(this);
    }
    
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerLastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
    
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerLastActivity.remove(event.getPlayer().getUniqueId());
        warnedPlayers.remove(event.getPlayer().getUniqueId());
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        LagShieldSettings settings = module.getSettings();
        
                if (exceedsChunkEntityLimit(event.getLocation().getChunk().getEntities().length + 1)) {
            if (!settings.isEntityTypeProtected(event.getEntityType()) && 
                event.getEntityType() != EntityType.PLAYER) {
                event.setCancelled(true);
                return;
            }
        }
        
                if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            fallingBlockCount++;
            
                        if (fallingBlockCount > settings.getMaxFallingBlocks()) {
                event.setCancelled(true);
                
                                Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> fallingBlockCount--, 1L);
                return;
            }
            
                        Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> fallingBlockCount--, 100L);
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        LagShieldSettings settings = module.getSettings();
        
                if (exceedsChunkEntityLimit(event.getLocation().getChunk().getEntities().length + 1)) {
            if (!settings.isEntityTypeProtected(event.getEntityType())) {
                event.setCancelled(true);
                return;
            }
        }
        
                if (exceedsEntityTypeLimit(event.getEntityType())) {
            if (!settings.isEntityTypeProtected(event.getEntityType())) {
                event.setCancelled(true);
                return;
            }
        }
        
                if (module.getTpsMonitor().getAverageTPS() < settings.getCleanupTpsThreshold()) {
            if (!settings.isEntityTypeProtected(event.getEntityType()) && 
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM && 
                event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
                event.setCancelled(true);
            }
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        LagShieldSettings settings = module.getSettings();
        
        if (event.getEntity() instanceof TNTPrimed) {
            String worldName = event.getEntity().getWorld().getName();
            int count = tntCounter.getOrDefault(worldName, 0) + 1;
            tntCounter.put(worldName, count);
            
                        if (count > settings.getMaxTntDetonations()) {
                event.setCancelled(true);
                
                                event.getEntity().remove();
                
                return;
            }
            
                        Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> {
                int currentCount = tntCounter.getOrDefault(worldName, 1);
                if (currentCount <= 1) {
                    tntCounter.remove(worldName);
                } else {
                    tntCounter.put(worldName, currentCount - 1);
                }
            }, 20L);
        }
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        LagShieldSettings settings = module.getSettings();
        
                playerLastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        
                if (module.getTpsMonitor().getAverageTPS() < settings.getCleanupTpsThreshold()) {
            Material material = event.getBlock().getType();
            
                        if (isLaggyBlock(material)) {
                                if (!warnedPlayers.contains(event.getPlayer().getUniqueId())) {
                    event.getPlayer().sendMessage("§c[LagShield] §eВнимание! Сервер сейчас испытывает нагрузку. " +
                            "Размещение блоков этого типа может вызвать дополнительные лаги.");
                    warnedPlayers.add(event.getPlayer().getUniqueId());
                    
                                        Bukkit.getScheduler().runTaskLater(module.getPlugin(), 
                            () -> warnedPlayers.remove(event.getPlayer().getUniqueId()), 20L * 60 * 5);
                }
            }
        }
    }
    
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
                playerLastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
    
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        LagShieldSettings settings = module.getSettings();
        
                playerLastActivity.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        
                if (module.getTpsMonitor().getAverageTPS() < settings.getCleanupTpsThreshold()) {
            Block targetBlock = event.getBlockClicked().getRelative(event.getBlockFace());
            
                        if (event.getBucket() == Material.LAVA_BUCKET || event.getBucket() == Material.WATER_BUCKET) {
                Block[] surroundingBlocks = getSurroundingBlocks(targetBlock);
                
                for (Block surrounding : surroundingBlocks) {
                    if ((event.getBucket() == Material.LAVA_BUCKET && (surrounding.getType() == Material.WATER)) ||
                        (event.getBucket() == Material.WATER_BUCKET && (surrounding.getType() == Material.LAVA))) {
                                                if (!warnedPlayers.contains(event.getPlayer().getUniqueId())) {
                            event.getPlayer().sendMessage("§c[LagShield] §eВнимание! Сервер сейчас испытывает нагрузку. " +
                                    "Взаимодействие лавы и воды может вызвать дополнительные лаги.");
                            warnedPlayers.add(event.getPlayer().getUniqueId());
                            
                                                        Bukkit.getScheduler().runTaskLater(module.getPlugin(), 
                                    () -> warnedPlayers.remove(event.getPlayer().getUniqueId()), 20L * 60 * 5);
                        }
                        
                        break;
                    }
                }
            }
        }
    }
    
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
                module.getChunkMonitor().registerChunkLoad(event.getChunk());
    }
    
    
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
                module.getChunkMonitor().registerChunkUnload(event.getChunk());
    }
    
    
    private boolean exceedsChunkEntityLimit(int entityCount) {
        return entityCount > module.getSettings().getMaxEntitiesPerChunk();
    }
    
    
    private boolean exceedsEntityTypeLimit(EntityType entityType) {
        LagShieldSettings settings = module.getSettings();
        int limit = settings.getEntityLimitPerType();
        int count = 0;
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getType() == entityType) {
                    count++;
                    if (count > limit) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    
    private Block[] getSurroundingBlocks(Block block) {
        return new Block[] {
            block.getRelative(1, 0, 0),
            block.getRelative(-1, 0, 0),
            block.getRelative(0, 1, 0),
            block.getRelative(0, -1, 0),
            block.getRelative(0, 0, 1),
            block.getRelative(0, 0, -1)
        };
    }
    
    
    private boolean isLaggyBlock(Material material) {
        switch (material) {
            case HOPPER:
            case DROPPER:
            case DISPENSER:
            case COMPARATOR:
            case REDSTONE:
            case REDSTONE_BLOCK:
            case REDSTONE_LAMP:
            case REDSTONE_TORCH:
            case REDSTONE_WIRE:
            case PISTON:
            case STICKY_PISTON:
            case DAYLIGHT_DETECTOR:
            case OBSERVER:
                return true;
            default:
                return false;
        }
    }
    
    
    public long getPlayerLastActivity(UUID playerId) {
        return playerLastActivity.getOrDefault(playerId, 0L);
    }
    
    
    public Set<UUID> getActivePlayers(int minutes) {
        Set<UUID> activePlayers = new HashSet<>();
        long threshold = System.currentTimeMillis() - (minutes * 60 * 1000);
        
        for (Map.Entry<UUID, Long> entry : playerLastActivity.entrySet()) {
            if (entry.getValue() >= threshold) {
                activePlayers.add(entry.getKey());
            }
        }
        
        return activePlayers;
    }
} 