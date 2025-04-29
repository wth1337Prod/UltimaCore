package me.wth.ultimaCore.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class InventoryUtils {
    
    
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(loreList);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    
    public static ItemStack createItem(Material material, String name, List<String> lore) {
        return createItem(material, name, lore.toArray(new String[0]));
    }
    
    
    public static ItemStack createGlassPane(int colorCode, String name) {
        Material material;
        
        switch (colorCode) {
            case 0:
                material = Material.WHITE_STAINED_GLASS_PANE;
                break;
            case 1:
                material = Material.ORANGE_STAINED_GLASS_PANE;
                break;
            case 2:
                material = Material.MAGENTA_STAINED_GLASS_PANE;
                break;
            case 3:
                material = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
                break;
            case 4:
                material = Material.YELLOW_STAINED_GLASS_PANE;
                break;
            case 5:
                material = Material.LIME_STAINED_GLASS_PANE;
                break;
            case 6:
                material = Material.PINK_STAINED_GLASS_PANE;
                break;
            case 7:
                material = Material.GRAY_STAINED_GLASS_PANE;
                break;
            case 8:
                material = Material.LIGHT_GRAY_STAINED_GLASS_PANE;
                break;
            case 9:
                material = Material.CYAN_STAINED_GLASS_PANE;
                break;
            case 10:
                material = Material.PURPLE_STAINED_GLASS_PANE;
                break;
            case 11:
                material = Material.BLUE_STAINED_GLASS_PANE;
                break;
            case 12:
                material = Material.BROWN_STAINED_GLASS_PANE;
                break;
            case 13:
                material = Material.GREEN_STAINED_GLASS_PANE;
                break;
            case 14:
                material = Material.RED_STAINED_GLASS_PANE;
                break;
            case 15:
                material = Material.BLACK_STAINED_GLASS_PANE;
                break;
            default:
                material = Material.GRAY_STAINED_GLASS_PANE;
        }
        
        return createItem(material, name);
    }
    
    
    public static String createProgressBar(double value, double max, int length, char completeChar, char incompleteChar) {
        double percentage = value / max;
        int completeLength = (int) (length * percentage);
        
        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < completeLength) {
                progressBar.append(completeChar);
            } else {
                progressBar.append(incompleteChar);
            }
        }
        
        return progressBar.toString();
    }
    
    
    public static String formatNumber(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000.0);
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else {
            return String.format("%.1fB", number / 1000000000.0);
        }
    }
} 