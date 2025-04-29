package me.wth.ultimaCore.modules.threadmaster;

import me.wth.ultimaCore.UltimaCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ThreadMasterCommand implements CommandExecutor, TabCompleter {
    private final ThreadMasterModule module;
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    
    public ThreadMasterCommand(ThreadMasterModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.threadmaster.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                showHelp(sender);
                break;
            case "status":
                showStatus(sender);
                break;
            case "reload":
                reloadModule(sender);
                break;
            case "threads":
                showThreadInfo(sender);
                break;
            case "test":
                runTestTask(sender, args);
                break;
            case "cancel":
                cancelAllTasks(sender);
                break;
            case "settings":
                if (args.length < 2) {
                    showSettings(sender);
                } else {
                    changeSettings(sender, args);
                }
                break;
            case "diagnose":
                diagnoseLag(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда. Используйте /threadmaster help для справки.");
                break;
        }
        
        return true;
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== Помощь по ThreadMaster ===");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster help " + ChatColor.WHITE + "- Показать эту справку");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster status " + ChatColor.WHITE + "- Показать статус модуля");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster reload " + ChatColor.WHITE + "- Перезагрузить модуль");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster threads " + ChatColor.WHITE + "- Показать информацию о потоках");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster test [длительность] [приоритет] " + ChatColor.WHITE + "- Запустить тестовую задачу");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster cancel " + ChatColor.WHITE + "- Отменить все задачи");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster settings [параметр] [значение] " + ChatColor.WHITE + "- Управление настройками");
        sender.sendMessage(ChatColor.YELLOW + "/threadmaster diagnose " + ChatColor.WHITE + "- Диагностировать проблемы лагов");
    }
    
    
    private void showStatus(CommandSender sender) {
        ThreadMasterSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Статус ThreadMaster ===");
        sender.sendMessage(ChatColor.YELLOW + "Модуль активен: " + 
                (settings.isEnabled() ? ChatColor.GREEN + "Да" : ChatColor.RED + "Нет"));
        sender.sendMessage(ChatColor.YELLOW + "Активных потоков: " + 
                ChatColor.WHITE + module.getActiveThreadCount() + "/" + settings.getMaxWorkerThreads());
        sender.sendMessage(ChatColor.YELLOW + "Задач в очереди: " + 
                ChatColor.WHITE + module.getQueueSize());
        sender.sendMessage(ChatColor.YELLOW + "Асинхронная загрузка чанков: " + 
                (settings.isAsyncChunkLoading() ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Выключена"));
        sender.sendMessage(ChatColor.YELLOW + "Авто-балансировка потоков: " + 
                (settings.isAutoBalanceThreads() ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Выключена"));
        sender.sendMessage(ChatColor.YELLOW + "Диагностика: " + 
                (settings.isEnableDiagnostics() ? ChatColor.GREEN + "Включена" : ChatColor.RED + "Выключена"));
    }
    
    
    private void reloadModule(CommandSender sender) {
        try {
            module.reload();
            sender.sendMessage(ChatColor.GREEN + "Модуль ThreadMaster успешно перезагружен!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при перезагрузке модуля: " + e.getMessage());
            UltimaCore.getInstance().getLogger().severe("Ошибка при перезагрузке ThreadMaster: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    private void showThreadInfo(CommandSender sender) {
                int totalThreads = Thread.activeCount();
        int daemonThreads = 0;
        int runningThreads = 0;
        int blockedThreads = 0;
        int waitingThreads = 0;
        
        ThreadInfo[] threadInfos = threadBean.dumpAllThreads(false, false);
        for (ThreadInfo info : threadInfos) {
            if (info.getThreadState() == Thread.State.RUNNABLE) {
                runningThreads++;
            } else if (info.getThreadState() == Thread.State.BLOCKED) {
                blockedThreads++;
            } else if (info.getThreadState() == Thread.State.WAITING || info.getThreadState() == Thread.State.TIMED_WAITING) {
                waitingThreads++;
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "=== Информация о потоках ===");
        sender.sendMessage(ChatColor.YELLOW + "Всего потоков: " + ChatColor.WHITE + totalThreads);
        sender.sendMessage(ChatColor.YELLOW + "Активных: " + ChatColor.WHITE + runningThreads + 
                ChatColor.YELLOW + ", Заблокированных: " + ChatColor.WHITE + blockedThreads + 
                ChatColor.YELLOW + ", Ожидающих: " + ChatColor.WHITE + waitingThreads);
        sender.sendMessage(ChatColor.YELLOW + "Пул ThreadMaster: " + 
                ChatColor.WHITE + module.getActiveThreadCount() + " активных, " + 
                module.getQueueSize() + " в очереди");
        
                long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            sender.sendMessage(ChatColor.RED + "Обнаружен deadlock потоков: " + deadlockedThreads.length);
        } else {
            sender.sendMessage(ChatColor.GREEN + "Deadlock потоков не обнаружен");
        }
    }
    
    
    private void runTestTask(CommandSender sender, String[] args) {
                int duration = 1000;         int priority = module.getSettings().getDefaultTaskPriority();
        
                if (args.length > 1) {
            try {
                duration = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Неверный формат длительности. Должно быть число.");
                return;
            }
        }
        
        if (args.length > 2) {
            try {
                priority = Integer.parseInt(args[2]);
                if (priority < 1 || priority > 10) {
                    sender.sendMessage(ChatColor.RED + "Приоритет должен быть от 1 до 10.");
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Неверный формат приоритета. Должно быть число от 1 до 10.");
                return;
            }
        }
        
                final int finalDuration = duration;
        final int finalPriority = priority;
        
        Runnable testTask = () -> {
            long startTime = System.currentTimeMillis();
            sender.sendMessage(ChatColor.GREEN + "Тестовая задача запущена с приоритетом " + finalPriority);
            
                        try {
                while (System.currentTimeMillis() - startTime < finalDuration) {
                                        Math.sqrt(Math.pow(Math.random() * 1000, 2));
                    
                                        if ((System.currentTimeMillis() - startTime) % 100 == 0) {
                        Thread.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
                sender.sendMessage(ChatColor.RED + "Тестовая задача прервана");
                return;
            }
            
            long elapsed = System.currentTimeMillis() - startTime;
            sender.sendMessage(ChatColor.GREEN + "Тестовая задача выполнена за " + elapsed + " мс");
        };
        
                module.submitTask(testTask, finalPriority);
        sender.sendMessage(ChatColor.YELLOW + "Тестовая задача отправлена в пул потоков (длительность: " + 
                finalDuration + " мс, приоритет: " + finalPriority + ")");
    }
    
    
    private void cancelAllTasks(CommandSender sender) {
        module.cancelAllTasks();
        sender.sendMessage(ChatColor.GREEN + "Все задачи отменены и очередь очищена");
    }
    
    
    private void showSettings(CommandSender sender) {
        ThreadMasterSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "=== Настройки ThreadMaster ===");
        sender.sendMessage(ChatColor.YELLOW + "enabled: " + settings.isEnabled());
        sender.sendMessage(ChatColor.YELLOW + "minWorkerThreads: " + settings.getMinWorkerThreads());
        sender.sendMessage(ChatColor.YELLOW + "maxWorkerThreads: " + settings.getMaxWorkerThreads());
        sender.sendMessage(ChatColor.YELLOW + "autoBalanceThreads: " + settings.isAutoBalanceThreads());
        sender.sendMessage(ChatColor.YELLOW + "threadBalanceInterval: " + settings.getThreadBalanceInterval());
        sender.sendMessage(ChatColor.YELLOW + "asyncChunkLoading: " + settings.isAsyncChunkLoading());
        sender.sendMessage(ChatColor.YELLOW + "asyncWorldTicking: " + settings.isAsyncWorldTicking());
        sender.sendMessage(ChatColor.YELLOW + "optimizeMainThread: " + settings.isOptimizeMainThread());
        sender.sendMessage(ChatColor.YELLOW + "useThreadPriorities: " + settings.isUseThreadPriorities());
        sender.sendMessage(ChatColor.YELLOW + "defaultTaskPriority: " + settings.getDefaultTaskPriority());
        sender.sendMessage(ChatColor.YELLOW + "limitTaskExecutionTime: " + settings.isLimitTaskExecutionTime());
        sender.sendMessage(ChatColor.YELLOW + "maxTaskExecutionTime: " + settings.getMaxTaskExecutionTime());
        sender.sendMessage(ChatColor.YELLOW + "enableDiagnostics: " + settings.isEnableDiagnostics());
        sender.sendMessage(ChatColor.YELLOW + "logDeadlocks: " + settings.isLogDeadlocks());
        sender.sendMessage(ChatColor.YELLOW + "logLongRunningTasks: " + settings.isLogLongRunningTasks());
    }
    
    
    private void changeSettings(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Использование: /threadmaster settings <параметр> <значение>");
            return;
        }
        
        String property = args[1].toLowerCase();
        String value = args[2].toLowerCase();
        ThreadMasterSettings settings = module.getSettings();
        
        try {
            switch (property) {
                case "enabled":
                    settings.setEnabled(Boolean.parseBoolean(value));
                    break;
                case "minworkerthreads":
                    settings.setMinWorkerThreads(Integer.parseInt(value));
                    break;
                case "maxworkerthreads":
                    settings.setMaxWorkerThreads(Integer.parseInt(value));
                    break;
                case "autobalancethreads":
                    settings.setAutoBalanceThreads(Boolean.parseBoolean(value));
                    break;
                case "threadbalanceinterval":
                    settings.setThreadBalanceInterval(Integer.parseInt(value));
                    break;
                case "asyncchunkloading":
                    settings.setAsyncChunkLoading(Boolean.parseBoolean(value));
                    break;
                case "asyncworldticking":
                    settings.setAsyncWorldTicking(Boolean.parseBoolean(value));
                    break;
                case "optimizemainthread":
                    settings.setOptimizeMainThread(Boolean.parseBoolean(value));
                    break;
                case "usethreadpriorities":
                    settings.setUseThreadPriorities(Boolean.parseBoolean(value));
                    break;
                case "defaulttaskpriority":
                    settings.setDefaultTaskPriority(Integer.parseInt(value));
                    break;
                case "limittaskexecutiontime":
                    settings.setLimitTaskExecutionTime(Boolean.parseBoolean(value));
                    break;
                case "maxtaskexecutiontime":
                    settings.setMaxTaskExecutionTime(Long.parseLong(value));
                    break;
                case "enablediagnostics":
                    settings.setEnableDiagnostics(Boolean.parseBoolean(value));
                    break;
                case "logdeadlocks":
                    settings.setLogDeadlocks(Boolean.parseBoolean(value));
                    break;
                case "loglongrunningtasks":
                    settings.setLogLongRunningTasks(Boolean.parseBoolean(value));
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + property);
                    return;
            }
            
            sender.sendMessage(ChatColor.GREEN + "Параметр " + property + " изменен на " + value);
            
                        if (property.equals("minworkerthreads") || property.equals("maxworkerthreads") || 
                    property.equals("enabled") || property.equals("autobalancethreads")) {
                module.reload();
                sender.sendMessage(ChatColor.YELLOW + "Модуль перезагружен для применения изменений");
            }
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверный формат значения. Ожидалось число.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Ошибка при изменении параметра: " + e.getMessage());
        }
    }
    
    
    private void diagnoseLag(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Запуск диагностики лагов...");
        
                        double tps = estimateServerTPS();
        double serverLoadAvg = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        double memoryUsage = (double) usedMemory / maxMemory * 100;
        
        sender.sendMessage(ChatColor.YELLOW + "Текущий TPS: " + 
                (tps > 18 ? ChatColor.GREEN : tps > 15 ? ChatColor.YELLOW : ChatColor.RED) + 
                String.format("%.2f", tps));
        
        sender.sendMessage(ChatColor.YELLOW + "Загрузка системы: " + 
                (serverLoadAvg < 0 ? ChatColor.GRAY + "Н/Д" : 
                 serverLoadAvg < 1 ? ChatColor.GREEN : 
                 serverLoadAvg < 3 ? ChatColor.YELLOW : ChatColor.RED) + 
                (serverLoadAvg < 0 ? "" : String.format("%.2f", serverLoadAvg)));
        
        sender.sendMessage(ChatColor.YELLOW + "Использование памяти: " + 
                (memoryUsage < 70 ? ChatColor.GREEN : memoryUsage < 85 ? ChatColor.YELLOW : ChatColor.RED) + 
                String.format("%.1f%%", memoryUsage) + 
                ChatColor.GRAY + " (" + usedMemory + "MB / " + maxMemory + "MB)");
        
                int totalChunks = 0;
        StringBuilder worldsInfo = new StringBuilder();
        
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            totalChunks += chunks;
            worldsInfo.append("\n")
                     .append(ChatColor.GRAY)
                     .append(" - ")
                     .append(world.getName())
                     .append(": ")
                     .append(chunks)
                     .append(" чанков, ")
                     .append(world.getEntities().size())
                     .append(" сущностей");
        }
        
        sender.sendMessage(ChatColor.YELLOW + "Загружено чанков: " + 
                (totalChunks < 1000 ? ChatColor.GREEN : totalChunks < 2000 ? ChatColor.YELLOW : ChatColor.RED) + 
                totalChunks + worldsInfo.toString());
        
                sender.sendMessage(ChatColor.GREEN + "Рекомендации:");
        
        if (tps < 18) {
            sender.sendMessage(ChatColor.YELLOW + " - Низкий TPS может быть вызван перегрузкой основного потока");
            sender.sendMessage(ChatColor.YELLOW + " - Попробуйте увеличить max_worker_threads и optimize_main_thread");
        }
        
        if (memoryUsage > 85) {
            sender.sendMessage(ChatColor.YELLOW + " - Высокое использование памяти может привести к частым сборкам мусора");
            sender.sendMessage(ChatColor.YELLOW + " - Рекомендуется увеличить выделенную память JVM или проверить утечки памяти");
        }
        
        if (totalChunks > 2000) {
            sender.sendMessage(ChatColor.YELLOW + " - Слишком много загруженных чанков может вызывать нагрузку");
            sender.sendMessage(ChatColor.YELLOW + " - Уменьшите view-distance в server.properties или используйте ChunkMaster");
        }
        
                long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            sender.sendMessage(ChatColor.RED + " - Обнаружен deadlock потоков! Это может вызывать зависания сервера");
            sender.sendMessage(ChatColor.RED + " - Рекомендуется перезапустить сервер");
        }
    }
    
    
    private double estimateServerTPS() {
                double tps = 20.0;
        
        try {
                        try {
                Object minecraftServer = UltimaCore.getInstance().getServer().getClass().getMethod("getServer").invoke(UltimaCore.getInstance().getServer());
                if (minecraftServer != null) {
                    double[] tpsArray = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
                    if (tpsArray.length > 0) {
                        return Math.min(20.0, tpsArray[0]);
                    }
                }
            } catch (Exception ignored) {
                            }
            
                        Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            double memoryUsage = (double)(totalMemory - freeMemory) / maxMemory;
            
                        double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
            if (systemLoad > 0) {
                tps -= Math.min(5.0, systemLoad);
            }
            
                        if (memoryUsage > 0.8) {
                tps -= (memoryUsage - 0.8) * 20.0;
            }
            
                        int totalChunks = 0;
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                totalChunks += world.getLoadedChunks().length;
            }
            
                        if (totalChunks > 1500) {
                tps -= Math.min(5.0, (totalChunks - 1500) / 500.0);
            }
            
                        return Math.max(1.0, Math.min(20.0, tps));
        } catch (Exception e) {
                        return 18.0;
        }
    }
    
    
    private boolean isDetailedLoggingEnabled() {
        try {
                        return module.getSettings().getClass().getMethod("isDetailedLogging").invoke(module.getSettings()) instanceof Boolean 
                && (Boolean) module.getSettings().getClass().getMethod("isDetailedLogging").invoke(module.getSettings());
        } catch (Exception e) {
                        return false;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.threadmaster.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> commands = Arrays.asList("help", "status", "reload", "threads", "test", "cancel", "settings", "diagnose");
            return filterStartsWith(commands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("settings")) {
                List<String> params = Arrays.asList(
                        "enabled", "minWorkerThreads", "maxWorkerThreads", "autoBalanceThreads", 
                        "threadBalanceInterval", "asyncChunkLoading", "asyncWorldTicking", 
                        "optimizeMainThread", "useThreadPriorities", "defaultTaskPriority", 
                        "limitTaskExecutionTime", "maxTaskExecutionTime", "enableDiagnostics", 
                        "logDeadlocks", "logLongRunningTasks"
                );
                return filterStartsWith(params, args[1]);
            } else if (args[0].equalsIgnoreCase("test")) {
                return Arrays.asList("1000", "2000", "5000");             }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("settings")) {
                String param = args[1].toLowerCase();
                if (param.equals("enabled") || param.equals("autobalancethreads") || 
                        param.equals("asyncchunkloading") || param.equals("asyncworldticking") || 
                        param.equals("optimizemainthread") || param.equals("usethreadpriorities") || 
                        param.equals("limittaskexecutiontime") || param.equals("enablediagnostics") || 
                        param.equals("logdeadlocks") || param.equals("loglongrunningtasks")) {
                    return Arrays.asList("true", "false");
                } else if (param.equals("defaulttaskpriority")) {
                    return IntStream.rangeClosed(1, 10).mapToObj(Integer::toString).collect(Collectors.toList());
                } else if (param.equals("minworkerthreads") || param.equals("maxworkerthreads")) {
                    return Arrays.asList("1", "2", "4", "8", "16");
                }
            } else if (args[0].equalsIgnoreCase("test")) {
                return IntStream.rangeClosed(1, 10).mapToObj(Integer::toString).collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
    
    
    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream()
                .filter(entry -> entry.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
} 