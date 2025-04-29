package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class PacketLatencyAnalyzer {
    
    private final ProtocolOptimizerModule module;
    
        private final Map<UUID, Map<String, Long>> playerLatencyStats = new ConcurrentHashMap<>();
    
        private Map<String, Long> trafficStats = new ConcurrentHashMap<>();
    
        private final Map<String, AtomicLong> globalLatencyStats = new ConcurrentHashMap<>();
    private final Map<String, Integer> packetTypeStats = new ConcurrentHashMap<>();
    private int activePlayerCount = 0;
    
    
    public PacketLatencyAnalyzer(ProtocolOptimizerModule module) {
        this.module = module;
        
                trafficStats.put("TRAFFIC_SAVED", 0L);
        trafficStats.put("PACKETS_OPTIMIZED", 0L);
        
                Bukkit.getScheduler().runTaskTimerAsynchronously(module.getPlugin(), 
                this::monitorLatency, 20L, 20L);
    }
    
    
    private void monitorLatency() {
                for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerLatency(player);
        }
    }
    
    
    private void updatePlayerLatency(Player player) {
        if (player == null) return;
        
        UUID playerUUID = player.getUniqueId();
        int ping = getPlayerPing(player);
        
                Map<String, Long> playerStats = playerLatencyStats.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        playerStats.put("PING", (long) ping);
        playerStats.put("LAST_UPDATE", System.currentTimeMillis());
        
                updateGlobalLatencyStats(ping);
    }
    
    
    private int getPlayerPing(Player player) {
        try {
            return player.getPing();
        } catch (Exception e) {
                        return 0;
        }
    }
    
    
    private void updateGlobalLatencyStats(int ping) {
                globalLatencyStats.computeIfAbsent("TOTAL_PING", k -> new AtomicLong(0)).addAndGet(ping);
        globalLatencyStats.computeIfAbsent("PING_SAMPLES", k -> new AtomicLong(0)).incrementAndGet();
        
                AtomicLong minPing = globalLatencyStats.computeIfAbsent("MIN_PING", k -> new AtomicLong(Integer.MAX_VALUE));
        while (true) {
            long current = minPing.get();
            if (ping >= current || minPing.compareAndSet(current, ping)) break;
        }
        
                AtomicLong maxPing = globalLatencyStats.computeIfAbsent("MAX_PING", k -> new AtomicLong(0));
        while (true) {
            long current = maxPing.get();
            if (ping <= current || maxPing.compareAndSet(current, ping)) break;
        }
    }
    
    
    public void updateNetworkStats(int playerCount, int packetCount, Map<String, Integer> packetTypes) {
        this.activePlayerCount = playerCount;
        
                for (Map.Entry<String, Integer> entry : packetTypes.entrySet()) {
            packetTypeStats.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        
                globalLatencyStats.computeIfAbsent("TOTAL_PACKETS", k -> new AtomicLong(0)).addAndGet(packetCount);
    }
    
    
    public void updateTrafficStats(Map<String, Long> stats) {
        this.trafficStats = new ConcurrentHashMap<>(stats);
    }
    
    
    public Map<String, Long> getTrafficStats() {
        return new HashMap<>(trafficStats);
    }
    
    
    public String getOverallStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("&7=== &eОбщая статистика задержек &7===\n");
        
                long totalPing = globalLatencyStats.getOrDefault("TOTAL_PING", new AtomicLong(0)).get();
        long pingCount = globalLatencyStats.getOrDefault("PING_SAMPLES", new AtomicLong(0)).get();
        double avgPing = pingCount > 0 ? (double) totalPing / pingCount : 0;
        
        stats.append("&7Активных игроков: &e").append(activePlayerCount).append("\n");
        stats.append("&7Средний пинг: &e").append(String.format("%.2f", avgPing)).append(" мс\n");
        
        long minPing = globalLatencyStats.getOrDefault("MIN_PING", new AtomicLong(0)).get();
        if (minPing < Integer.MAX_VALUE) {
            stats.append("&7Минимальный пинг: &a").append(minPing).append(" мс\n");
        }
        
        long maxPing = globalLatencyStats.getOrDefault("MAX_PING", new AtomicLong(0)).get();
        stats.append("&7Максимальный пинг: &c").append(maxPing).append(" мс\n");
        
                long totalPackets = globalLatencyStats.getOrDefault("TOTAL_PACKETS", new AtomicLong(0)).get();
        stats.append("&7Всего обработано пакетов: &e").append(totalPackets).append("\n");
        
                long trafficSaved = trafficStats.getOrDefault("TRAFFIC_SAVED", 0L);
        if (trafficSaved > 0) {
            stats.append("&7Сэкономлено трафика: &a").append(formatByteSize(trafficSaved)).append("\n");
        }
        
        long packetsOptimized = trafficStats.getOrDefault("PACKETS_OPTIMIZED", 0L);
        if (packetsOptimized > 0) {
            stats.append("&7Оптимизировано пакетов: &a").append(packetsOptimized).append("\n");
        }
        
        return stats.toString();
    }
    
    
    public String getPlayerLatencyStats(UUID playerUUID) {
        Map<String, Long> playerStats = playerLatencyStats.get(playerUUID);
        if (playerStats == null || playerStats.isEmpty()) {
            return "&cСтатистика задержек для этого игрока отсутствует.";
        }
        
        Player player = Bukkit.getPlayer(playerUUID);
        String playerName = player != null ? player.getName() : playerUUID.toString();
        
        StringBuilder stats = new StringBuilder();
        stats.append("&7=== &eСтатистика задержек для &6").append(playerName).append(" &7===\n");
        
        long ping = playerStats.getOrDefault("PING", 0L);
        long lastUpdate = playerStats.getOrDefault("LAST_UPDATE", 0L);
        
        stats.append("&7Текущий пинг: &e").append(ping).append(" мс\n");
        
        if (lastUpdate > 0) {
            long timeSinceUpdate = System.currentTimeMillis() - lastUpdate;
            stats.append("&7Последнее обновление: &e").append(formatTime(timeSinceUpdate)).append(" назад\n");
        }
        
        return stats.toString();
    }
    
    
    public void clearAllData() {
        playerLatencyStats.clear();
        globalLatencyStats.clear();
        packetTypeStats.clear();
        trafficStats.clear();
        
                trafficStats.put("TRAFFIC_SAVED", 0L);
        trafficStats.put("PACKETS_OPTIMIZED", 0L);
    }
    
    
    public void shutdown() {
            }
    
    
    private String formatByteSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " Б";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f КБ", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f МБ", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f ГБ", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    
    private String formatTime(long ms) {
        if (ms < 1000) {
            return ms + " мс";
        } else if (ms < 60 * 1000) {
            return String.format("%.1f сек", ms / 1000.0);
        } else {
            return String.format("%d мин %d сек", ms / (60 * 1000), (ms % (60 * 1000)) / 1000);
        }
    }
    
    
    public void resetStats() {
        this.playerLatencyStats.clear();
        this.globalLatencyStats.clear();
        this.packetTypeStats.clear();
        this.trafficStats.clear();
        
                trafficStats.put("TRAFFIC_SAVED", 0L);
        trafficStats.put("PACKETS_OPTIMIZED", 0L);
        trafficStats.put("TOTAL_PACKETS", 0L);
        
        LoggerUtil.debug("Сброшена статистика анализатора задержек пакетов");
    }
} 