package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.api.Module;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;


public class ReloadCommand extends BaseCommand {
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Начинаю перезагрузку UltimaCore...");
        
        long startTime = System.currentTimeMillis();
        
        try {
                        plugin.reloadConfig();
            sender.sendMessage(ChatColor.YELLOW + "Конфигурация перезагружена");
            
                        int reloadedModules = 0;
            for (Module module : plugin.getModules().values()) {
                if (module instanceof me.wth.ultimaCore.api.AbstractModule) {
                    try {
                        me.wth.ultimaCore.api.AbstractModule abstractModule = (me.wth.ultimaCore.api.AbstractModule) module;
                        
                                                if (hasReloadMethod(abstractModule)) {
                            sender.sendMessage(ChatColor.YELLOW + "Перезагрузка модуля: " + module.getName());
                            invokeReloadMethod(abstractModule);
                            reloadedModules++;
                        }
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке модуля " + module.getName() + ": " + e.getMessage());
                        LoggerUtil.severe("Ошибка при перезагрузке модуля " + module.getName(), e);
                    }
                }
            }
            
            long elapsedTime = System.currentTimeMillis() - startTime;
            sender.sendMessage(ChatColor.GREEN + "UltimaCore успешно перезагружен! Перезагружено модулей: " + 
                    reloadedModules + ". Время: " + elapsedTime + "мс");
            
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке UltimaCore: " + e.getMessage());
            LoggerUtil.severe("Ошибка при перезагрузке UltimaCore", e);
        }
    }
    
    
    private boolean hasReloadMethod(me.wth.ultimaCore.api.AbstractModule module) {
        try {
            module.getClass().getMethod("reload");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
    
    
    private void invokeReloadMethod(me.wth.ultimaCore.api.AbstractModule module) {
        try {
            module.getClass().getMethod("reload").invoke(module);
        } catch (Exception e) {
            LoggerUtil.warning("Не удалось вызвать метод reload для модуля " + module.getName(), e);
        }
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return "Перезагружает плагин UltimaCore и его модули";
    }

    @Override
    public String getSyntax() {
        return "/ultimacore reload";
    }

    @Override
    public String getPermission() {
        return "ultimacore.command.reload";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"rl", "restart"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();     }
} 