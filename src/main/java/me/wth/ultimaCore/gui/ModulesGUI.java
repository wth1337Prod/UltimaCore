package me.wth.ultimaCore.gui;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ModulesGUI extends AbstractGUI {
    
    public ModulesGUI(UltimaCore plugin) {
        super(plugin, "&8📚 &bМодули UltimaCore", 54);
    }
    
    @Override
    public Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
                fillBorders(inventory, 3);
        
                inventory.setItem(4, InventoryUtils.createItem(Material.BEACON,
                "&e&lИнформация о модулях",
                "",
                "&7Всего модулей: &f" + plugin.getModules().size(),
                "&7Нажмите на модуль для подробностей"));
        
                Map<String, Module> modules = plugin.getModules();
        int slot = 10;
        int row = 1;
        int col = 1;
        
        for (Module module : modules.values()) {
            boolean isEnabled = false;
            if (module instanceof AbstractModule) {
                isEnabled = ((AbstractModule) module).isEnabled();
            } else {
                                try {
                    Method isEnabledMethod = module.getClass().getMethod("isEnabled");
                    isEnabled = (boolean) isEnabledMethod.invoke(module);
                } catch (Exception ignored) {
                                        isEnabled = true;
                }
            }
            
            Material material = getModuleMaterial(module.getName());
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("&7Статус: " + (isEnabled ? "&aВключен" : "&cВыключен"));
            lore.add("&7Описание: &f" + module.getDescription());
            lore.add("");
            lore.add("&7Нажмите для просмотра информации");
            
            inventory.setItem(slot, InventoryUtils.createItem(material, 
                    (isEnabled ? "&a&l" : "&c&l") + module.getName(), 
                    lore));
            
                        col++;
            if (col > 7) {
                col = 1;
                row++;
                slot = 9 * row + 1;
            } else {
                slot++;
            }
        }
        
                inventory.setItem(49, InventoryUtils.createItem(Material.ARROW,
                "&c&lНазад",
                "",
                "&7Вернуться к общей информации"));
        
        return inventory;
    }
    
    @Override
    public void handleClick(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event) {
        if (slot == 49) {
                        new ServerStatusGUI(plugin).open(player);
            return;
        }
        
                if (clickedItem != null && clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            
            for (Module module : plugin.getModules().values()) {
                if (module.getName().equalsIgnoreCase(displayName)) {
                                        new ModuleInfoGUI(plugin, module).open(player);
                    break;
                }
            }
        }
    }
    
    
    private Material getModuleMaterial(String moduleName) {
        switch (moduleName.toLowerCase()) {
            case "chunkmaster": 
                return Material.ENDER_PEARL;
            case "entityoptimizer": 
                return Material.ZOMBIE_HEAD;
            case "lagshield": 
                return Material.SHIELD;
            case "memoryguard": 
                return Material.CHEST;
            case "networkoptimizer": 
                return Material.REPEATER;
            case "performanceanalytics": 
                return Material.COMPASS;
            case "physicsoptimizer": 
                return Material.PISTON;
            case "pluginoptimizer": 
                return Material.COMMAND_BLOCK;
            case "protocoloptimizer": 
                return Material.CONDUIT;
            case "redstonegenius": 
                return Material.REDSTONE;
            case "threadmaster": 
                return Material.CLOCK;
            default: 
                return Material.BOOK;
        }
    }
} 