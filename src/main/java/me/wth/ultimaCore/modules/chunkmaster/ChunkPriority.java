package me.wth.ultimaCore.modules.chunkmaster;


public enum ChunkPriority {
    CRITICAL(3),       HIGH(2),           NORMAL(1),         LOW(0);        
    private final int value;

    ChunkPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    
    public static ChunkPriority getLower(ChunkPriority priority) {
        switch (priority) {
            case CRITICAL:
                return HIGH;
            case HIGH:
                return NORMAL;
            default:
                return LOW;
        }
    }
} 