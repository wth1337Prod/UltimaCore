package me.wth.ultimaCore.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.Set;


public class RedstoneUtil {
    
        private static final BlockFace[] DIRECTIONS = new BlockFace[] {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, 
            BlockFace.UP, BlockFace.DOWN
    };
    
        private static final BlockFace[] HORIZONTAL = new BlockFace[] {
            BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST
    };
    
    
    public static boolean isRedstoneComponent(Block block) {
        Material type = block.getType();
        return isRedstoneComponent(type);
    }
    
    
    public static boolean isRedstoneComponent(Material type) {
        switch (type) {
            case REDSTONE_WIRE:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REPEATER:
            case COMPARATOR:
            case OBSERVER:
            case LEVER:
            case STONE_BUTTON:
            case ACACIA_BUTTON:
            case BIRCH_BUTTON:
            case CRIMSON_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            case SPRUCE_BUTTON:
            case WARPED_BUTTON:
            case DISPENSER:
            case DROPPER:
            case HOPPER:
            case PISTON:
            case STICKY_PISTON:
            case REDSTONE_LAMP:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case DAYLIGHT_DETECTOR:
            case TARGET:
                return true;
            default:
                return false;
        }
    }
    
    
    public static boolean isPowered(Block block) {
        return block.isBlockPowered() || block.isBlockIndirectlyPowered();
    }
    
    
    public static Set<Block> getAdjacentRedstoneComponents(Block block) {
        Set<Block> components = new HashSet<>();
        
        for (BlockFace face : DIRECTIONS) {
            Block relative = block.getRelative(face);
            if (isRedstoneComponent(relative)) {
                components.add(relative);
            }
        }
        
        return components;
    }
    
    
    public static Set<Block> getHorizontalAdjacentRedstoneComponents(Block block) {
        Set<Block> components = new HashSet<>();
        
        for (BlockFace face : HORIZONTAL) {
            Block relative = block.getRelative(face);
            if (isRedstoneComponent(relative)) {
                components.add(relative);
            }
        }
        
        return components;
    }
    
    
    public static int getPower(Block block) {
        if (block.getType() == Material.REDSTONE_WIRE) {
            return block.getData();
        }
        
        return Math.max(block.getBlockPower(), block.getBlockPower(BlockFace.DOWN));
    }
    
    
    public static boolean isPotentialClockComponent(Block block) {
        Material type = block.getType();
        
        switch (type) {
            case REPEATER:
            case COMPARATOR:
            case OBSERVER:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
                return true;
            default:
                return false;
        }
    }
    
    
    public static int getMaxDelay(Block block) {
        Material type = block.getType();
        
        if (type == Material.REPEATER) {
            return 4 * 2;         } else if (type == Material.COMPARATOR) {
            return 1;         } else if (type == Material.OBSERVER) {
            return 2;         }
        
        return 0;
    }
    
    
    public static boolean isPartOfClock(Block block) {
                if (!isPotentialClockComponent(block)) {
            return false;
        }
        
                Set<Block> neighbors = getAdjacentRedstoneComponents(block);
        
                if (neighbors.size() < 2) {
            return false;
        }
        
                for (Block neighbor : neighbors) {
            if (isPotentialClockComponent(neighbor)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    public static boolean isConnectedToOutput(Block block) {
        for (BlockFace face : DIRECTIONS) {
            Block relative = block.getRelative(face);
            Material type = relative.getType();
            
                        if (type == Material.REDSTONE_LAMP || 
                type == Material.PISTON || 
                type == Material.STICKY_PISTON || 
                type == Material.DISPENSER || 
                type == Material.DROPPER || 
                type == Material.HOPPER) {
                return true;
            }
        }
        
        return false;
    }
} 