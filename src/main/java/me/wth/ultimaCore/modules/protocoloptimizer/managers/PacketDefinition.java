package me.wth.ultimaCore.modules.protocoloptimizer.managers;


public class PacketDefinition {
    private final String packetType;
    private final PacketPriority priority;
    private final boolean canBulk;
    private final boolean canCache;
    
    
    public PacketDefinition(String packetType, PacketPriority priority, boolean canBulk, boolean canCache) {
        this.packetType = packetType;
        this.priority = priority;
        this.canBulk = canBulk;
        this.canCache = canCache;
    }
    
    
    public String getPacketType() {
        return packetType;
    }
    
    
    public PacketPriority getPriority() {
        return priority;
    }
    
    
    public boolean isCanBulk() {
        return canBulk;
    }
    
    
    public boolean isCanCache() {
        return canCache;
    }
    
    @Override
    public String toString() {
        return "PacketDefinition{" +
                "packetType='" + packetType + '\'' +
                ", priority=" + priority +
                ", canBulk=" + canBulk +
                ", canCache=" + canCache +
                '}';
    }
} 