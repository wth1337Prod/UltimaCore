package me.wth.ultimaCore.utils;

import com.sun.management.OperatingSystemMXBean;
import me.wth.ultimaCore.UltimaCore;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


public class PerformanceUtils {
    private static final double[] recentTPS = new double[]{20.0, 20.0, 20.0};
    private static long lastCPUTime = 0;
    private static long lastCPUCheck = 0;
    private static double cpuUsage = 0.0;
    
    
    static {
        try {
                        Class<?> minecraftServerClass = null;
            
                        for (Method method : Bukkit.getServer().getClass().getMethods()) {
                if (method.getName().equals("getServer")) {
                    minecraftServerClass = method.getReturnType();
                    break;
                }
            }
            
            if (minecraftServerClass != null) {
                final Field tpsField;
                
                                Field tempTpsField = null;
                for (Field field : minecraftServerClass.getDeclaredFields()) {
                    if (field.getType().isArray() && field.getType().getComponentType() == double.class) {
                        field.setAccessible(true);
                        tempTpsField = field;
                        break;
                    }
                }
                tpsField = tempTpsField;
                
                if (tpsField != null) {
                                        final Object minecraftServer;
                    Object tempServer = null;
                    for (Method method : Bukkit.getServer().getClass().getMethods()) {
                        if (method.getName().equals("getServer") && method.getParameterCount() == 0) {
                            tempServer = method.invoke(Bukkit.getServer());
                            break;
                        }
                    }
                    minecraftServer = tempServer;
                    
                    if (minecraftServer != null) {
                                                Bukkit.getScheduler().runTaskTimer(UltimaCore.getInstance(), () -> {
                            try {
                                                                double[] serverTPS = (double[]) tpsField.get(minecraftServer);
                                System.arraycopy(serverTPS, 0, recentTPS, 0, recentTPS.length);
                            } catch (Exception e) {
                                LoggerUtil.warning("Ошибка при получении TPS", e);
                            }
                        }, 100, 100);
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.warning("Не удалось инициализировать TPS через рефлексию", e);
        }
        
                Bukkit.getScheduler().runTaskTimerAsynchronously(UltimaCore.getInstance(), () -> {
            updateCPUUsage();
        }, 100, 100);
    }
    
    
    public static double[] getRecentTPS() {
        return recentTPS.clone();
    }
    
    
    private static void updateCPUUsage() {
        try {
            OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long cpuTime = osBean.getProcessCpuTime();
            long now = System.currentTimeMillis();
            
            if (lastCPUTime > 0 && lastCPUCheck > 0) {
                long timeDiff = now - lastCPUCheck;
                long cpuDiff = cpuTime - lastCPUTime;
                
                if (timeDiff > 0) {
                                        double usage = (double) cpuDiff / (timeDiff * 10000 * Runtime.getRuntime().availableProcessors());
                    cpuUsage = Math.min(100.0, usage * 100.0);
                }
            }
            
            lastCPUTime = cpuTime;
            lastCPUCheck = now;
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при получении использования CPU", e);
        }
    }
    
    
    public static double getCPUUsage() {
        return cpuUsage;
    }
    
    
    public static long getTotalGCTime() {
        long totalGcTime = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long collectionTime = gcBean.getCollectionTime();
            if (collectionTime > 0) {
                totalGcTime += collectionTime;
            }
        }
        
        return totalGcTime;
    }
    
    
    public static int getTotalEntities() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getEntities().size();
        }
        return total;
    }
    
    
    public static int getTotalChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }
    
    
    public static int getGCCount() {
        long totalGcCount = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            if (count > 0) {
                totalGcCount += count;
            }
        }
        
        return (int) totalGcCount;
    }
    
    
    public static void forceGarbageCollection() {
        System.gc();
    }
} 