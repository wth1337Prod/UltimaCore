package me.wth.ultimaCore.gui;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public abstract class AbstractGUI implements Listener {
    protected final UltimaCore plugin;
    protected final String title;
    protected final int size;
    
        private static final Map<UUID, AbstractGUI> openInventories = new HashMap<>();
    
    
    public AbstractGUI(UltimaCore plugin, String title, int size) {
        this.plugin = plugin;
        this.title = ChatColor.translateAlternateColorCodes('&', title);
        this.size = size;
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    
    public abstract Inventory createInventory(Player player);
    
    
    public abstract void handleClick(Player player, int slot, ItemStack clickedItem, InventoryClickEvent event);
    
    
    public void handleClose(Player player, InventoryCloseEvent event) {
            }
    
    
    public void open(Player player) {
        Inventory inventory = createInventory(player);
        player.openInventory(inventory);
        openInventories.put(player.getUniqueId(), this);
    }
    
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        AbstractGUI openGUI = openInventories.get(player.getUniqueId());
        
        if (openGUI != null && event.getView().getTitle().equals(this.title)) {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            
            if (slot >= 0 && slot < size && event.getView().getTopInventory().equals(event.getClickedInventory())) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null) {
                    handleClick(player, slot, clickedItem, event);
                }
            }
        }
    }
    
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        AbstractGUI openGUI = openInventories.get(player.getUniqueId());
        
        if (openGUI != null && event.getView().getTitle().equals(this.title)) {
            handleClose(player, event);
            openInventories.remove(player.getUniqueId());
        }
    }
    
    
    public void update(Player player) {
        if (player.getOpenInventory().getTitle().equals(this.title)) {
            Inventory inventory = createInventory(player);
            player.getOpenInventory().getTopInventory().setContents(inventory.getContents());
        }
    }
    
    
    public static AbstractGUI getOpenGUI(Player player) {
        return openInventories.get(player.getUniqueId());
    }
    
    
    protected void fillBorders(Inventory inventory, int colorCode) {
        ItemStack glass = InventoryUtils.createGlassPane(colorCode, " ");
        
                for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
            inventory.setItem(size - 9 + i, glass);
        }
        
                for (int i = 9; i < size - 9; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }
} 