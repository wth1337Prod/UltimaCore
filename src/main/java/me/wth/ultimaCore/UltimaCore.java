package me.wth.ultimaCore;

import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.commands.CommandManager;
import me.wth.ultimaCore.config.ConfigManager;
import me.wth.ultimaCore.messages.MessageManager;
import me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule;
import me.wth.ultimaCore.modules.entityoptimizer.EntityOptimizerModule;
import me.wth.ultimaCore.modules.lagshield.LagShieldModule;
import me.wth.ultimaCore.modules.memoryguard.MemoryGuardModule;
import me.wth.ultimaCore.modules.networkoptimizer.NetworkOptimizerModule;
import me.wth.ultimaCore.modules.performanceanalytics.PerformanceAnalyticsModule;
import me.wth.ultimaCore.modules.physicsoptimizer.PhysicsOptimizerModule;
import me.wth.ultimaCore.modules.pluginoptimizer.PluginOptimizerModule;
import me.wth.ultimaCore.modules.redstonegenius.RedstoneGeniusModule;
import me.wth.ultimaCore.modules.threadmaster.ThreadMasterModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class UltimaCore extends JavaPlugin {
    private static UltimaCore instance;
    private final Map<String, Module> modules = new HashMap<>();
    private CommandManager commandManager;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        LoggerUtil.info("Инициализация UltimaCore...");
        
                configManager = new ConfigManager(this);
        
                messageManager = new MessageManager(this);
        
                registerModules();
        
                commandManager = new CommandManager(this);
        
                startModuleTicker();
        
        LoggerUtil.info("UltimaCore успешно инициализирован!");
    }

    @Override
    public void onDisable() {
        LoggerUtil.info("Отключение UltimaCore...");
        
                for (Module module : modules.values()) {
            try {
                LoggerUtil.info("Отключение модуля " + module.getName() + "...");
                module.onDisable();
            } catch (Exception e) {
                LoggerUtil.severe("Ошибка при отключении модуля " + module.getName(), e);
            }
        }
        
        LoggerUtil.info("UltimaCore успешно отключен!");
    }
    
    
    private void registerModules() {
        registerModule(new ChunkMasterModule());
        registerModule(new EntityOptimizerModule());
        registerModule(new RedstoneGeniusModule());
        registerModule(new ThreadMasterModule());
        registerModule(new MemoryGuardModule());
        registerModule(new PhysicsOptimizerModule());
        registerModule(new LagShieldModule(this));
        registerModule(new PerformanceAnalyticsModule());
        registerModule(new PluginOptimizerModule());
        registerModule(new NetworkOptimizerModule());
        registerModule(new me.wth.ultimaCore.modules.protocoloptimizer.ProtocolOptimizerModule(this));
        
                for (Module module : modules.values()) {
            try {
                LoggerUtil.info("Инициализация модуля " + module.getName() + "...");
                module.onEnable();
            } catch (Exception e) {
                LoggerUtil.severe("Ошибка при инициализации модуля " + module.getName(), e);
            }
        }
    }
    
    
    private void registerModule(Module module) {
        module.setPlugin(this);
        modules.put(module.getName().toLowerCase(), module);
    }
    
    
    private void startModuleTicker() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Module module : modules.values()) {
                try {
                    if (module instanceof me.wth.ultimaCore.api.AbstractModule abstractModule && abstractModule.isEnabled()) {
                        module.onTick();
                    }
                } catch (Exception e) {
                    LoggerUtil.warning("Ошибка при выполнении тика в модуле " + module.getName(), e);
                }
            }
        }, 1L, 1L);
    }
    
    
    public Module getModule(String name) {
        return modules.get(name.toLowerCase());
    }
    
    
    public Map<String, Module> getModules() {
        return new HashMap<>(modules);
    }
    
    
    public CommandManager getCommandManager() {
        return commandManager;
    }
    
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
    
    
    public static UltimaCore getInstance() {
        return instance;
    }
}
