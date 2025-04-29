package me.wth.ultimaCore.modules.lagshield;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.config.Config;
import me.wth.ultimaCore.api.AbstractModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;


public class LagShieldModule extends AbstractModule {
    private LagShieldSettings settings;
    private LagShieldListener listener;
    private MemoryMonitor memoryMonitor;
    private EntityLimiter entityLimiter;
    private ChunkMonitor chunkMonitor;
    private TPSMonitor tpsMonitor;
    private BukkitTask cleanupTask;
    private BukkitTask monitorTask;
    private final UltimaCore plugin;

    
    public LagShieldModule(UltimaCore plugin) {
        this.plugin = plugin;
        setPlugin(plugin);
    }

    @Override
    public void onEnable() {
        this.settings = new LagShieldSettings();
        loadConfig();
        
                this.tpsMonitor = new TPSMonitor(this);
        this.memoryMonitor = new MemoryMonitor(this);
        this.entityLimiter = new EntityLimiter(this);
        this.chunkMonitor = new ChunkMonitor(this);
        
                this.listener = new LagShieldListener(this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        
                startTasks();
        
        plugin.getLogger().info("Модуль LagShield успешно включен!");
    }

    @Override
    public void onDisable() {
                if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        
        if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        
                if (listener != null) {
            listener.unregister();
            listener = null;
        }
        
        plugin.getLogger().info("Модуль LagShield выключен.");
    }

    
    @Override
    public void onTick() {
            }

    public void loadConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("lagshield");
        
        if (section == null) {
            section = config.createSection("lagshield");
        }
        
        if (settings == null) {
            settings = new LagShieldSettings();
        }
        
        settings.loadFromConfig(section);
        config.save();
    }

    public void saveConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("lagshield");
        
        if (section == null) {
            section = config.createSection("lagshield");
        }
        
        settings.saveToConfig(section);
        config.save();
    }

    
    private void startTasks() {
                cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (settings.isAutoCleanupEnabled() && tpsMonitor.getAverageTPS() < settings.getCleanupTpsThreshold()) {
                performCleanup();
            }
        }, 20L * 60 * 2, 20L * 60 * settings.getCleanupInterval());
        
                monitorTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tpsMonitor.update();
            memoryMonitor.update();
            
            if (settings.isMemoryProtectionEnabled() && 
                    memoryMonitor.getUsedMemoryPercentage() > settings.getMemoryThreshold()) {
                performMemoryCleanup();
            }
        }, 20L, 20L);
    }

    
    public void performCleanup() {
        if (!settings.isAutoCleanupEnabled()) return;
        
        int removed = 0;
        
                if (settings.isCleanEntitiesEnabled()) {
            removed += entityLimiter.cleanupEntities();
        }
        
                if (settings.isUnloadChunksEnabled()) {
            removed += chunkMonitor.unloadInactiveChunks();
        }
        
        if (removed > 0) {
            plugin.getLogger().info("LagShield: Очищено " + removed + " объектов для повышения производительности.");
        }
    }

    
    private void performMemoryCleanup() {
        System.gc();
        plugin.getLogger().info("LagShield: Запущена сборка мусора для освобождения памяти.");
    }

    
    public LagShieldSettings getSettings() {
        return settings;
    }

    
    public TPSMonitor getTpsMonitor() {
        return tpsMonitor;
    }

    
    public MemoryMonitor getMemoryMonitor() {
        return memoryMonitor;
    }

    
    public EntityLimiter getEntityLimiter() {
        return entityLimiter;
    }

    
    public ChunkMonitor getChunkMonitor() {
        return chunkMonitor;
    }
    
    
    public UltimaCore getPlugin() {
        return plugin;
    }
    
    
    @Override
    public String getName() {
        return "LagShield";
    }
    
    
    @Override
    public String getDescription() {
        return "Модуль для защиты сервера от лагов и оптимизации производительности";
    }
} 