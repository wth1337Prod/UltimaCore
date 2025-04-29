package me.wth.ultimaCore.modules.protocoloptimizer.listeners;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;


public class    PacketListeners implements Listener {
    private final ProtocolOptimizerModule module;
    
    
    public PacketListeners(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!module.isEnabled() || !module.getSettings().isOptimizeMovementPackets()) {
            return;
        }
        
        Player player = event.getPlayer();
        
                try {
                        double distance = event.getFrom().distanceSquared(event.getTo());
            
            if (distance < 0.005) {                 module.markMovementPacketsForOptimization(player);
            } else {
                module.resetMovementPacketsOptimization(player);
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при оптимизации пакетов движения: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        try {
                        module.flushPacketsQueue(player);
            module.resetAllPacketsOptimization(player);
            
                        if (module.getSettings().isOptimizeChunkLoadOnTeleport()) {
                module.optimizeChunkLoadAfterTeleport(player, event.getTo());
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при обработке телепортации: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        if (!module.isEnabled() || !module.getSettings().isOptimizeActionPackets()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        try {
                        if (module.isActionSpamming(player, "sneak")) {
                module.optimizeActionPackets(player, "sneak");
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при оптимизации пакетов приседания: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        if (!module.isEnabled() || !module.getSettings().isOptimizeActionPackets()) {
            return;
        }
        
        Player player = event.getPlayer();
        
        try {
                        if (module.isActionSpamming(player, "sprint")) {
                module.optimizeActionPackets(player, "sprint");
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при оптимизации пакетов бега: " + e.getMessage());
        }
    }
} 