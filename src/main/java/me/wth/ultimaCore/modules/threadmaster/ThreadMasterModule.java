package me.wth.ultimaCore.modules.threadmaster;

import me.wth.ultimaCore.UltimaCore;
import me.wth.ultimaCore.api.AbstractModule;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.Comparator;


public class ThreadMasterModule extends AbstractModule implements Listener {
    private ThreadMasterSettings settings;
    
        private ThreadPoolExecutor taskExecutor;
    
        private PriorityBlockingQueue<Runnable> taskQueue;
    
        private BukkitTask monitoringTask;
    private BukkitTask balancingTask;
    private BukkitTask diagnosticsTask;
    
        private final AtomicInteger threadCounter = new AtomicInteger(0);
    
        private final Map<Long, TaskInfo> runningTasks = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerThreadStats> playerStats = new ConcurrentHashMap<>();
    
        private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
        private static class ThreadMasterThreadFactory implements ThreadFactory {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber;
        private final String namePrefix;
        
        ThreadMasterThreadFactory(String prefix, AtomicInteger counter) {
                        group = Thread.currentThread().getThreadGroup();
            threadNumber = counter;
            namePrefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
    
    
    private static class TaskInfo {
        private final Runnable task;
        private final long startTime;
        private final int priority;
        private final String source;
        
        public TaskInfo(Runnable task, int priority, String source) {
            this.task = task;
            this.startTime = System.currentTimeMillis();
            this.priority = priority;
            this.source = source;
        }
        
        public long getExecutionTime() {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    
    private static class PlayerThreadStats {
        private final UUID playerId;
        private int taskCount = 0;
        private long totalExecutionTime = 0;
        
        public PlayerThreadStats(UUID playerId) {
            this.playerId = playerId;
        }
        
        public void addTask(long executionTime) {
            taskCount++;
            totalExecutionTime += executionTime;
        }
        
        public int getTaskCount() {
            return taskCount;
        }
        
        public long getAverageExecutionTime() {
            return taskCount > 0 ? totalExecutionTime / taskCount : 0;
        }
    }
    
    
    private static class TaskPriorityComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            if (r1 instanceof PrioritizedTask && r2 instanceof PrioritizedTask) {
                return Integer.compare(
                        ((PrioritizedTask) r2).getPriority(),
                        ((PrioritizedTask) r1).getPriority()
                );
            }
                        return 0;
        }
    }
    
    
    public interface PrioritizedTask extends Runnable {
        int getPriority();
    }
    
    
    public class PrioritizedTaskWrapper implements PrioritizedTask {
        private final Runnable task;
        private final int priority;
        
        public PrioritizedTaskWrapper(Runnable task, int priority) {
            this.task = task;
            this.priority = priority;
        }
        
        @Override
        public void run() {
            long taskId = Thread.currentThread().getId();
            TaskInfo info = new TaskInfo(task, priority, getCallerInfo());
            runningTasks.put(taskId, info);
            
            try {
                task.run();
            } finally {
                runningTasks.remove(taskId);
                
                                if (settings.isLogLongRunningTasks() && info.getExecutionTime() > settings.getMaxTaskExecutionTime()) {
                    LoggerUtil.warning(String.format(
                            "Задача из %s выполнялась слишком долго: %d мс (приоритет: %d)",
                            info.source, info.getExecutionTime(), info.priority
                    ));
                }
            }
        }
        
        @Override
        public int getPriority() {
            return priority;
        }
        
        
        private String getCallerInfo() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length > 3) {
                StackTraceElement caller = stackTrace[3];
                return caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber();
            }
            return "неизвестно";
        }
    }
    
    
    public ThreadMasterModule() {
            }
    
    @Override
    public void onEnable() {
        super.onEnable();
        
                settings = new ThreadMasterSettings();
        loadConfig();
        
                initThreadPool();
        
                Bukkit.getPluginManager().registerEvents(this, UltimaCore.getInstance());
        
                startMonitoring();
        
                UltimaCore.getInstance().getCommand("threadmaster").setExecutor(new ThreadMasterCommand(this));
        UltimaCore.getInstance().getCommand("threadmaster").setTabCompleter(new ThreadMasterCommand(this));
        
        LoggerUtil.info("Модуль ThreadMaster успешно включен. Создан пул из " + settings.getMinWorkerThreads() + " потоков.");
    }
    
    @Override
    public void onDisable() {
                stopMonitoring();
        
                shutdownThreadPool();
        
                runningTasks.clear();
        playerStats.clear();
        
        LoggerUtil.info("Модуль ThreadMaster выключен.");
        super.onDisable();
    }
    
    
    private void loadConfig() {
        ConfigurationSection section = UltimaCore.getInstance().getConfig().getConfigurationSection("modules.threadmaster");
        if (section != null) {
            settings.loadFromConfig(section);
        }
    }
    
    
    private void initThreadPool() {
                taskQueue = new PriorityBlockingQueue<>(
                settings.getMaxTaskQueueSize(), 
                (Comparator<Runnable>) (r1, r2) -> {
                    if (r1 instanceof PrioritizedTask && r2 instanceof PrioritizedTask) {
                        return Integer.compare(
                                ((PrioritizedTask) r2).getPriority(),
                                ((PrioritizedTask) r1).getPriority()
                        );
                    }
                    return 0;
                }
        );
        
                taskExecutor = new ThreadPoolExecutor(
                settings.getMinWorkerThreads(),
                settings.getMaxWorkerThreads(),
                settings.getThreadKeepAliveTime(), TimeUnit.SECONDS,
                taskQueue,
                new ThreadMasterThreadFactory("ThreadMaster-Worker-", threadCounter)
        );
        
                taskExecutor.allowCoreThreadTimeOut(settings.isAllowCoreThreadTimeout());
        
                taskExecutor.prestartAllCoreThreads();
    }
    
    
    private void shutdownThreadPool() {
        if (taskExecutor != null) {
            taskExecutor.shutdown();
            try {
                                if (!taskExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                                        taskExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                LoggerUtil.warning("Прерывание при остановке пула потоков", e);
            }
        }
    }
    
    
    private void startMonitoring() {
                if (settings.isMonitorThreads()) {
            monitoringTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    UltimaCore.getInstance(),
                    this::monitorThreads,
                    20L,                     20L              );
        }
        
                if (settings.isAutoBalanceThreads()) {
            balancingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    UltimaCore.getInstance(),
                    this::balanceThreadPool,
                    100L,                     settings.getThreadBalanceInterval()             );
        }
        
                if (settings.isEnableDiagnostics()) {
            diagnosticsTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                    UltimaCore.getInstance(),
                    this::runDiagnostics,
                    200L,                     settings.getDiagnosticsInterval() * 20L             );
        }
    }
    
    
    private void stopMonitoring() {
        if (monitoringTask != null) {
            monitoringTask.cancel();
            monitoringTask = null;
        }
        
        if (balancingTask != null) {
            balancingTask.cancel();
            balancingTask = null;
        }
        
        if (diagnosticsTask != null) {
            diagnosticsTask.cancel();
            diagnosticsTask = null;
        }
    }
    
    
    private void monitorThreads() {
                int activeThreads = taskExecutor.getActiveCount();
        int queueSize = taskQueue.size();
        long completedTasks = taskExecutor.getCompletedTaskCount();
        
                if (settings.isDetailedLogging()) {
            LoggerUtil.info(String.format(
                    "ThreadMaster: активные потоки: %d, в очереди: %d, выполнено задач: %d",
                    activeThreads, queueSize, completedTasks
            ));
        }
        
                if (settings.isLogDeadlocks()) {
            long[] deadlockedThreads = threadBean.findDeadlockedThreads();
            if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                LoggerUtil.warning("ThreadMaster: Обнаружен deadlock потоков!");
                ThreadInfo[] threadInfos = threadBean.getThreadInfo(deadlockedThreads, true, true);
                for (ThreadInfo threadInfo : threadInfos) {
                    LoggerUtil.warning("Deadlocked thread: " + threadInfo.getThreadName());
                }
            }
        }
    }
    
    
    private void balanceThreadPool() {
                int activeThreads = taskExecutor.getActiveCount();
        int queueSize = taskQueue.size();
        int poolSize = taskExecutor.getPoolSize();
        
                double utilization = (double) activeThreads / poolSize;
        
                if (utilization > 0.75 && queueSize > 10 && poolSize < settings.getMaxWorkerThreads()) {
                        taskExecutor.setCorePoolSize(Math.min(poolSize + 1, settings.getMaxWorkerThreads()));
            LoggerUtil.info("ThreadMaster: Увеличено количество потоков до " + taskExecutor.getCorePoolSize());
        }
                else if (utilization < 0.25 && queueSize < 5 && poolSize > settings.getMinWorkerThreads()) {
                        taskExecutor.setCorePoolSize(Math.max(poolSize - 1, settings.getMinWorkerThreads()));
            LoggerUtil.info("ThreadMaster: Уменьшено количество потоков до " + taskExecutor.getCorePoolSize());
        }
    }
    
    
    private void runDiagnostics() {
                ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
        int blockedThreads = 0;
        int waitingThreads = 0;
        
        for (ThreadInfo info : threadInfos) {
            if (info.getThreadState() == Thread.State.BLOCKED) {
                blockedThreads++;
            } else if (info.getThreadState() == Thread.State.WAITING || info.getThreadState() == Thread.State.TIMED_WAITING) {
                waitingThreads++;
            }
        }
        
                LoggerUtil.info(String.format(
                "ThreadMaster диагностика: активно %d потоков, заблокировано %d, ожидают %d, задач в очереди %d",
                taskExecutor.getActiveCount(), blockedThreads, waitingThreads, taskQueue.size()
        ));
        
                if (settings.isLogLongRunningTasks()) {
            for (Map.Entry<Long, TaskInfo> entry : runningTasks.entrySet()) {
                TaskInfo info = entry.getValue();
                long executionTime = info.getExecutionTime();
                if (executionTime > settings.getMaxTaskExecutionTime()) {
                    LoggerUtil.warning(String.format(
                            "Долго выполняющаяся задача из %s: %d мс (приоритет: %d)",
                            info.source, executionTime, info.priority
                    ));
                }
            }
        }
    }
    
    
    public Future<?> submitTask(Runnable task, int priority) {
        if (!settings.isEnabled() || task == null) {
            return CompletableFuture.completedFuture(null);
        }
        
                priority = Math.max(1, Math.min(10, priority));
        
                PrioritizedTaskWrapper wrapper = new PrioritizedTaskWrapper(task, priority);
        
                return taskExecutor.submit(wrapper);
    }
    
    
    public Future<?> submitTask(Runnable task) {
        return submitTask(task, settings.getDefaultTaskPriority());
    }
    
    
    public void cancelAllTasks() {
        taskExecutor.purge();
        taskQueue.clear();
        LoggerUtil.info("ThreadMaster: Все задачи отменены и очередь очищена");
    }
    
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerStats.put(player.getUniqueId(), new PlayerThreadStats(player.getUniqueId()));
    }
    
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerStats.remove(player.getUniqueId());
    }
    
    
    public PlayerThreadStats getPlayerStats(UUID playerId) {
        return playerStats.get(playerId);
    }
    
    
    public ThreadMasterSettings getSettings() {
        return settings;
    }
    
    
    public int getActiveThreadCount() {
        return taskExecutor != null ? taskExecutor.getActiveCount() : 0;
    }
    
    
    public int getQueueSize() {
        return taskQueue != null ? taskQueue.size() : 0;
    }
    
    
    public void reload() {
                stopMonitoring();
        
                shutdownThreadPool();
        
                loadConfig();
        
                initThreadPool();
        
                startMonitoring();
        
        LoggerUtil.info("Модуль ThreadMaster перезагружен");
    }
    
    @Override
    public void onTick() {
                        if (settings.isOptimizeMainThread()) {
                        Thread currentThread = Thread.currentThread();
            if (currentThread.getPriority() != Thread.MAX_PRIORITY) {
                currentThread.setPriority(Thread.MAX_PRIORITY);
            }
        }
    }
    
    @Override
    public String getName() {
        return "ThreadMaster";
    }
    
    @Override
    public String getDescription() {
        return "Модуль многопоточности и оптимизации потоков сервера";
    }
} 