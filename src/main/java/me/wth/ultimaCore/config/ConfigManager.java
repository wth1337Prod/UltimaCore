package me.wth.ultimaCore.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;


public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, Config> configs = new HashMap<>();
    private Config mainConfig;
    
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = new Config(plugin, "config.yml");
        configs.put("config.yml", mainConfig);
    }
    
    
    public Config getConfig() {
        return mainConfig;
    }
    
    
    public Config getConfig(String fileName) {
        if (!configs.containsKey(fileName)) {
            Config config = new Config(plugin, fileName);
            configs.put(fileName, config);
            return config;
        }
        
        return configs.get(fileName);
    }
    
    
    public void addConfig(String fileName, Config config) {
        configs.put(fileName, config);
    }
    
    
    public void reloadConfigs() {
        for (Config config : configs.values()) {
            config.reload();
        }
    }
    
    
    public void saveConfigs() {
        for (Config config : configs.values()) {
            config.save();
        }
    }
} 