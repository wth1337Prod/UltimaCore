package me.wth.ultimaCore.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.wth.ultimaCore.UltimaCore;
import net.md_5.bungee.api.ChatColor;


public class MessageUtil {
    
    private static UltimaCore plugin;
    private static FileConfiguration messagesConfig;
    private static final Map<String, String> cache = new HashMap<>();
    
        private static boolean useGradients = true;
    private static String gradientFrom = "00AAFF";
    private static String gradientTo = "55FFCC";
    
        private static final Map<String, List<String>> moduleKeywords = new HashMap<>();
    
    
    public static void init(UltimaCore plugin) {
        MessageUtil.plugin = plugin;
        loadMessages();
    }
    
    
    public static void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        cache.clear();
        
                useGradients = messagesConfig.getBoolean("settings.use_gradients", true);
        gradientFrom = messagesConfig.getString("settings.gradient_from", "00AAFF");
        gradientTo = messagesConfig.getString("settings.gradient_to", "55FFCC");
        
                moduleKeywords.clear();
        if (messagesConfig.contains("settings.keywords")) {
            for (String module : messagesConfig.getConfigurationSection("settings.keywords").getKeys(false)) {
                List<String> keywords = messagesConfig.getStringList("settings.keywords." + module);
                moduleKeywords.put(module, keywords);
            }
        }
    }
    
    
    public static String getMessage(String path, String defaultMessage) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        
        String message = messagesConfig.getString(path, defaultMessage);
        
                String prefix = messagesConfig.getString("settings.prefix", "&8[&bUltimaCore&8] &r");
        if (message.startsWith("{prefix}")) {
            message = message.replace("{prefix}", prefix);
        }
        
                cache.put(path, message);
        return message;
    }
    
    
    public static String getMessage(String path, String defaultMessage, Map<String, String> placeholders) {
        String message = getMessage(path, defaultMessage);
        
                if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    
    public static String format(String message, String module) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
                if (useGradients && module != null && moduleKeywords.containsKey(module)) {
            message = GradientUtil.highlightKeywords(
                message, 
                moduleKeywords.get(module), 
                gradientFrom, 
                gradientTo,
                true
            );
        }
        
                return GradientUtil.applyGradients(message);
    }
    
    
    public static void send(CommandSender sender, String message, String module) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        
                String formatted = format(message, module);
        
                sender.sendMessage(formatted);
    }
    
    
    public static void sendMessage(CommandSender sender, String path, String defaultMessage, 
                                  String module, Map<String, String> placeholders) {
        if (sender == null) {
            return;
        }
        
        String message = getMessage(path, defaultMessage, placeholders);
        send(sender, message, module);
    }
    
    
    public static void sendMessage(CommandSender sender, String path, String defaultMessage, String module) {
        sendMessage(sender, path, defaultMessage, module, null);
    }
    
    
    public static void sendError(CommandSender sender, String path, String defaultMessage, String module) {
        if (sender == null) {
            return;
        }
        
        String message = getMessage(path, defaultMessage);
        message = "&c" + message;         send(sender, message, module);
    }
    
    
    public static void broadcastMessage(String path, String defaultMessage, 
                                       String module, Map<String, String> placeholders) {
        String message = getMessage(path, defaultMessage, placeholders);
        String formatted = format(message, module);
        
        plugin.getServer().broadcastMessage(formatted);
    }
    
    
    public static void broadcastMessageWithPermission(String path, String defaultMessage, 
                                                    String module, String permission, 
                                                    Map<String, String> placeholders) {
        String message = getMessage(path, defaultMessage, placeholders);
        String formatted = format(message, module);
        
        plugin.getServer().broadcast(formatted, permission);
    }
    
    
    public static Map<String, String> createPlaceholders(String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be key-value pairs");
        }
        
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < placeholders.length; i += 2) {
            result.put(placeholders[i], placeholders[i + 1]);
        }
        
        return result;
    }
} 