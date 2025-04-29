package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.api.Module;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ConfigCommand extends BaseCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return;
        }
        
        String option = args[0].toLowerCase();
        switch (option) {
            case "reload":
                reloadConfig(sender);
                break;
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Использование: /ultimacore config set <параметр> <значение>");
                    return;
                }
                setConfigValue(sender, args[1], args[2]);
                break;
            case "get":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Использование: /ultimacore config get <параметр>");
                    return;
                }
                getConfigValue(sender, args[1]);
                break;
            case "list":
                if (args.length < 2) {
                    listConfigSections(sender, "");
                } else {
                    listConfigSections(sender, args[1]);
                }
                break;
            case "info":
                showConfigInfo(sender);
                break;
            case "save":
                saveConfig(sender);
                break;
            default:
                showHelp(sender);
        }
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "UltimaCore - Конфигурация" + ChatColor.GOLD + " ===");
        sender.sendMessage(ChatColor.GOLD + "Использование:");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config reload" + ChatColor.GOLD + ": Перезагрузить конфигурацию");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config set <параметр> <значение>" + ChatColor.GOLD + ": Установить значение параметра");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config get <параметр>" + ChatColor.GOLD + ": Получить значение параметра");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config list [секция]" + ChatColor.GOLD + ": Показать список параметров");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config info" + ChatColor.GOLD + ": Показать информацию о конфигурации");
        sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "/ultimacore config save" + ChatColor.GOLD + ": Сохранить изменения в конфигурации");
    }
    
    
    private void reloadConfig(CommandSender sender) {
        try {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация успешно перезагружена!");
            
                        for (Module module : plugin.getModules().values()) {
                if (module instanceof me.wth.ultimaCore.api.AbstractModule) {
                    sender.sendMessage(ChatColor.YELLOW + "Перезагрузка модуля: " + module.getName());
                }
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке конфигурации: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void setConfigValue(CommandSender sender, String path, String value) {
        try {
            FileConfiguration config = plugin.getConfig();
            
                        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                config.set(path, Boolean.parseBoolean(value));
            } else if (value.matches("-?\\d+")) {
                config.set(path, Integer.parseInt(value));
            } else if (value.matches("-?\\d+\\.\\d+")) {
                config.set(path, Double.parseDouble(value));
            } else {
                config.set(path, value);
            }
            
            sender.sendMessage(ChatColor.GREEN + "Параметр " + ChatColor.YELLOW + path + 
                    ChatColor.GREEN + " установлен в значение " + ChatColor.YELLOW + value);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при установке параметра: " + e.getMessage());
        }
    }
    
    
    private void getConfigValue(CommandSender sender, String path) {
        try {
            FileConfiguration config = plugin.getConfig();
            
            if (config.contains(path)) {
                Object value = config.get(path);
                sender.sendMessage(ChatColor.GOLD + "Параметр " + ChatColor.YELLOW + path + 
                        ChatColor.GOLD + " = " + ChatColor.GREEN + value + 
                        ChatColor.GRAY + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            } else {
                sender.sendMessage(ChatColor.RED + "Параметр " + path + " не найден в конфигурации");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при получении параметра: " + e.getMessage());
        }
    }
    
    
    private void listConfigSections(CommandSender sender, String section) {
        try {
            FileConfiguration config = plugin.getConfig();
            
            if (!section.isEmpty() && !config.contains(section)) {
                sender.sendMessage(ChatColor.RED + "Секция " + section + " не найдена в конфигурации");
                return;
            }
            
            sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Параметры конфигурации" + 
                    (!section.isEmpty() ? " (" + section + ")" : "") + ChatColor.GOLD + " ===");
            
            if (section.isEmpty()) {
                                for (String key : config.getKeys(false)) {
                    if (config.isConfigurationSection(key)) {
                        sender.sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + " (секция)");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + key + ChatColor.WHITE + " = " + 
                                ChatColor.GREEN + config.get(key));
                    }
                }
            } else {
                                for (String key : config.getConfigurationSection(section).getKeys(false)) {
                    String fullPath = section + "." + key;
                    if (config.isConfigurationSection(fullPath)) {
                        sender.sendMessage(ChatColor.GOLD + key + ChatColor.GRAY + " (секция)");
                    } else {
                        sender.sendMessage(ChatColor.GOLD + key + ChatColor.WHITE + " = " + 
                                ChatColor.GREEN + config.get(fullPath));
                    }
                }
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при получении списка параметров: " + e.getMessage());
        }
    }
    
    
    private void showConfigInfo(CommandSender sender) {
        try {
            FileConfiguration config = plugin.getConfig();
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            
            sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Информация о конфигурации" + ChatColor.GOLD + " ===");
            sender.sendMessage(ChatColor.GOLD + "Файл: " + ChatColor.YELLOW + configFile.getAbsolutePath());
            sender.sendMessage(ChatColor.GOLD + "Размер: " + ChatColor.YELLOW + (configFile.length() / 1024) + " KB");
            sender.sendMessage(ChatColor.GOLD + "Последнее изменение: " + ChatColor.YELLOW + new java.util.Date(configFile.lastModified()));
            
            int sectionCount = 0;
            int paramCount = 0;
            
            for (String key : config.getKeys(true)) {
                if (config.isConfigurationSection(key)) {
                    sectionCount++;
                } else {
                    paramCount++;
                }
            }
            
            sender.sendMessage(ChatColor.GOLD + "Количество секций: " + ChatColor.YELLOW + sectionCount);
            sender.sendMessage(ChatColor.GOLD + "Количество параметров: " + ChatColor.YELLOW + paramCount);
            
                        sender.sendMessage(ChatColor.GOLD + "Модули:");
            for (Module module : plugin.getModules().values()) {
                sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + module.getName());
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при получении информации о конфигурации: " + e.getMessage());
        }
    }
    
    
    private void saveConfig(CommandSender sender) {
        try {
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация успешно сохранена!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при сохранении конфигурации: " + e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "config";
    }
    
    @Override
    public String getDescription() {
        return "Управление конфигурацией плагина";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore config [reload|set|get|list|info|save]";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.command.config";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"cfg", "configuration"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> result = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> options = Arrays.asList("reload", "set", "get", "list", "info", "save");
            for (String option : options) {
                if (option.startsWith(args[0].toLowerCase())) {
                    result.add(option);
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
                                FileConfiguration config = plugin.getConfig();
                List<String> keys = new ArrayList<>(config.getKeys(true));
                
                return keys.stream()
                        .filter(key -> key.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("list")) {
                                FileConfiguration config = plugin.getConfig();
                List<String> sections = new ArrayList<>();
                
                for (String key : config.getKeys(true)) {
                    if (config.isConfigurationSection(key) && key.toLowerCase().startsWith(args[1].toLowerCase())) {
                        sections.add(key);
                    }
                }
                
                return sections;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                                FileConfiguration config = plugin.getConfig();
                String path = args[1];
                
                if (config.isBoolean(path)) {
                    return Arrays.asList("true", "false");
                } else if (path.contains("enabled") || path.contains("disable") || path.contains("active")) {
                    return Arrays.asList("true", "false");
                }
            }
        }
        
        return result;
    }
} 