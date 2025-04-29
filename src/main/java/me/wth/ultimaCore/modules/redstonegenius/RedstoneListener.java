package me.wth.ultimaCore.modules.redstonegenius;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;


public class RedstoneListener implements Listener {

    private final RedstoneGenius module;
    private final RedstoneTracker tracker;
    private final RedstoneSettings settings;
    private final LoggerUtil logger;

    
    public RedstoneListener(RedstoneGenius module) {
        this.module = module;
        this.tracker = module.getTracker();
        this.settings = module.getSettings();
        this.logger = module.getLogger();
    }

    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onRedstone(BlockRedstoneEvent event) {
                if (!module.isEnabled()) {
            return;
        }

                Block block = event.getBlock();
        Material material = block.getType();
        Chunk chunk = block.getChunk();
        Location location = block.getLocation();
        
                if (settings.isMaterialIgnored(material.name())) {
            return;
        }

                int currentLevel = event.getOldCurrent();
        int newLevel = event.getNewCurrent();
        
                if (currentLevel == newLevel) {
            return;
        }
        
                boolean shouldThrottle = tracker.registerActivation(location, material);
        
                if (tracker.isThrottled(location) || shouldThrottle) {
                        if (settings.isThrottlingEnabled()) {
                event.setNewCurrent(currentLevel);
                
                                if (module.isDebug()) {
                    logger.debug("Заблокирована редстоун-активация на " + locationToString(location) + " (" + material + ")");
                }
            }
            
                        if (settings.isClockDetectionEnabled() && tracker.checkForClockPattern(location)) {
                                if (settings.isAutoDisableClocks()) {
                                        event.setNewCurrent(0);
                    
                                        logger.info("Обнаружены и отключены редстоуновые часы на " + locationToString(location));
                } else {
                                        logger.warning("Обнаружены редстоуновые часы на " + locationToString(location));
                }
            }
        }
    }
    
    
    private String locationToString(Location location) {
        return location.getWorld().getName() + 
               " [x=" + location.getBlockX() + 
               ", y=" + location.getBlockY() + 
               ", z=" + location.getBlockZ() + "]";
    }
} 