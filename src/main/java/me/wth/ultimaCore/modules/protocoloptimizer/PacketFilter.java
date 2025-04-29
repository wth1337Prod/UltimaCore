package me.wth.ultimaCore.modules.protocoloptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


public class PacketFilter {
    private final ProtocolOptimizerModule module;
    
        private final Map<String, AtomicInteger> packetTypeCounts = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, AtomicInteger>> playerPacketCounts = new ConcurrentHashMap<>();
    
        private final Set<String> blockedPacketTypes = new HashSet<>();
    
        private final AtomicInteger totalPacketsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalPacketsFiltered = new AtomicInteger(0);
    
    
    public PacketFilter(ProtocolOptimizerModule module) {
        this.module = module;
        
                if (module.getSettings() != null) {
            blockedPacketTypes.addAll(module.getSettings().getBlockedPacketTypes());
        }
    }
    
    
    public boolean shouldFilterPacket(String packetType, UUID playerUUID) {
        if (packetType == null) return false;
        
                packetTypeCounts.computeIfAbsent(packetType, k -> new AtomicInteger(0)).incrementAndGet();
        totalPacketsProcessed.incrementAndGet();
        
                if (playerUUID != null) {
            playerPacketCounts.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>())
                    .computeIfAbsent(packetType, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
                if (blockedPacketTypes.contains(packetType)) {
            totalPacketsFiltered.incrementAndGet();
            return true;
        }
        
                if (playerUUID != null && module.shouldLimit(playerUUID, packetType)) {
            totalPacketsFiltered.incrementAndGet();
            return true;
        }
        
        return false;
    }
    
    
    public void addBlockedPacketType(String packetType) {
        if (packetType != null && !packetType.isEmpty()) {
            blockedPacketTypes.add(packetType);
            LoggerUtil.debug("Добавлен тип пакета в черный список: " + packetType);
        }
    }
    
    
    public void removeBlockedPacketType(String packetType) {
        if (packetType != null) {
            blockedPacketTypes.remove(packetType);
            LoggerUtil.debug("Удален тип пакета из черного списка: " + packetType);
        }
    }
    
    
    public void clearBlockedPacketTypes() {
        blockedPacketTypes.clear();
        LoggerUtil.debug("Очищен черный список типов пакетов");
    }
    
    
    public void clearPlayerStats(UUID playerUUID) {
        if (playerUUID != null) {
            playerPacketCounts.remove(playerUUID);
        }
    }
    
    
    public Map<String, Integer> getPacketTypeCounts() {
        Map<String, Integer> counts = new HashMap<>();
        
        for (Map.Entry<String, AtomicInteger> entry : packetTypeCounts.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().get());
        }
        
        return counts;
    }
    
    
    public String getStats() {
        StringBuilder stats = new StringBuilder();
        
        int total = totalPacketsProcessed.get();
        int filtered = totalPacketsFiltered.get();
        double percent = total > 0 ? (double) filtered / total * 100 : 0;
        
        stats.append("&7=== &eСтатистика фильтра пакетов &7===\n");
        stats.append("&7Всего обработано пакетов: &e").append(total).append("\n");
        stats.append("&7Отфильтровано пакетов: &c").append(filtered).append(" (");
        stats.append(String.format("%.2f", percent)).append("%)\n");
        
                if (!blockedPacketTypes.isEmpty()) {
            stats.append("&7Заблокированные типы пакетов:\n");
            for (String type : blockedPacketTypes) {
                stats.append("  &c").append(type).append("\n");
            }
        }
        
                stats.append("&7Топ типы пакетов:\n");
        packetTypeCounts.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().get(), e1.getValue().get()))
                .limit(5)
                .forEach(entry -> stats.append("  &e").append(entry.getKey()).append("&7: &a")
                        .append(entry.getValue().get()).append("\n"));
        
        return stats.toString();
    }
    
    
    public int getPacketCount(String packetType) {
        AtomicInteger count = packetTypeCounts.get(packetType);
        return count != null ? count.get() : 0;
    }
    
    
    public int getPlayerPacketCount(UUID playerUUID, String packetType) {
        Map<String, AtomicInteger> playerCounts = playerPacketCounts.get(playerUUID);
        if (playerCounts == null) return 0;
        
        AtomicInteger count = playerCounts.get(packetType);
        return count != null ? count.get() : 0;
    }
    
    
    public void resetStats() {
        packetTypeCounts.clear();
        playerPacketCounts.clear();
        totalPacketsProcessed.set(0);
        totalPacketsFiltered.set(0);
    }
} 