package me.wth.ultimaCore.modules.memoryguard;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MemoryGuardModule extends AbstractModule {
    private final UltimaCore plugin;
    private MemoryGuardSettings settings;
    private final Runtime runtime = Runtime.getRuntime();
    private final DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    
    private BukkitTask monitorTask;
    private BukkitTask warningTask;
    private BukkitTask cleanupTask;
    
    private long lastGcTime = System.currentTimeMillis();
    private long lastWarningTime = 0;
    private int warningLevel = 0;
    private boolean emergencyMode = false;
    
    private final List<MemoryAction> scheduledActions = new ArrayList<>();
    
    
    public MemoryGuardModule() {
        this.plugin = UltimaCore.getInstance();
    }

    @Override
    public void onEnable() {
                this.settings = new MemoryGuardSettings();
        loadConfig();
        
                startTasks();
        
                MemoryGuardCommand commandHandler = new MemoryGuardCommand(this);
        plugin.getCommand("memoryguard").setExecutor(commandHandler);
        plugin.getCommand("memoryguard").setTabCompleter(commandHandler);
        
        plugin.getLogger().info("Модуль MemoryGuard успешно включен!");
    }

    @Override
    public void onDisable() {
                if (monitorTask != null) {
            monitorTask.cancel();
            monitorTask = null;
        }
        
        if (warningTask != null) {
            warningTask.cancel();
            warningTask = null;
        }
        
        if (cleanupTask != null) {
            cleanupTask.cancel();
            cleanupTask = null;
        }
        
        plugin.getLogger().info("Модуль MemoryGuard выключен.");
    }
    
    
    public void loadConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("memoryguard");
        
        if (section == null) {
            section = config.createSection("memoryguard");
        }
        
        settings.loadFromConfig(section);
        config.save();
    }
    
    
    public void saveConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("memoryguard");
        
        if (section == null) {
            section = config.createSection("memoryguard");
        }
        
        settings.saveToConfig(section);
        config.save();
    }
    
    
    private void startTasks() {
                monitorTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkMemory, 20L, 20L * settings.getCheckInterval());
        
                warningTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processWarnings, 20L, 20L * 5);
        
                cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processScheduledActions, 20L, 20L);
    }
    
    
    private void checkMemory() {
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        
        long usedMemory = allocatedMemory - freeMemory;
        double usedPercentage = (double) usedMemory / maxMemory * 100;
        
                if (usedPercentage >= settings.getCriticalThreshold()) {
                        handleCriticalMemory(usedPercentage);
        } else if (usedPercentage >= settings.getWarningThreshold()) {
                        handleWarningMemory(usedPercentage);
        } else if (usedPercentage >= settings.getNoticeThreshold()) {
                        handleNoticeMemory(usedPercentage);
        } else {
                        handleNormalMemory();
        }
        
                checkGcFrequency();
    }
    
    
    private void handleCriticalMemory(double usedPercentage) {
        if (!emergencyMode) {
            emergencyMode = true;
            plugin.getLogger().warning("MemoryGuard: КРИТИЧЕСКАЯ ситуация с памятью! Используется " 
                    + decimalFormat.format(usedPercentage) + "% памяти.");
            
                        scheduleAction(MemoryAction.ActionType.EMERGENCY_GC);
            scheduleAction(MemoryAction.ActionType.CLEAR_ENTITIES);
            scheduleAction(MemoryAction.ActionType.UNLOAD_CHUNKS);
            
            if (settings.isEnableKickOnCritical()) {
                scheduleAction(MemoryAction.ActionType.KICK_PLAYERS);
            }
            
                        warningLevel = 3;
            lastWarningTime = System.currentTimeMillis();
        }
    }
    
    
    private void handleWarningMemory(double usedPercentage) {
        if (warningLevel < 2 && (System.currentTimeMillis() - lastWarningTime) > 60000) {
            plugin.getLogger().warning("MemoryGuard: ВНИМАНИЕ! Высокое потребление памяти: " 
                    + decimalFormat.format(usedPercentage) + "%");
            
                        scheduleAction(MemoryAction.ActionType.FULL_GC);
            scheduleAction(MemoryAction.ActionType.CLEAR_ITEMS);
            
                        warningLevel = 2;
            lastWarningTime = System.currentTimeMillis();
        }
    }
    
    
    private void handleNoticeMemory(double usedPercentage) {
        if (warningLevel < 1 && (System.currentTimeMillis() - lastWarningTime) > 300000) {
            plugin.getLogger().info("MemoryGuard: Повышенное потребление памяти: " 
                    + decimalFormat.format(usedPercentage) + "%");
            
                        scheduleAction(MemoryAction.ActionType.SUGGEST_GC);
            
                        warningLevel = 1;
            lastWarningTime = System.currentTimeMillis();
        }
    }
    
    
    private void handleNormalMemory() {
        if (emergencyMode) {
            plugin.getLogger().info("MemoryGuard: Ситуация с памятью нормализовалась.");
            emergencyMode = false;
        }
        
        if (warningLevel > 0 && (System.currentTimeMillis() - lastWarningTime) > 600000) {
            warningLevel = 0;
        }
    }
    
    
    private void checkGcFrequency() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long totalGcCount = 0;
        long totalGcTime = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            long count = gcBean.getCollectionCount();
            if (count > 0) {
                totalGcCount += count;
                totalGcTime += gcBean.getCollectionTime();
            }
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastCheck = currentTime - lastGcTime;
        
                if (timeSinceLastCheck < 10000 && totalGcCount > 5) {
            plugin.getLogger().warning("MemoryGuard: Обнаружена высокая частота сборок мусора. Возможна утечка памяти!");
            
            if (settings.isEnableDetailedLogging()) {
                plugin.getLogger().info("MemoryGuard: Количество сборок: " + totalGcCount + 
                        ", общее время: " + totalGcTime + "мс, за период: " + timeSinceLastCheck + "мс");
            }
        }
        
        lastGcTime = currentTime;
    }
    
    
    private void processWarnings() {
        if (warningLevel == 0 || !settings.isEnablePlayerWarnings()) {
            return;
        }
        
        String message;
        ChatColor color;
        
        switch (warningLevel) {
            case 3:
                message = "§c[MemoryGuard] §4ВНИМАНИЕ! Критическая нагрузка на сервер! Возможны лаги и перезагрузка.";
                color = ChatColor.RED;
                break;
            case 2:
                message = "§c[MemoryGuard] §eВнимание! Высокая нагрузка на сервер. Рекомендуется снизить активность.";
                color = ChatColor.YELLOW;
                break;
            default:
                message = "§c[MemoryGuard] §7Наблюдается повышенная нагрузка на сервер.";
                color = ChatColor.GRAY;
                break;
        }
        
                for (Player player : Bukkit.getOnlinePlayers()) {
            if (warningLevel >= 3 || player.hasPermission("ultimacore.memoryguard.warnings")) {
                player.sendMessage(message);
            }
        }
    }
    
    
    private void processScheduledActions() {
        if (scheduledActions.isEmpty()) {
            return;
        }
        
        List<MemoryAction> actionsToRemove = new ArrayList<>();
        
        for (MemoryAction action : scheduledActions) {
            if (System.currentTimeMillis() >= action.getExecutionTime()) {
                executeAction(action);
                actionsToRemove.add(action);
            }
        }
        
        scheduledActions.removeAll(actionsToRemove);
    }
    
    
    private void scheduleAction(MemoryAction.ActionType actionType) {
                for (MemoryAction action : scheduledActions) {
            if (action.getType() == actionType) {
                return;
            }
        }
        
                long delay = 0;
        switch (actionType) {
            case EMERGENCY_GC:
            case FULL_GC:
                delay = 0;                 break;
            case SUGGEST_GC:
                delay = 5000;                 break;
            case CLEAR_ITEMS:
                delay = 2000;                 break;
            case CLEAR_ENTITIES:
                delay = 1000;                 break;
            case UNLOAD_CHUNKS:
                delay = 3000;                 break;
            case KICK_PLAYERS:
                delay = 10000;                 break;
        }
        
                MemoryAction action = new MemoryAction(actionType, System.currentTimeMillis() + delay);
        scheduledActions.add(action);
    }
    
    
    private void executeAction(MemoryAction action) {
        switch (action.getType()) {
            case EMERGENCY_GC:
                performEmergencyGc();
                break;
            case FULL_GC:
                performFullGc();
                break;
            case SUGGEST_GC:
                performSuggestGc();
                break;
            case CLEAR_ITEMS:
                clearItems();
                break;
            case CLEAR_ENTITIES:
                clearEntities();
                break;
            case UNLOAD_CHUNKS:
                unloadChunks();
                break;
            case KICK_PLAYERS:
                kickPlayers();
                break;
        }
    }
    
    
    private void performEmergencyGc() {
        plugin.getLogger().warning("MemoryGuard: Выполняется экстренная очистка памяти!");
        
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            System.gc();
            System.gc();
            
                        long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usedPercentage = (double) usedMemory / maxMemory * 100;
            
            plugin.getLogger().info("MemoryGuard: После экстренной очистки: " + 
                    decimalFormat.format(usedPercentage) + "% памяти используется.");
        });
    }
    
    
    private void performFullGc() {
        if (settings.isEnableDetailedLogging()) {
            plugin.getLogger().info("MemoryGuard: Выполняется полная сборка мусора...");
        }
        
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            System.gc();
        });
    }
    
    
    private void performSuggestGc() {
        if (settings.isEnableDetailedLogging()) {
            plugin.getLogger().info("MemoryGuard: Рекомендуется сборка мусора...");
        }
        
                System.gc();
    }
    
    
    private void clearItems() {
        plugin.getLogger().info("MemoryGuard: Очистка предметов на земле...");
        
                Bukkit.getScheduler().runTask(plugin, () -> {
            int removed = 0;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                if (settings.isItemsRemovalEnabled(world.getName())) {
                    removed += world.getEntitiesByClass(org.bukkit.entity.Item.class).stream()
                            .filter(item -> !item.hasMetadata("protected"))
                            .peek(org.bukkit.entity.Entity::remove)
                            .count();
                }
            }
            
            if (settings.isEnableDetailedLogging() || removed > 0) {
                plugin.getLogger().info("MemoryGuard: Удалено " + removed + " предметов для освобождения памяти.");
            }
            
                        if (removed > 0 && settings.isEnablePlayerWarnings()) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "[MemoryGuard] Удалено " + removed + " предметов для оптимизации.");
            }
        });
    }
    
    
    private void clearEntities() {
        plugin.getLogger().info("MemoryGuard: Очистка сущностей...");
        
                Bukkit.getScheduler().runTask(plugin, () -> {
            int removed = 0;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                if (settings.isEntityRemovalEnabled(world.getName())) {
                    for (org.bukkit.entity.Entity entity : world.getEntities()) {
                                                if (entity instanceof Player || entity.hasMetadata("protected")) {
                            continue;
                        }
                        
                                                if (settings.isEntityTypeRemovable(entity.getType())) {
                            entity.remove();
                            removed++;
                        }
                    }
                }
            }
            
            if (settings.isEnableDetailedLogging() || removed > 0) {
                plugin.getLogger().info("MemoryGuard: Удалено " + removed + " сущностей для освобождения памяти.");
            }
            
                        if (removed > 0 && settings.isEnablePlayerWarnings()) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "[MemoryGuard] Удалено " + removed + " сущностей для оптимизации.");
            }
        });
    }
    
    
    private void unloadChunks() {
        plugin.getLogger().info("MemoryGuard: Выгрузка неиспользуемых чанков...");
        
                Bukkit.getScheduler().runTask(plugin, () -> {
            int unloaded = 0;
            
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                if (settings.isChunkUnloadEnabled(world.getName())) {
                                        List<org.bukkit.Chunk> playerChunks = new ArrayList<>();
                    for (Player player : world.getPlayers()) {
                        org.bukkit.Chunk chunk = player.getLocation().getChunk();
                        playerChunks.add(chunk);
                        
                                                int viewDistance = Bukkit.getViewDistance();
                        for (int x = -viewDistance; x <= viewDistance; x++) {
                            for (int z = -viewDistance; z <= viewDistance; z++) {
                                playerChunks.add(world.getChunkAt(chunk.getX() + x, chunk.getZ() + z));
                            }
                        }
                    }
                    
                                        for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        if (!playerChunks.contains(chunk)) {
                                                        chunk.unload(true);
                            unloaded++;
                        }
                    }
                }
            }
            
            if (settings.isEnableDetailedLogging() || unloaded > 0) {
                plugin.getLogger().info("MemoryGuard: Выгружено " + unloaded + " чанков для освобождения памяти.");
            }
        });
    }
    
    
    private void kickPlayers() {
        plugin.getLogger().warning("MemoryGuard: Выполняется отключение игроков из-за критической нехватки памяти...");
        
                Bukkit.getScheduler().runTask(plugin, () -> {
            int kickedCount = 0;
            String kickMessage = ChatColor.RED + "Сервер испытывает критическую нехватку памяти. " +
                    "Пожалуйста, подождите несколько минут перед повторным подключением.";
            
                        List<Player> kickCandidates = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("ultimacore.memoryguard.nokick")) {
                    kickCandidates.add(player);
                }
            }
            
                        kickCandidates.sort((p1, p2) -> Long.compare(p2.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE),
                    p1.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE)));
            
                        int kickLimit = Math.min(kickCandidates.size(), settings.getKickPlayersLimit());
            for (int i = 0; i < kickLimit; i++) {
                kickCandidates.get(i).kickPlayer(kickMessage);
                kickedCount++;
            }
            
            plugin.getLogger().warning("MemoryGuard: Отключено " + kickedCount + " игроков для предотвращения краша сервера.");
            
                        if (kickedCount > 0) {
                Bukkit.broadcastMessage(ChatColor.RED + "[MemoryGuard] Некоторые игроки были отключены из-за критической нехватки памяти на сервере.");
            }
        });
    }
    
    
    public MemoryGuardSettings getSettings() {
        return settings;
    }
    
    
    @Override
    public void onTick() {
            }
    
    @Override
    public String getName() {
        return "MemoryGuard";
    }
    
    @Override
    public String getDescription() {
        return "Модуль для мониторинга и управления памятью сервера";
    }
} 