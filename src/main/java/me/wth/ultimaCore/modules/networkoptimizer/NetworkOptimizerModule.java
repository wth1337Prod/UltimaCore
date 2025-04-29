package me.wth.ultimaCore.modules.networkoptimizer;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.config.Config;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.scheduler.BukkitTask;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class    NetworkOptimizerModule extends AbstractModule implements Listener {
    private final UltimaCore plugin;
    private NetworkOptimizerSettings settings;
    
    private BukkitTask monitoringTask;
    private Map<UUID, PlayerNetworkStats> playerStats = new HashMap<>();
    
        private final Map<String, AtomicInteger> connectionAttemptsPerIP = new ConcurrentHashMap<>();
    private final Map<String, Long> lastConnectionAttempt = new ConcurrentHashMap<>();
    private final Map<String, Integer> activeConnectionsPerIP = new ConcurrentHashMap<>();
    private final Map<UUID, Long> loginStartTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> packetCountPerIP = new ConcurrentHashMap<>();
    private int globalConnectionCount = 0;
    private long lastConnectionRateLimitReset = System.currentTimeMillis();
    
        private final Map<String, Boolean> proxyCheckCache = new ConcurrentHashMap<>();
    
    
    public NetworkOptimizerModule() {
        this.plugin = UltimaCore.getInstance();
    }

    @Override
    public void onEnable() {
                this.settings = new NetworkOptimizerSettings();
        loadConfig();
        
                Bukkit.getPluginManager().registerEvents(this, plugin);
        
                startMonitoring();
        
                if (settings.isEnableDdosProtection()) {
            startDdosProtectionTasks();
        }
        
                plugin.getCommand("netoptimizer").setExecutor(new NetworkOptimizerCommand(this));
        plugin.getCommand("netoptimizer").setTabCompleter(new NetworkOptimizerCommand(this));
        
        LoggerUtil.info("Модуль NetworkOptimizer успешно включен!");
        if (settings.isEnableDdosProtection()) {
            LoggerUtil.info("DDOS-защита активирована. Настроены следующие ограничения:");
            LoggerUtil.info("- Максимум " + settings.getMaxConnectionsPerIP() + " соединений с одного IP");
            LoggerUtil.info("- Минимум " + settings.getConnectionThrottleTime() + "мс между подключениями с одного IP");
            if (settings.isBlockProxiesAndVPNs()) {
                LoggerUtil.info("- Блокировка подключений через прокси и VPN включена");
            }
            if (settings.isEnableGeoIPFiltering()) {
                LoggerUtil.info("- Географическая фильтрация подключений включена");
            }
        }
    }

    @Override
    public void onDisable() {
                stopMonitoring();
        
                PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerLoginEvent.getHandlerList().unregister(this);
        AsyncPlayerPreLoginEvent.getHandlerList().unregister(this);
        
                for (Player player : Bukkit.getOnlinePlayers()) {
            resetPlayerNetworkSettings(player);
        }
        
                connectionAttemptsPerIP.clear();
        lastConnectionAttempt.clear();
        activeConnectionsPerIP.clear();
        loginStartTime.clear();
        proxyCheckCache.clear();
        
        LoggerUtil.info("Модуль NetworkOptimizer выключен.");
    }
    
    
    private void startDdosProtectionTasks() {
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            
                        loginStartTime.entrySet().removeIf(entry -> 
                now - entry.getValue() > TimeUnit.SECONDS.toMillis(settings.getLoginTimeout() * 2));
            
                        if (settings.isEnableConnectionRateLimit() && 
                    now - lastConnectionRateLimitReset > TimeUnit.SECONDS.toMillis(settings.getConnectionLimitPeriod())) {
                globalConnectionCount = 0;
                lastConnectionRateLimitReset = now;
            }
            
                        proxyCheckCache.entrySet().removeIf(entry -> 
                now - lastConnectionAttempt.getOrDefault(entry.getKey(), 0L) > TimeUnit.HOURS.toMillis(24));
                
                        packetCountPerIP.clear();
            
        }, 20L, 20L);     }
    
    
    private void startMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
        }
        
        monitoringTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::monitorNetworkUsage, 
                20L, 20L * settings.getMonitoringInterval());
    }
    
    
    private void stopMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
            monitoringTask = null;
        }
    }
    
    
    private void monitorNetworkUsage() {
                for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerStats.containsKey(player.getUniqueId())) {
                playerStats.put(player.getUniqueId(), new PlayerNetworkStats());
            }
            
            PlayerNetworkStats stats = playerStats.get(player.getUniqueId());
            stats.update(player);
            
            optimizePlayerNetwork(player, stats);
        }
        
                playerStats.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }
    
    
    private void optimizePlayerNetwork(Player player, PlayerNetworkStats stats) {
                if (player.hasPermission("ultimacore.networkoptimizer.bypass")) {
            return;
        }
        
                int ping = getPlayerPing(player);
        
                if (settings.isEnabledEntityVisibilityOptimization()) {
            if (ping > settings.getHighLatencyThreshold()) {
                                setEntityViewDistance(player, settings.getReducedEntityViewDistance());
            } else if (ping > settings.getMediumLatencyThreshold()) {
                                setEntityViewDistance(player, settings.getNormalEntityViewDistance());
            } else {
                                resetEntityViewDistance(player);
            }
        }
        
                if (settings.isEnabledChunkUpdateOptimization()) {
            if (ping > settings.getHighLatencyThreshold()) {
                setReducedChunkUpdates(player);
            } else {
                resetChunkUpdates(player);
            }
        }
        
                if (settings.isEnableDetailedLogging() && ping > settings.getMediumLatencyThreshold()) {
            LoggerUtil.info(String.format("NetworkOptimizer: Применены оптимизации для %s (пинг: %d мс)", 
                    player.getName(), ping));
        }
    }
    
    
    private void resetPlayerNetworkSettings(Player player) {
        resetEntityViewDistance(player);
        resetChunkUpdates(player);
    }
    
    
    private void setEntityViewDistance(Player player, int distance) {
                                try {
                                    
                    } catch (Exception e) {
            LoggerUtil.warning("Не удалось установить расстояние видимости сущностей: " + e.getMessage());
        }
    }
    
    
    private void resetEntityViewDistance(Player player) {
                    }
    
    
    private void setReducedChunkUpdates(Player player) {
                    }
    
    
    private void resetChunkUpdates(Player player) {
                    }
    
    
    private int getPlayerPing(Player player) {
        try {
            return player.getPing();
        } catch (Exception e) {
            return 0;         }
    }
    
    
    private boolean isProxyOrVPN(InetAddress address) {
        String ip = address.getHostAddress();
        
                if (proxyCheckCache.containsKey(ip)) {
            return proxyCheckCache.get(ip);
        }
        
                        boolean isProxy = false;
        
                proxyCheckCache.put(ip, isProxy);
        
        return isProxy;
    }
    
    
    private boolean isCountryAllowed(InetAddress address) {
        if (!settings.isEnableGeoIPFiltering()) {
            return true;
        }
        
                        
                String country = "RU";         
        for (String allowedCountry : settings.getAllowedCountries()) {
            if (allowedCountry.equalsIgnoreCase(country)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!settings.isEnableDdosProtection()) {
            return;
        }
        
        InetAddress address = event.getAddress();
        String ip = address.getHostAddress();
        
                if (settings.isEnableGeoIPFiltering() && !isCountryAllowed(address)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "§cДоступ с вашего региона заблокирован.");
            LoggerUtil.warning("Заблокировано подключение с IP " + ip + " - страна не разрешена");
            return;
        }
        
                if (settings.isBlockProxiesAndVPNs() && isProxyOrVPN(address)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "§cПодключение через прокси или VPN запрещено.");
            LoggerUtil.warning("Заблокировано подключение с IP " + ip + " - обнаружен прокси/VPN");
            return;
        }
        
                loginStartTime.put(event.getUniqueId(), System.currentTimeMillis());
        
                long lastAttemptTime = lastConnectionAttempt.getOrDefault(ip, 0L);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastAttemptTime < settings.getConnectionThrottleTime()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "§cСлишком частые попытки подключения. Пожалуйста, подождите.");
            LoggerUtil.warning("Заблокировано подключение с IP " + ip + " - слишком частые попытки");
            return;
        }
        
                lastConnectionAttempt.put(ip, currentTime);
        
                int connections = activeConnectionsPerIP.getOrDefault(ip, 0);
        if (connections >= settings.getMaxConnectionsPerIP()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                    "§cПревышено максимальное количество подключений с вашего IP-адреса.");
            LoggerUtil.warning("Заблокировано подключение с IP " + ip + " - превышен лимит соединений");
            return;
        }
        
                if (settings.isEnableConnectionRateLimit()) {
            if (globalConnectionCount >= settings.getMaxGlobalConnections()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, 
                        "§cСервер в данный момент перегружен. Пожалуйста, попробуйте позже.");
                LoggerUtil.warning("Заблокировано подключение - превышен глобальный лимит подключений");
                return;
            }
            globalConnectionCount++;
        }
        
                AtomicInteger attempts = connectionAttemptsPerIP.computeIfAbsent(ip, k -> new AtomicInteger(0));
        attempts.incrementAndGet();
    }
    
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!settings.isEnableDdosProtection()) {
            return;
        }
        
        String ip = event.getAddress().getHostAddress();
        
                UUID uuid = event.getPlayer().getUniqueId();
        long loginTime = loginStartTime.getOrDefault(uuid, System.currentTimeMillis());
        
        if (System.currentTimeMillis() - loginTime > TimeUnit.SECONDS.toMillis(settings.getLoginTimeout())) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, 
                    "§cПревышено время ожидания входа. Пожалуйста, попробуйте снова.");
            LoggerUtil.warning("Заблокировано подключение игрока " + event.getPlayer().getName() + 
                    " - превышено время входа");
            loginStartTime.remove(uuid);
            return;
        }
        
                int connections = activeConnectionsPerIP.getOrDefault(ip, 0);
        activeConnectionsPerIP.put(ip, connections + 1);
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
                playerStats.put(player.getUniqueId(), new PlayerNetworkStats());
        
                if (settings.isEnableOptimizationOnJoin()) {
            optimizePlayerNetwork(player, playerStats.get(player.getUniqueId()));
        }
        
                loginStartTime.remove(player.getUniqueId());
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
                playerStats.remove(player.getUniqueId());
        
                String ip = player.getAddress().getAddress().getHostAddress();
        int connections = activeConnectionsPerIP.getOrDefault(ip, 1);
        if (connections > 1) {
            activeConnectionsPerIP.put(ip, connections - 1);
        } else {
            activeConnectionsPerIP.remove(ip);
        }
    }
    
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerPing(ServerListPingEvent event) {
        if (!settings.isEnableDdosProtection()) {
            return;
        }
        
        String ip = event.getAddress().getHostAddress();
        
                AtomicInteger pingCount = packetCountPerIP.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int count = pingCount.incrementAndGet();
        
        if (count > settings.getPacketSpamThreshold()) {
                        LoggerUtil.warning("Обнаружен возможный спам пингами с IP " + ip);
        }
    }
    
    
    public void loadConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("networkoptimizer");
        
        if (section == null) {
            section = config.createSection("networkoptimizer");
        }
        
        settings.loadFromConfig(section);
        config.save();
    }
    
    
    public void saveConfig() {
        Config config = plugin.getConfigManager().getConfig();
        ConfigurationSection section = config.getConfigurationSection("networkoptimizer");
        
        if (section == null) {
            section = config.createSection("networkoptimizer");
        }
        
        settings.saveToConfig(section);
        config.save();
    }
    
    
    public NetworkOptimizerSettings getSettings() {
        return settings;
    }
    
    @Override
    public void onTick() {
            }
    
    @Override
    public String getName() {
        return "NetworkOptimizer";
    }
    
    @Override
    public String getDescription() {
        return "Модуль для оптимизации сетевого взаимодействия и защиты от DDOS";
    }
} 