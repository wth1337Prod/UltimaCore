package me.wth.ultimaCore.messages;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.utils.GradientUtil;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MessageManager {
    private final UltimaCore plugin;
    private final Map<String, String> messages = new HashMap<>();
    private final Map<String, List<String>> keywordsByModule = new HashMap<>();
    private String defaultGradientFrom = "FF5555";
    private String defaultGradientTo = "55FFFF";
    private String prefix = "&8[&bUltima&3Core&8]&r ";
    private boolean useGradients = true;
    
    
    public MessageManager(UltimaCore plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    
    public void loadMessages() {
        messages.clear();
        keywordsByModule.clear();
        
                File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
                YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
                InputStream defaultMessagesStream = plugin.getResource("messages.yml");
        if (defaultMessagesStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultMessagesStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultMessages);
        }
        
                prefix = messagesConfig.getString("settings.prefix", prefix);
        useGradients = messagesConfig.getBoolean("settings.use-prefix", useGradients);
        defaultGradientFrom = messagesConfig.getString("settings.gradient-start", defaultGradientFrom);
        defaultGradientTo = messagesConfig.getString("settings.gradient-end", defaultGradientTo);
        
                for (String key : messagesConfig.getKeys(true)) {
            if (messagesConfig.isString(key)) {
                messages.put(key, messagesConfig.getString(key));
            }
        }
        
                Configuration keywordsSection = (Configuration) messagesConfig.getConfigurationSection("keywords");
        if (keywordsSection != null) {
            for (String module : keywordsSection.getKeys(false)) {
                List<String> keywords = messagesConfig.getStringList("keywords." + module);
                keywordsByModule.put(module, keywords);
            }
        }
        
        LoggerUtil.info("Загружено " + messages.size() + " сообщений и " + keywordsByModule.size() + " наборов ключевых слов");
    }
    
    
    public String getMessage(String key, String moduleName, Object... args) {
        String message = messages.getOrDefault(key, key);
        
                if (args.length > 0) {
            message = String.format(message, args);
        }
        
                if (!message.startsWith("NOPREFIX:")) {
            message = prefix + message;
        } else {
            message = message.substring(9);         }
        
                if (useGradients && moduleName != null && !moduleName.isEmpty()) {
            List<String> keywords = keywordsByModule.getOrDefault(
                    moduleName.toLowerCase(), new ArrayList<>());
            
            if (!keywords.isEmpty()) {
                message = GradientUtil.highlightKeywords(
                        message, keywords, defaultGradientFrom, defaultGradientTo);
            } else {
                message = GradientUtil.processColors(message);
            }
        } else {
            message = GradientUtil.processColors(message);
        }
        
        return message;
    }
    
    
    public String getMessage(String key, Object... args) {
        return getMessage(key, null, args);
    }
    
    
    public void sendMessage(Player player, String key, String moduleName, Object... args) {
        if (player == null || !player.isOnline()) return;
        
        String message = getMessage(key, moduleName, args);
        
                List<String> messages = GradientUtil.splitLongMessage(message, 256);
        for (String msg : messages) {
            player.sendMessage(msg);
        }
    }
    
    
    public void sendMessage(Player player, String key, Object... args) {
        sendMessage(player, key, null, args);
    }
    
    
    public void addKeywords(String moduleName, String... keywords) {
        List<String> keywordList = keywordsByModule.computeIfAbsent(
                moduleName.toLowerCase(), k -> new ArrayList<>());
        keywordList.addAll(Arrays.asList(keywords));
    }
    
    
    public void setDefaultGradient(String fromHex, String toHex) {
        this.defaultGradientFrom = fromHex;
        this.defaultGradientTo = toHex;
    }
    
    
    public boolean isUseGradients() {
        return useGradients;
    }
    
    
    public void setUseGradients(boolean useGradients) {
        this.useGradients = useGradients;
    }
    
    
    public String getPrefix() {
        return prefix;
    }
    
    
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    
    public void saveMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
                messagesConfig.set("settings.prefix", prefix);
        messagesConfig.set("settings.use-prefix", useGradients);
        messagesConfig.set("settings.gradient-start", defaultGradientFrom);
        messagesConfig.set("settings.gradient-end", defaultGradientTo);
        
                for (Map.Entry<String, List<String>> entry : keywordsByModule.entrySet()) {
            messagesConfig.set("keywords." + entry.getKey(), entry.getValue());
        }
        
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            LoggerUtil.warning("Не удалось сохранить файл сообщений: " + e.getMessage());
        }
    }
} 