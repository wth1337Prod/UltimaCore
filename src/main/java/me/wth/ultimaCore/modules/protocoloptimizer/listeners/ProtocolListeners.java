package me.wth.ultimaCore.modules.protocoloptimizer.listeners;

import me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;


public class ProtocolListeners implements Listener {
    private final ProtocolOptimizerModule module;
    
    
    public ProtocolListeners(ProtocolOptimizerModule module) {
        this.module = module;
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        try {
                        module.initializePlayerData(player);
            
                        if (module.getSettings().isDelayInitialPackets()) {
                module.delayInitialPacketsFor(player);
            }
            
                        LoggerUtil.debug("[ProtocolOptimizer] Игрок " + player.getName() + 
                    " подключился, инициализированы данные оптимизации протокола.");
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при обработке входа игрока: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        try {
                        module.cleanupPlayerData(player);
            
                        LoggerUtil.debug("[ProtocolOptimizer] Игрок " + player.getName() + 
                    " отключился, очищены данные оптимизации протокола.");
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при очистке данных игрока: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!module.isEnabled() || event.isCancelled()) {
            return;
        }
        
        try {
            if (module.getSettings().isOptimizeEntityPackets()) {
                                module.optimizeEntityPackets(event.getEntity());
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при оптимизации пакетов сущности: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        
        try {
            if (module.getSettings().isOptimizeChunkPackets()) {
                                module.optimizeChunkPackets(event.getChunk());
            }
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при оптимизации пакетов чанка: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        
        try {
                        module.clearChunkData(event.getChunk());
        } catch (Exception e) {
            LoggerUtil.error("[ProtocolOptimizer] Ошибка при очистке данных чанка: " + e.getMessage());
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        if (!module.isEnabled()) {
            return;
        }
        
        String command = event.getCommand().toLowerCase();
        
                if (command.startsWith("tps") || command.startsWith("lag") || 
                command.startsWith("mem") || command.startsWith("gc")) {
            
                        module.collectNetworkStats();
            
                        module.updateOptimizationState();
        }
    }
} 