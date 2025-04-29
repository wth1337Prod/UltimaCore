package me.wth.ultimaCore.utils;

import me.wth.ultimaCore.UltimaCore;

import java.util.logging.Level;
import java.util.logging.Logger;


public class LoggerUtil {
    private static final String PREFIX = "[UltimaCore] ";
    private static LoggerUtil instance;
    
    
    public static LoggerUtil getInstance() {
        if (instance == null) {
            instance = new LoggerUtil();
        }
        return instance;
    }
    
    
    private static Logger getLogger() {
        return UltimaCore.getInstance().getLogger();
    }
    
    
    public static void info(String message) {
        getLogger().info(PREFIX + message);
    }
    
    
    public static void warning(String message) {
        getLogger().warning(PREFIX + message);
    }
    
    
    public static void warning(String message, Exception e) {
        getLogger().log(Level.WARNING, PREFIX + message, e);
    }
    
    
    public static void severe(String message) {
        getLogger().severe(PREFIX + message);
    }
    
    
    public static void severe(String message, Exception e) {
        getLogger().log(Level.SEVERE, PREFIX + message, e);
    }
    
    
    public static void debug(String message) {
        if (UltimaCore.getInstance().getConfig().getBoolean("debug", false)) {
            getLogger().info(PREFIX + "[DEBUG] " + message);
        }
    }

    
    public static void error(String message) {
        getLogger().severe(PREFIX + message);
    }
    
    
    public static void error(String message, Exception e) {
        getLogger().log(Level.SEVERE, PREFIX + message, e);
    }
} 