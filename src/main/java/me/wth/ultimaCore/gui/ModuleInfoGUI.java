package me.wth.ultimaCore.gui;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.config.Config;
import me.wth.ultimaCore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModuleInfoGUI extends AbstractGUI {
    private final Module module;
    
    
    public ModuleInfoGUI(UltimaCore plugin, Module module) {
        super(plugin, "&8⚙ &b" + module.getName(), 54);
        this.module = module;
    }
    
    @Override
    public Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, size, title);
        
                fillBorders(inventory, 5);
        
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
        
                inventory.setItem(4, InventoryUtils.createItem(Material.NAME_TAG,
                (isEnabled ? "&a&l" : "&c&l") + module.getName(),
                "",
                "&7Статус: " + (isEnabled ? "&aВключен" : "&cВыключен"),
                "&7Описание: &f" + module.getDescription()));
        
                Map<String, Object> settings = getModuleSettings();
        if (!settings.isEmpty()) {
            inventory.setItem(22, InventoryUtils.createItem(Material.WRITABLE_BOOK,
                    "&b&lНастройки модуля",
                    "",
                    "&7Здесь отображаются настройки модуля",
                    "&7из конфигурационного файла"));
            
            int slot = 28;
            int count = 0;
            
            for (Map.Entry<String, Object> entry : settings.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (count >= 14) {
                                        break;
                }
                
                                Material material;
                String valueStr;
                
                if (value instanceof Boolean) {
                    material = (Boolean) value ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
                    valueStr = (Boolean) value ? "&aВключено" : "&cВыключено";
                } else if (value instanceof Number) {
                    material = Material.CLOCK;
                    valueStr = "&e" + value;
                } else if (value instanceof String) {
                    material = Material.PAPER;
                    valueStr = "&f\"" + value + "\"";
                } else if (value instanceof List) {
                    material = Material.CHEST;
                    valueStr = "&7[список: &f" + ((List<?>) value).size() + " элементов&7]";
                } else if (value instanceof Map || value instanceof ConfigurationSection) {
                    material = Material.BOOK;
                    valueStr = "&7{секция}";
                } else {
                    material = Material.BARRIER;
                    valueStr = "&7" + (value != null ? value.toString() : "null");
                }
                
                inventory.setItem(slot, InventoryUtils.createItem(material,
                        "&e" + key,
                        "",
                        "&7Значение: " + valueStr,
                        "&7Тип: &f" + (value != null ? value.getClass().getSimpleName() : "null")));
                
                                count++;
                
                if (count % 7 == 0) {
                    slot += 2;
                } else {
                    slot++;
                }
            }
        }
        
                Map<String, Object> metrics = getModuleMetrics();
        if (!metrics.isEmpty()) {
            int slot = 13;
            
            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                Material material = Material.REDSTONE_TORCH;
                String valueStr = value != null ? value.toString() : "N/A";
                
                inventory.setItem(slot, InventoryUtils.createItem(material,
                        "&a" + key,
                        "",
                        "&7Значение: &f" + valueStr));
                
                                if (slot == 13) {
                    slot = 31;
                } else {
                    slot++;
                }
                
                if (slot >= 36) {
                    break;
                }
            }
        }
        
                inventory.setItem(49, InventoryUtils.createItem(Material.ARROW,
                "&c&lНазад",
                "",
                "&7Вернуться к списку модулей"));
        
        return inventory;
    }
    
    @Override
    public void handleClick(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event) {
        if (slot == 49) {
                        new ModulesGUI(plugin).open(player);
        }
    }
    
    
    private Map<String, Object> getModuleSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        try {
                        Config config = plugin.getConfigManager().getConfig(module.getName().toLowerCase());
            if (config != null && config.getConfig() != null) {
                                for (String key : config.getConfig().getKeys(true)) {
                    if (!config.getConfig().isConfigurationSection(key)) {
                        settings.put(key, config.getConfig().get(key));
                    }
                }
            }
        } catch (Exception e) {
                    }
        
        return settings;
    }
    
    
    private Map<String, Object> getModuleMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
                        for (Field field : module.getClass().getDeclaredFields()) {
                if (field.getName().startsWith("metric")) {
                    field.setAccessible(true);
                    Object value = field.get(module);
                    if (value != null) {
                        String name = field.getName().substring(6);                         name = name.substring(0, 1).toUpperCase() + name.substring(1);                         metrics.put(name, value);
                    }
                }
            }
            
                        for (Method method : module.getClass().getDeclaredMethods()) {
                String methodName = method.getName();
                if ((methodName.startsWith("get") || methodName.startsWith("is")) && 
                        method.getParameterCount() == 0 && 
                        !methodName.equals("getName") && 
                        !methodName.equals("getDescription") && 
                        !methodName.equals("getPlugin")) {
                    
                    method.setAccessible(true);
                    Object value = method.invoke(module);
                    
                    if (value != null && (value instanceof Number || value instanceof Boolean || value instanceof String)) {
                        String name;
                        if (methodName.startsWith("get")) {
                            name = methodName.substring(3);
                        } else {
                            name = methodName.substring(2);
                        }
                        
                                                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                        metrics.put(name, value);
                    }
                }
            }
        } catch (Exception e) {
                    }
        
        return metrics;
    }
} 