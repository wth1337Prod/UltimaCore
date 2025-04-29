package me.wth.ultimaCore.modules.physicsoptimizer;

import org.bukkit.configuration.ConfigurationSection;


public class PhysicsOptimizerSettings {
        private boolean enabled = true;                         private boolean limitAllPhysics = true;                 private int physicsEventThreshold = 5000;               private double eventThresholdCancelChance = 0.75;       
        private int chunkCooldownTicks = 20;                    private double chunkCooldownCancelChance = 0.9;         private int maxPhysicsPerChunk = 64;                    
        private boolean enablePhysicsRateLimit = true;          private int physicsProcessingInterval = 2;              private int maxQueuedPhysicsUpdates = 1000;             
        private boolean limitFallingBlocks = true;              private int maxFallingBlocksPerWorld = 400;             private double fallingBlockLimitationChance = 0.85;     private boolean mergeFallingBlocks = true;              private int fallingBlocksMergeThreshold = 150;          private double fallingBlocksMergeChance = 0.7;          
        private boolean limitFluidFlow = true;                  private int maxFluidUpdatesPerSecond = 3000;            private double fluidLimitationChance = 0.8;             private double lavaFlowReduceChance = 0.5;              
        private boolean optimizeExplosions = true;              private boolean limitExplosionBlocks = true;            private int maxBlocksPerExplosion = 100;                private boolean reduceExplosionRadius = true;           private double explosionRadiusReduction = 0.3;          private boolean reduceExplosionFire = true;             private double fireSpreadReduction = 0.8;               
    
    public void loadFromConfig(ConfigurationSection section) {
                enabled = section.getBoolean("enabled", enabled);
        limitAllPhysics = section.getBoolean("limitAllPhysics", limitAllPhysics);
        physicsEventThreshold = section.getInt("physicsEventThreshold", physicsEventThreshold);
        eventThresholdCancelChance = section.getDouble("eventThresholdCancelChance", eventThresholdCancelChance);
        
                chunkCooldownTicks = section.getInt("chunkCooldownTicks", chunkCooldownTicks);
        chunkCooldownCancelChance = section.getDouble("chunkCooldownCancelChance", chunkCooldownCancelChance);
        maxPhysicsPerChunk = section.getInt("maxPhysicsPerChunk", maxPhysicsPerChunk);
        
                enablePhysicsRateLimit = section.getBoolean("enablePhysicsRateLimit", enablePhysicsRateLimit);
        physicsProcessingInterval = section.getInt("physicsProcessingInterval", physicsProcessingInterval);
        maxQueuedPhysicsUpdates = section.getInt("maxQueuedPhysicsUpdates", maxQueuedPhysicsUpdates);
        
                limitFallingBlocks = section.getBoolean("limitFallingBlocks", limitFallingBlocks);
        maxFallingBlocksPerWorld = section.getInt("maxFallingBlocksPerWorld", maxFallingBlocksPerWorld);
        fallingBlockLimitationChance = section.getDouble("fallingBlockLimitationChance", fallingBlockLimitationChance);
        mergeFallingBlocks = section.getBoolean("mergeFallingBlocks", mergeFallingBlocks);
        fallingBlocksMergeThreshold = section.getInt("fallingBlocksMergeThreshold", fallingBlocksMergeThreshold);
        fallingBlocksMergeChance = section.getDouble("fallingBlocksMergeChance", fallingBlocksMergeChance);
        
                limitFluidFlow = section.getBoolean("limitFluidFlow", limitFluidFlow);
        maxFluidUpdatesPerSecond = section.getInt("maxFluidUpdatesPerSecond", maxFluidUpdatesPerSecond);
        fluidLimitationChance = section.getDouble("fluidLimitationChance", fluidLimitationChance);
        lavaFlowReduceChance = section.getDouble("lavaFlowReduceChance", lavaFlowReduceChance);
        
                optimizeExplosions = section.getBoolean("optimizeExplosions", optimizeExplosions);
        limitExplosionBlocks = section.getBoolean("limitExplosionBlocks", limitExplosionBlocks);
        maxBlocksPerExplosion = section.getInt("maxBlocksPerExplosion", maxBlocksPerExplosion);
        reduceExplosionRadius = section.getBoolean("reduceExplosionRadius", reduceExplosionRadius);
        explosionRadiusReduction = section.getDouble("explosionRadiusReduction", explosionRadiusReduction);
        reduceExplosionFire = section.getBoolean("reduceExplosionFire", reduceExplosionFire);
        fireSpreadReduction = section.getDouble("fireSpreadReduction", fireSpreadReduction);
    }
    
    
    public void saveToConfig(ConfigurationSection section) {
                section.set("enabled", enabled);
        section.set("limitAllPhysics", limitAllPhysics);
        section.set("physicsEventThreshold", physicsEventThreshold);
        section.set("eventThresholdCancelChance", eventThresholdCancelChance);
        
                section.set("chunkCooldownTicks", chunkCooldownTicks);
        section.set("chunkCooldownCancelChance", chunkCooldownCancelChance);
        section.set("maxPhysicsPerChunk", maxPhysicsPerChunk);
        
                section.set("enablePhysicsRateLimit", enablePhysicsRateLimit);
        section.set("physicsProcessingInterval", physicsProcessingInterval);
        section.set("maxQueuedPhysicsUpdates", maxQueuedPhysicsUpdates);
        
                section.set("limitFallingBlocks", limitFallingBlocks);
        section.set("maxFallingBlocksPerWorld", maxFallingBlocksPerWorld);
        section.set("fallingBlockLimitationChance", fallingBlockLimitationChance);
        section.set("mergeFallingBlocks", mergeFallingBlocks);
        section.set("fallingBlocksMergeThreshold", fallingBlocksMergeThreshold);
        section.set("fallingBlocksMergeChance", fallingBlocksMergeChance);
        
                section.set("limitFluidFlow", limitFluidFlow);
        section.set("maxFluidUpdatesPerSecond", maxFluidUpdatesPerSecond);
        section.set("fluidLimitationChance", fluidLimitationChance);
        section.set("lavaFlowReduceChance", lavaFlowReduceChance);
        
                section.set("optimizeExplosions", optimizeExplosions);
        section.set("limitExplosionBlocks", limitExplosionBlocks);
        section.set("maxBlocksPerExplosion", maxBlocksPerExplosion);
        section.set("reduceExplosionRadius", reduceExplosionRadius);
        section.set("explosionRadiusReduction", explosionRadiusReduction);
        section.set("reduceExplosionFire", reduceExplosionFire);
        section.set("fireSpreadReduction", fireSpreadReduction);
    }
    
        
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isLimitAllPhysics() {
        return limitAllPhysics;
    }
    
    public void setLimitAllPhysics(boolean limitAllPhysics) {
        this.limitAllPhysics = limitAllPhysics;
    }
    
    public int getPhysicsEventThreshold() {
        return physicsEventThreshold;
    }
    
    public void setPhysicsEventThreshold(int physicsEventThreshold) {
        this.physicsEventThreshold = physicsEventThreshold;
    }
    
    public double getEventThresholdCancelChance() {
        return eventThresholdCancelChance;
    }
    
    public void setEventThresholdCancelChance(double eventThresholdCancelChance) {
        this.eventThresholdCancelChance = eventThresholdCancelChance;
    }
    
    public int getChunkCooldownTicks() {
        return chunkCooldownTicks;
    }
    
    public void setChunkCooldownTicks(int chunkCooldownTicks) {
        this.chunkCooldownTicks = chunkCooldownTicks;
    }
    
    public double getChunkCooldownCancelChance() {
        return chunkCooldownCancelChance;
    }
    
    public void setChunkCooldownCancelChance(double chunkCooldownCancelChance) {
        this.chunkCooldownCancelChance = chunkCooldownCancelChance;
    }
    
    public int getMaxPhysicsPerChunk() {
        return maxPhysicsPerChunk;
    }
    
    public void setMaxPhysicsPerChunk(int maxPhysicsPerChunk) {
        this.maxPhysicsPerChunk = maxPhysicsPerChunk;
    }
    
    public boolean isEnablePhysicsRateLimit() {
        return enablePhysicsRateLimit;
    }
    
    public void setEnablePhysicsRateLimit(boolean enablePhysicsRateLimit) {
        this.enablePhysicsRateLimit = enablePhysicsRateLimit;
    }
    
    public int getPhysicsProcessingInterval() {
        return physicsProcessingInterval;
    }
    
    public void setPhysicsProcessingInterval(int physicsProcessingInterval) {
        this.physicsProcessingInterval = physicsProcessingInterval;
    }
    
    public int getMaxQueuedPhysicsUpdates() {
        return maxQueuedPhysicsUpdates;
    }
    
    public void setMaxQueuedPhysicsUpdates(int maxQueuedPhysicsUpdates) {
        this.maxQueuedPhysicsUpdates = maxQueuedPhysicsUpdates;
    }
    
    public boolean isLimitFallingBlocks() {
        return limitFallingBlocks;
    }
    
    public void setLimitFallingBlocks(boolean limitFallingBlocks) {
        this.limitFallingBlocks = limitFallingBlocks;
    }
    
    public int getMaxFallingBlocksPerWorld() {
        return maxFallingBlocksPerWorld;
    }
    
    public void setMaxFallingBlocksPerWorld(int maxFallingBlocksPerWorld) {
        this.maxFallingBlocksPerWorld = maxFallingBlocksPerWorld;
    }
    
    public double getFallingBlockLimitationChance() {
        return fallingBlockLimitationChance;
    }
    
    public void setFallingBlockLimitationChance(double fallingBlockLimitationChance) {
        this.fallingBlockLimitationChance = fallingBlockLimitationChance;
    }
    
    public boolean isMergeFallingBlocks() {
        return mergeFallingBlocks;
    }
    
    public void setMergeFallingBlocks(boolean mergeFallingBlocks) {
        this.mergeFallingBlocks = mergeFallingBlocks;
    }
    
    public int getFallingBlocksMergeThreshold() {
        return fallingBlocksMergeThreshold;
    }
    
    public void setFallingBlocksMergeThreshold(int fallingBlocksMergeThreshold) {
        this.fallingBlocksMergeThreshold = fallingBlocksMergeThreshold;
    }
    
    public double getFallingBlocksMergeChance() {
        return fallingBlocksMergeChance;
    }
    
    public void setFallingBlocksMergeChance(double fallingBlocksMergeChance) {
        this.fallingBlocksMergeChance = fallingBlocksMergeChance;
    }
    
    public boolean isLimitFluidFlow() {
        return limitFluidFlow;
    }
    
    public void setLimitFluidFlow(boolean limitFluidFlow) {
        this.limitFluidFlow = limitFluidFlow;
    }
    
    public int getMaxFluidUpdatesPerSecond() {
        return maxFluidUpdatesPerSecond;
    }
    
    public void setMaxFluidUpdatesPerSecond(int maxFluidUpdatesPerSecond) {
        this.maxFluidUpdatesPerSecond = maxFluidUpdatesPerSecond;
    }
    
    public double getFluidLimitationChance() {
        return fluidLimitationChance;
    }
    
    public void setFluidLimitationChance(double fluidLimitationChance) {
        this.fluidLimitationChance = fluidLimitationChance;
    }
    
    public double getLavaFlowReduceChance() {
        return lavaFlowReduceChance;
    }
    
    public void setLavaFlowReduceChance(double lavaFlowReduceChance) {
        this.lavaFlowReduceChance = lavaFlowReduceChance;
    }
    
    public boolean isOptimizeExplosions() {
        return optimizeExplosions;
    }
    
    public void setOptimizeExplosions(boolean optimizeExplosions) {
        this.optimizeExplosions = optimizeExplosions;
    }
    
    public boolean isLimitExplosionBlocks() {
        return limitExplosionBlocks;
    }
    
    public void setLimitExplosionBlocks(boolean limitExplosionBlocks) {
        this.limitExplosionBlocks = limitExplosionBlocks;
    }
    
    public int getMaxBlocksPerExplosion() {
        return maxBlocksPerExplosion;
    }
    
    public void setMaxBlocksPerExplosion(int maxBlocksPerExplosion) {
        this.maxBlocksPerExplosion = maxBlocksPerExplosion;
    }
    
    public boolean isReduceExplosionRadius() {
        return reduceExplosionRadius;
    }
    
    public void setReduceExplosionRadius(boolean reduceExplosionRadius) {
        this.reduceExplosionRadius = reduceExplosionRadius;
    }
    
    public double getExplosionRadiusReduction() {
        return explosionRadiusReduction;
    }
    
    public void setExplosionRadiusReduction(double explosionRadiusReduction) {
        this.explosionRadiusReduction = explosionRadiusReduction;
    }
    
    public boolean isReduceExplosionFire() {
        return reduceExplosionFire;
    }
    
    public void setReduceExplosionFire(boolean reduceExplosionFire) {
        this.reduceExplosionFire = reduceExplosionFire;
    }
    
    public double getFireSpreadReduction() {
        return fireSpreadReduction;
    }
    
    public void setFireSpreadReduction(double fireSpreadReduction) {
        this.fireSpreadReduction = fireSpreadReduction;
    }
} 