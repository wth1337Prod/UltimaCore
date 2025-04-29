package me.wth.ultimaCore.modules.protocoloptimizer.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class MessageUtils {

    
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bUltimaCore&8] &f" + message));
    }

    
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bUltimaCore&8] &c" + message));
    }

    
    public static void sendSuccessMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bUltimaCore&8] &a" + message));
    }

    
    public static void sendInfoMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&bUltimaCore&8] &e" + message));
    }

    
    public static void sendHeader(CommandSender sender, String title) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8=== &b" + title + " &8==="));
    }

    
    public static void sendListItem(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- " + message));
    }

    
    public static void sendDivider(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8==================="));
    }

    
    public static void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cИспользование: &7" + usage));
    }

    
    public static void sendToggleMessage(CommandSender sender, String feature, boolean enabled) {
        String status = enabled ? "&aвключен" : "&cвыключен";
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7" + feature + " теперь " + status));
    }

    
    public static void sendStat(CommandSender sender, String statName, Object value) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7" + statName + ": &f" + value));
    }

    
    public static String formatNumber(int number) {
        return String.format("%,d", number).replace(',', ' ');
    }

    
    public static String formatDecimal(double number, int precision) {
        return String.format("%." + precision + "f", number);
    }

    
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return formatDecimal(bytes / 1024.0, 2) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return formatDecimal(bytes / (1024.0 * 1024.0), 2) + " MB";
        } else {
            return formatDecimal(bytes / (1024.0 * 1024.0 * 1024.0), 2) + " GB";
        }
    }

    
    public static boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sendErrorMessage(sender, "У вас нет разрешения на выполнение этой команды.");
            return false;
        }
        return true;
    }

    
    public static boolean isPlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sendErrorMessage(sender, "Эта команда может быть выполнена только игроком.");
            return false;
        }
        return true;
    }
} 