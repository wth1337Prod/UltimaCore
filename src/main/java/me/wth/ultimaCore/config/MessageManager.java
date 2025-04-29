package me.wth.ultimaCore.config;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.utils.GradientUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageManager {
    private final UltimaCore plugin;
    private FileConfiguration messagesConfig;
    private final File messagesFile;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    private String prefix;
    private boolean gradientsEnabled;
    private final Map<String, String[]> gradientColors = new HashMap<>();
    private List<String> highlightKeywords;

    
    public MessageManager(UltimaCore plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        loadMessages();
    }

    
    public void loadMessages() {
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
                prefix = messagesConfig.getString("settings.prefix", "&8[&b&lUltima&f&lCore&8]");
        gradientsEnabled = messagesConfig.getBoolean("settings.gradient.enabled", true);
        
                gradientColors.put("highlight", new String[]{
                messagesConfig.getString("settings.gradient.highlight.from", "4D94FF"),
                messagesConfig.getString("settings.gradient.highlight.to", "0066CC")
        });
        
        gradientColors.put("error", new String[]{
                messagesConfig.getString("settings.gradient.error.from", "FF4D4D"),
                messagesConfig.getString("settings.gradient.error.to", "CC0000")
        });
        
        gradientColors.put("success", new String[]{
                messagesConfig.getString("settings.gradient.success.from", "4DFF4D"),
                messagesConfig.getString("settings.gradient.success.to", "00CC00")
        });
        
        gradientColors.put("warning", new String[]{
                messagesConfig.getString("settings.gradient.warning.from", "FFD700"),
                messagesConfig.getString("settings.gradient.warning.to", "FFA500")
        });
        
                highlightKeywords = messagesConfig.getStringList("keywords");
    }

    
    public void saveMessages() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить файл сообщений: " + e.getMessage());
        }
    }

    
    public void reloadMessages() {
        loadMessages();
    }

    
    public String getMessage(String path, Object... replacements) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            return "§cСообщение не найдено: " + path;
        }
        
        return formatMessage(message, replacements);
    }

    
    public String formatMessage(String message, Object... replacements) {
                if (!message.contains(prefix) && !message.startsWith("&")) {
            message = prefix + " " + message;
        }
        
                if (replacements.length > 0) {
            Map<String, String> replacementMap = new HashMap<>();
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    replacementMap.put(replacements[i].toString(), replacements[i + 1].toString());
                }
            }
            
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(message);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                String replacement = replacementMap.getOrDefault(key, "{" + key + "}");
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            message = sb.toString();
        }
        
                if (gradientsEnabled) {
                        if (highlightKeywords != null && !highlightKeywords.isEmpty()) {
                message = GradientUtil.highlightKeywords(
                        message, 
                        highlightKeywords, 
                        gradientColors.get("highlight")[0], 
                        gradientColors.get("highlight")[1], 
                        true
                );
            }
            
                        message = GradientUtil.applyGradients(message);
        }
        
                message = ChatColor.translateAlternateColorCodes('&', message);
        
        return message;
    }

    
    public void sendMessage(CommandSender sender, String path, Object... replacements) {
        String message = getMessage(path, replacements);
        if (!message.isEmpty()) {
            sender.sendMessage(message);
        }
    }

    
    public void sendCustomMessage(CommandSender sender, String message, Object... replacements) {
        String formattedMessage = formatMessage(message, replacements);
        if (!formattedMessage.isEmpty()) {
            sender.sendMessage(formattedMessage);
        }
    }

    
    public void sendErrorMessage(CommandSender sender, String path, Object... replacements) {
        String message = getMessage(path, replacements);
        if (!message.isEmpty()) {
            if (gradientsEnabled && sender instanceof Player) {
                                String gradientMessage = GradientUtil.applyGradient(
                        message,
                        gradientColors.get("error")[0],
                        gradientColors.get("error")[1]
                );
                sender.sendMessage(gradientMessage);
            } else {
                sender.sendMessage(message);
            }
        }
    }

    
    public void sendSuccessMessage(CommandSender sender, String path, Object... replacements) {
        String message = getMessage(path, replacements);
        if (!message.isEmpty()) {
            if (gradientsEnabled && sender instanceof Player) {
                                String gradientMessage = GradientUtil.applyGradient(
                        message,
                        gradientColors.get("success")[0],
                        gradientColors.get("success")[1]
                );
                sender.sendMessage(gradientMessage);
            } else {
                sender.sendMessage(message);
            }
        }
    }

    
    public void sendWarningMessage(CommandSender sender, String path, Object... replacements) {
        String message = getMessage(path, replacements);
        if (!message.isEmpty()) {
            if (gradientsEnabled && sender instanceof Player) {
                                String gradientMessage = GradientUtil.applyGradient(
                        message,
                        gradientColors.get("warning")[0],
                        gradientColors.get("warning")[1]
                );
                sender.sendMessage(gradientMessage);
            } else {
                sender.sendMessage(message);
            }
        }
    }

    
    public boolean hasMessage(String path) {
        return messagesConfig.contains(path);
    }

    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    
    public String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', prefix);
    }
} 