package me.wth.ultimaCore.api;

import me.wth.ultimaCore.UltimaCore;


public interface Module {
    
    void onEnable();

    
    void onDisable();

    
    void onTick();

    
    String getName();

    
    String getDescription();

    
    void reload();

    
    void setPlugin(UltimaCore plugin);
    
    
    UltimaCore getPlugin();
} 