package me.wth.ultimaCore.modules.memoryguard;


public class MemoryAction {
    
    public enum ActionType {
        
        EMERGENCY_GC,
        
        
        FULL_GC,
        
        
        SUGGEST_GC,
        
        
        CLEAR_ITEMS,
        
        
        CLEAR_ENTITIES,
        
        
        UNLOAD_CHUNKS,
        
        
        KICK_PLAYERS
    }
    
    private final ActionType type;
    private final long executionTime;
    
    
    public MemoryAction(ActionType type, long executionTime) {
        this.type = type;
        this.executionTime = executionTime;
    }
    
    
    public ActionType getType() {
        return type;
    }
    
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        MemoryAction other = (MemoryAction) obj;
        return type == other.type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
} 