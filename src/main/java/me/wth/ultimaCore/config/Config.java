package me.wth.ultimaCore.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;


public class Config {
    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;
    
    
    public Config(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        reload();
    }
    
    
    public void reload() {
        if (!configFile.exists()) {
            plugin.saveResource(configFile.getName(), false);
        }
        
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    
    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить конфигурацию: " + e.getMessage());
        }
    }
    
    
    public ConfigurationSection getConfigurationSection(String path) {
        return config.getConfigurationSection(path);
    }
    
    
    public ConfigurationSection createSection(String path) {
        return config.createSection(path);
    }
    
    
    public FileConfiguration getConfig() {
        return config;
    }
} 