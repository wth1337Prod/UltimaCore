package me.wth.ultimaCore.modules.threadmaster;

import org.bukkit.configuration.ConfigurationSection;


public class ThreadMasterSettings {
        private boolean enabled = true;                      
        private int maxWorkerThreads = 4;                    private int minWorkerThreads = 1;                    private boolean autoBalanceThreads = true;           private int threadBalanceInterval = 1200;            
        private boolean asyncChunkLoading = true;            private boolean asyncWorldTicking = false;           
        private boolean optimizeMainThread = true;           private boolean useThreadPriorities = true;          private boolean monitorThreads = true;               
        private int maxTaskQueueSize = 1000;                 private int threadKeepAliveTime = 60;                private boolean allowCoreThreadTimeout = false;      
        private int defaultTaskPriority = 5;                 private boolean limitTaskExecutionTime = true;       private long maxTaskExecutionTime = 50;              
        private boolean enableDiagnostics = true;            private int diagnosticsInterval = 300;               private boolean logDeadlocks = true;                 private boolean logLongRunningTasks = true;          
        private boolean detailedLogging = false;
    
    
    public void loadFromConfig(ConfigurationSection section) {
        if (section == null) return;
        
                enabled = section.getBoolean("enabled", enabled);
        
                maxWorkerThreads = section.getInt("max_worker_threads", maxWorkerThreads);
        minWorkerThreads = section.getInt("min_worker_threads", minWorkerThreads);
        autoBalanceThreads = section.getBoolean("auto_balance_threads", autoBalanceThreads);
        threadBalanceInterval = section.getInt("thread_balance_interval", threadBalanceInterval);
        
                asyncChunkLoading = section.getBoolean("async_chunk_loading", asyncChunkLoading);
        asyncWorldTicking = section.getBoolean("async_world_ticking", asyncWorldTicking);
        
                optimizeMainThread = section.getBoolean("optimize_main_thread", optimizeMainThread);
        useThreadPriorities = section.getBoolean("use_thread_priorities", useThreadPriorities);
        monitorThreads = section.getBoolean("monitor_threads", monitorThreads);
        
                maxTaskQueueSize = section.getInt("max_task_queue_size", maxTaskQueueSize);
        threadKeepAliveTime = section.getInt("thread_keep_alive_time", threadKeepAliveTime);
        allowCoreThreadTimeout = section.getBoolean("allow_core_thread_timeout", allowCoreThreadTimeout);
        
                defaultTaskPriority = section.getInt("default_task_priority", defaultTaskPriority);
        limitTaskExecutionTime = section.getBoolean("limit_task_execution_time", limitTaskExecutionTime);
        maxTaskExecutionTime = section.getLong("max_task_execution_time", maxTaskExecutionTime);
        
                enableDiagnostics = section.getBoolean("enable_diagnostics", enableDiagnostics);
        diagnosticsInterval = section.getInt("diagnostics_interval", diagnosticsInterval);
        logDeadlocks = section.getBoolean("log_deadlocks", logDeadlocks);
        logLongRunningTasks = section.getBoolean("log_long_running_tasks", logLongRunningTasks);
        
                detailedLogging = section.getBoolean("detailed_logging", detailedLogging);
    }
    
    
    public boolean isEnabled() {
        return enabled;
    }
    
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    
    public int getMaxWorkerThreads() {
        return maxWorkerThreads;
    }
    
    
    public void setMaxWorkerThreads(int maxWorkerThreads) {
        this.maxWorkerThreads = maxWorkerThreads;
    }
    
    
    public int getMinWorkerThreads() {
        return minWorkerThreads;
    }
    
    
    public void setMinWorkerThreads(int minWorkerThreads) {
        this.minWorkerThreads = minWorkerThreads;
    }
    
    
    public boolean isAutoBalanceThreads() {
        return autoBalanceThreads;
    }
    
    
    public void setAutoBalanceThreads(boolean autoBalanceThreads) {
        this.autoBalanceThreads = autoBalanceThreads;
    }
    
    
    public int getThreadBalanceInterval() {
        return threadBalanceInterval;
    }
    
    
    public void setThreadBalanceInterval(int threadBalanceInterval) {
        this.threadBalanceInterval = threadBalanceInterval;
    }
    
    
    public boolean isAsyncChunkLoading() {
        return asyncChunkLoading;
    }
    
    
    public void setAsyncChunkLoading(boolean asyncChunkLoading) {
        this.asyncChunkLoading = asyncChunkLoading;
    }
    
    
    public boolean isAsyncWorldTicking() {
        return asyncWorldTicking;
    }
    
    
    public void setAsyncWorldTicking(boolean asyncWorldTicking) {
        this.asyncWorldTicking = asyncWorldTicking;
    }
    
    
    public boolean isOptimizeMainThread() {
        return optimizeMainThread;
    }
    
    
    public void setOptimizeMainThread(boolean optimizeMainThread) {
        this.optimizeMainThread = optimizeMainThread;
    }
    
    
    public boolean isUseThreadPriorities() {
        return useThreadPriorities;
    }
    
    
    public void setUseThreadPriorities(boolean useThreadPriorities) {
        this.useThreadPriorities = useThreadPriorities;
    }
    
    
    public boolean isMonitorThreads() {
        return monitorThreads;
    }
    
    
    public void setMonitorThreads(boolean monitorThreads) {
        this.monitorThreads = monitorThreads;
    }
    
    
    public int getMaxTaskQueueSize() {
        return maxTaskQueueSize;
    }
    
    
    public void setMaxTaskQueueSize(int maxTaskQueueSize) {
        this.maxTaskQueueSize = maxTaskQueueSize;
    }
    
    
    public int getThreadKeepAliveTime() {
        return threadKeepAliveTime;
    }
    
    
    public void setThreadKeepAliveTime(int threadKeepAliveTime) {
        this.threadKeepAliveTime = threadKeepAliveTime;
    }
    
    
    public boolean isAllowCoreThreadTimeout() {
        return allowCoreThreadTimeout;
    }
    
    
    public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) {
        this.allowCoreThreadTimeout = allowCoreThreadTimeout;
    }
    
    
    public int getDefaultTaskPriority() {
        return defaultTaskPriority;
    }
    
    
    public void setDefaultTaskPriority(int defaultTaskPriority) {
        this.defaultTaskPriority = defaultTaskPriority;
    }
    
    
    public boolean isLimitTaskExecutionTime() {
        return limitTaskExecutionTime;
    }
    
    
    public void setLimitTaskExecutionTime(boolean limitTaskExecutionTime) {
        this.limitTaskExecutionTime = limitTaskExecutionTime;
    }
    
    
    public long getMaxTaskExecutionTime() {
        return maxTaskExecutionTime;
    }
    
    
    public void setMaxTaskExecutionTime(long maxTaskExecutionTime) {
        this.maxTaskExecutionTime = maxTaskExecutionTime;
    }
    
    
    public boolean isEnableDiagnostics() {
        return enableDiagnostics;
    }
    
    
    public void setEnableDiagnostics(boolean enableDiagnostics) {
        this.enableDiagnostics = enableDiagnostics;
    }
    
    
    public int getDiagnosticsInterval() {
        return diagnosticsInterval;
    }
    
    
    public void setDiagnosticsInterval(int diagnosticsInterval) {
        this.diagnosticsInterval = diagnosticsInterval;
    }
    
    
    public boolean isLogDeadlocks() {
        return logDeadlocks;
    }
    
    
    public void setLogDeadlocks(boolean logDeadlocks) {
        this.logDeadlocks = logDeadlocks;
    }
    
    
    public boolean isLogLongRunningTasks() {
        return logLongRunningTasks;
    }
    
    
    public void setLogLongRunningTasks(boolean logLongRunningTasks) {
        this.logLongRunningTasks = logLongRunningTasks;
    }
    
    
    public boolean isDetailedLogging() {
        return detailedLogging;
    }
    
    
    public void setDetailedLogging(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }
} 