package me.wth.ultimaCore.api;

import me.wth.ultimaCore.UltimaCore;


public abstract class AbstractModule implements Module {
    protected UltimaCore plugin;
    private boolean enabled = false;
    
    @Override
    public void setPlugin(UltimaCore plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public UltimaCore getPlugin() {
        return plugin;
    }
    
    
    public boolean isEnabled() {
        return enabled;
    }
    
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void onEnable() {
        setEnabled(true);
    }
    
    @Override
    public void onDisable() {
        setEnabled(false);
    }
    
    @Override
    public void onTick() {
            }
    
    @Override
    public void reload() {
        if (isEnabled()) {
            onDisable();
            onEnable();
        }
    }
} 