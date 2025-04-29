package me.wth.ultimaCore.modules.redstonegenius;

import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;


public class RedstoneGenius extends AbstractModule {
    
    private RedstoneTracker tracker;
    private RedstoneSettings settings;
    private RedstoneListener listener;
    private boolean debug;
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                loadConfig();
        
                this.tracker = new RedstoneTracker(settings);
        
                this.listener = new RedstoneListener(this);
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        
        LoggerUtil.info("Модуль RedstoneGenius успешно включен.");
        
        if (debug) {
            LoggerUtil.debug("Настройки RedstoneGenius:");
            LoggerUtil.debug(" - Макс. активаций на блок: " + settings.getMaxActivationsPerBlock());
            LoggerUtil.debug(" - Макс. активаций на чанк: " + settings.getMaxActivationsPerChunk());
            LoggerUtil.debug(" - Макс. частота часов: " + settings.getMaxClockFrequency() + " Гц");
            LoggerUtil.debug(" - Продолжительность блокировки: " + settings.getThrottleDuration() + " сек");
            LoggerUtil.debug(" - Отключение часов: " + settings.isDisableClocks());
        }
    }
    
    @Override
    public void onDisable() {
                HandlerList.unregisterAll(listener);
        
                if (tracker != null) {
            tracker.reset();
        }
        
        LoggerUtil.info("Модуль RedstoneGenius отключен.");
        super.onDisable();
    }
    
    @Override
    public String getName() {
        return "RedstoneGenius";
    }
    
    @Override
    public String getDescription() {
        return "Модуль оптимизации редстоуновых механизмов и схем";
    }
    
    @Override
    public void reload() {
                tracker.reset();
        
                loadConfig();
        
        LoggerUtil.info("Модуль RedstoneGenius перезагружен.");
    }
    
    
    private void loadConfig() {
        plugin.reloadConfig();
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("modules.redstonegenius");
        
                if (config == null) {
            config = plugin.getConfig().createSection("modules.redstonegenius");
        }
        
                this.debug = plugin.getConfig().getBoolean("debug", false);
        
                if (settings == null) {
            settings = new RedstoneSettings();
        }
        settings.loadFromConfig(config);
    }
    
    
    public boolean handleCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showStatus(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "status":
                showStatus(sender);
                return true;
            case "reload":
                reload();
                sender.sendMessage(ChatColor.GREEN + "Модуль RedstoneGenius перезагружен.");
                return true;
            case "reset":
                tracker.reset();
                sender.sendMessage(ChatColor.GREEN + "Данные трекера редстоуна сброшены.");
                return true;
            default:
                return false;
        }
    }
    
    
    private void showStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Статус RedstoneGenius" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.GOLD + "Обнаружено часов: " + ChatColor.WHITE + tracker.getDetectedClocksCount());
        sender.sendMessage(ChatColor.GOLD + "Отслеживаемых локаций: " + ChatColor.WHITE + tracker.getTrackedLocationsCount());
        sender.sendMessage(ChatColor.GOLD + "Заблокированных локаций: " + ChatColor.WHITE + tracker.getThrottledLocationsCount());
        sender.sendMessage(ChatColor.GOLD + "Ограничения:");
        sender.sendMessage(ChatColor.GOLD + " - Макс. активаций на блок: " + ChatColor.WHITE + settings.getMaxActivationsPerBlock());
        sender.sendMessage(ChatColor.GOLD + " - Макс. активаций на чанк: " + ChatColor.WHITE + settings.getMaxActivationsPerChunk());
        sender.sendMessage(ChatColor.GOLD + " - Макс. частота часов: " + ChatColor.WHITE + settings.getMaxClockFrequency() + " Гц");
        sender.sendMessage(ChatColor.GOLD + " - Длительность блокировки: " + ChatColor.WHITE + settings.getThrottleDuration() + " сек");
    }
    
    
    public RedstoneTracker getTracker() {
        return tracker;
    }
    
    
    public RedstoneSettings getSettings() {
        return settings;
    }
    
    
    public boolean isDebug() {
        return debug;
    }
    
    
    public LoggerUtil getLogger() {
        return LoggerUtil.getInstance();
    }
} 