package me.wth.ultimaCore.modules.entityoptimizer;


public enum AIOptimizationLevel {
    
    FULL(5),
    
    
    HIGH(4),
    
    
    MEDIUM(3),
    
    
    LOW(2),
    
    
    MINIMAL(1);
    
    private final int value;
    
    AIOptimizationLevel(int value) {
        this.value = value;
    }
    
    
    public int getValue() {
        return value;
    }
    
    
    public static AIOptimizationLevel getLower(AIOptimizationLevel level) {
        switch (level) {
            case FULL:
                return HIGH;
            case HIGH:
                return MEDIUM;
            case MEDIUM:
                return LOW;
            default:
                return MINIMAL;
        }
    }
    
    
    public static AIOptimizationLevel getHigher(AIOptimizationLevel level) {
        switch (level) {
            case MINIMAL:
                return LOW;
            case LOW:
                return MEDIUM;
            case MEDIUM:
                return HIGH;
            default:
                return FULL;
        }
    }
} 