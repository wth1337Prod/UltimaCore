package me.wth.ultimaCore.commands;

import me.wth.ultimaCore.utils.LoggerUtil;
import me.wth.ultimaCore.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class AnalyzeCommand extends BaseCommand {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "UltimaCore - Анализ сервера" + ChatColor.GOLD + " ===");
            sender.sendMessage(ChatColor.GOLD + "Доступные опции анализа:");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "tps" + ChatColor.GOLD + ": Анализ TPS сервера");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "memory" + ChatColor.GOLD + ": Анализ использования памяти");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "worlds" + ChatColor.GOLD + ": Анализ нагрузки миров");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "entities" + ChatColor.GOLD + ": Анализ сущностей");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "chunks" + ChatColor.GOLD + ": Анализ загруженных чанков");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "heavychunks" + ChatColor.GOLD + ": Анализ тяжёлых чанков");
            sender.sendMessage(ChatColor.GOLD + " - " + ChatColor.YELLOW + "full" + ChatColor.GOLD + ": Полный анализ сервера");
            return;
        }
        
        String option = args[0].toLowerCase();
        switch (option) {
            case "tps":
                analyzeTPS(sender);
                break;
            case "memory":
                analyzeMemory(sender);
                break;
            case "worlds":
                analyzeWorlds(sender);
                break;
            case "entities":
                analyzeEntities(sender);
                break;
            case "chunks":
                analyzeChunks(sender);
                break;
            case "heavychunks":
                analyzeHeavyChunks(sender);
                break;
            case "full":
                analyzeTPS(sender);
                analyzeMemory(sender);
                analyzeWorlds(sender);
                analyzeEntities(sender);
                analyzeChunks(sender);
                analyzeHeavyChunks(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная опция анализа: " + option);
                sender.sendMessage(ChatColor.RED + "Используйте /ultimacore analyze для просмотра доступных опций");
        }
    }
    
    
    private void analyzeTPS(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ TPS" + ChatColor.GOLD + " ===");
        
        double[] tps = PerformanceUtils.getRecentTPS();
        String tpsString = ChatColor.GREEN + df.format(tps[0]);
        String tpsStatus = ChatColor.GREEN + "Отлично";
        
        if (tps[0] < 18.0) {
            tpsString = ChatColor.YELLOW + df.format(tps[0]);
            tpsStatus = ChatColor.YELLOW + "Нормально";
        } else if (tps[0] < 10.0) {
            tpsString = ChatColor.RED + df.format(tps[0]);
            tpsStatus = ChatColor.RED + "Критически низкий";
        }
        
        sender.sendMessage(ChatColor.GOLD + "Текущий TPS: " + tpsString + ChatColor.GOLD + " - " + tpsStatus);
        sender.sendMessage(ChatColor.GOLD + "TPS за 1 минуту: " + ChatColor.YELLOW + df.format(tps[1]));
        sender.sendMessage(ChatColor.GOLD + "TPS за 5 минут: " + ChatColor.YELLOW + df.format(tps[2]));
        
        double cpuUsage = PerformanceUtils.getCPUUsage();
        String cpuColor = ChatColor.GREEN.toString();
        String cpuStatus = "Низкая нагрузка";
        
        if (cpuUsage > 60) {
            cpuColor = ChatColor.YELLOW.toString();
            cpuStatus = "Средняя нагрузка";
        } else if (cpuUsage > 85) {
            cpuColor = ChatColor.RED.toString();
            cpuStatus = "Высокая нагрузка";
        }
        
        sender.sendMessage(ChatColor.GOLD + "Загрузка CPU: " + cpuColor + df.format(cpuUsage) + "% (" + cpuStatus + ")");
        
        sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
        if (tps[0] < 18.0) {
            sender.sendMessage(ChatColor.YELLOW + " - Проверьте нагрузку от плагинов командой /ultimacore profile");
            sender.sendMessage(ChatColor.YELLOW + " - Проанализируйте сущности в мирах для выявления источников нагрузки");
        }
        if (cpuUsage > 85) {
            sender.sendMessage(ChatColor.YELLOW + " - Сервер испытывает высокую нагрузку на CPU");
            sender.sendMessage(ChatColor.YELLOW + " - Рассмотрите возможность оптимизации или обновления сервера");
        }
    }
    
    
    private void analyzeMemory(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ памяти" + ChatColor.GOLD + " ===");
        
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        double memoryPercent = (double) usedMemory / maxMemory * 100;
        
        String memColor = ChatColor.GREEN.toString();
        String memStatus = "Достаточно памяти";
        
        if (memoryPercent > 70) {
            memColor = ChatColor.YELLOW.toString();
            memStatus = "Высокое использование памяти";
        } else if (memoryPercent > 90) {
            memColor = ChatColor.RED.toString();
            memStatus = "Критически мало свободной памяти";
        }
        
        sender.sendMessage(ChatColor.GOLD + "Использование памяти: " + memColor + usedMemory + "MB / " + maxMemory + "MB " + 
                "(" + df.format(memoryPercent) + "%) - " + memStatus);
        sender.sendMessage(ChatColor.GOLD + "Выделено JVM: " + ChatColor.YELLOW + totalMemory + "MB / " + maxMemory + "MB");
        
                long totalGCTime = PerformanceUtils.getTotalGCTime();
        sender.sendMessage(ChatColor.GOLD + "Время сборки мусора: " + ChatColor.YELLOW + totalGCTime + "мс");
        
        sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
        if (memoryPercent > 85) {
            sender.sendMessage(ChatColor.YELLOW + " - Увеличьте объем выделяемой памяти (параметр -Xmx)");
            sender.sendMessage(ChatColor.YELLOW + " - Проверьте наличие утечек памяти в плагинах");
        }
        if (totalGCTime > 10000) {
            sender.sendMessage(ChatColor.YELLOW + " - Высокое время сборки мусора может указывать на проблемы с памятью");
            sender.sendMessage(ChatColor.YELLOW + " - Рассмотрите использование G1GC с настройкой -XX:+UseG1GC");
        }
    }
    
    
    private void analyzeWorlds(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ миров" + ChatColor.GOLD + " ===");
        
        int totalChunks = 0;
        int totalEntities = 0;
        
        for (World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            int entities = world.getEntities().size();
            int players = world.getPlayers().size();
            
            totalChunks += chunks;
            totalEntities += entities;
            
            String chunkStatus = chunks < 1000 ? ChatColor.GREEN + "Низкая нагрузка" : 
                                chunks < 2000 ? ChatColor.YELLOW + "Средняя нагрузка" : 
                                ChatColor.RED + "Высокая нагрузка";
            
            sender.sendMessage(ChatColor.YELLOW + world.getName() + ChatColor.GOLD + ": " + 
                    chunks + " чанков, " + entities + " сущностей, " + players + " игроков - " + chunkStatus);
        }
        
        sender.sendMessage(ChatColor.GOLD + "Всего: " + ChatColor.YELLOW + 
                totalChunks + " чанков, " + totalEntities + " сущностей");
        
        sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
        if (totalChunks > 3000) {
            sender.sendMessage(ChatColor.YELLOW + " - Большое количество загруженных чанков снижает производительность");
            sender.sendMessage(ChatColor.YELLOW + " - Уменьшите view-distance в server.properties");
        }
        if (totalEntities > 5000) {
            sender.sendMessage(ChatColor.YELLOW + " - Большое количество сущностей может приводить к лагам");
            sender.sendMessage(ChatColor.YELLOW + " - Используйте команду /ultimacore analyze entities для более детального анализа");
        }
    }
    
    
    private void analyzeEntities(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ сущностей" + ChatColor.GOLD + " ===");
        
                for (World world : Bukkit.getWorlds()) {
            sender.sendMessage(ChatColor.YELLOW + "Мир: " + world.getName());
            
                        List<Entity> entities = world.getEntities();
            if (entities.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + " - Нет сущностей");
                continue;
            }
            
                        entities.stream()
                    .collect(Collectors.groupingBy(Entity::getType, Collectors.counting()))
                    .entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .limit(10)                     .forEach(entry -> {
                        String color = entry.getValue() > 300 ? ChatColor.RED.toString() : 
                                     entry.getValue() > 100 ? ChatColor.YELLOW.toString() : 
                                     ChatColor.GREEN.toString();
                        sender.sendMessage(ChatColor.GOLD + " - " + entry.getKey() + ": " + color + entry.getValue());
                    });
        }
        
        sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
        sender.sendMessage(ChatColor.YELLOW + " - Большое количество мобов одного типа может вызывать лаги");
        sender.sendMessage(ChatColor.YELLOW + " - Рассмотрите использование плагинов для управления спавном мобов");
        sender.sendMessage(ChatColor.YELLOW + " - Для предметов на земле используйте периодическую очистку");
    }
    
    
    private void analyzeChunks(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ чанков" + ChatColor.GOLD + " ===");
        
        int totalChunks = 0;
        Map<World, Integer> chunksPerWorld = new HashMap<>();
        
                for (World world : Bukkit.getWorlds()) {
            int chunks = world.getLoadedChunks().length;
            chunksPerWorld.put(world, chunks);
            totalChunks += chunks;
        }
        
                for (Map.Entry<World, Integer> entry : chunksPerWorld.entrySet()) {
            World world = entry.getKey();
            int chunks = entry.getValue();
            
            String status = "Низкая нагрузка";
            String color = ChatColor.GREEN.toString();
            
            if (chunks > 1000) {
                status = "Средняя нагрузка";
                color = ChatColor.YELLOW.toString();
            } else if (chunks > 2000) {
                status = "Высокая нагрузка";
                color = ChatColor.RED.toString();
            }
            
            sender.sendMessage(ChatColor.YELLOW + world.getName() + ChatColor.GOLD + ": " + 
                    color + chunks + " чанков - " + status);
        }
        
        sender.sendMessage(ChatColor.GOLD + "Всего чанков: " + ChatColor.YELLOW + totalChunks);
        
                try {
            me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule chunkMaster = 
                    (me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule) plugin.getModule("chunkmaster");
            
            if (chunkMaster != null) {
                List<me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkCoord> heavyChunks = chunkMaster.getHeavyChunks();
                
                if (!heavyChunks.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Обнаружено " + heavyChunks.size() + " тяжёлых чанков:");
                    
                                        int shown = 0;
                    for (me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkCoord coord : heavyChunks) {
                        if (shown >= 5) break;
                        
                        World world = Bukkit.getWorld(coord.worldId);
                        if (world != null) {
                            me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkMetrics metrics = 
                                    chunkMaster.getChunkMetrics(world, coord.x, coord.z);
                            
                            if (metrics != null) {
                                sender.sendMessage(ChatColor.RED + "Мир: " + world.getName() + 
                                        ", X: " + coord.x + ", Z: " + coord.z + 
                                        " - " + metrics.entityCount + " сущностей, " + 
                                        metrics.tileEntityCount + " блок-сущностей, " + 
                                        "оценка тяжести: " + metrics.getHeavinessScore() + "%");
                                shown++;
                            }
                        }
                    }
                    
                    if (heavyChunks.size() > 5) {
                        sender.sendMessage(ChatColor.YELLOW + "... и ещё " + (heavyChunks.size() - 5) + " чанков");
                    }
                    
                    sender.sendMessage(ChatColor.GOLD + "Для детального анализа используйте: " + 
                            ChatColor.YELLOW + "/ultimacore analyze heavychunks");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "Тяжёлых чанков не обнаружено");
                }
            }
        } catch (Exception e) {
            LoggerUtil.warning("Ошибка при получении информации о тяжёлых чанках", e);
        }
        
                sender.sendMessage(ChatColor.GOLD + "Рекомендации:");
        if (totalChunks > 3000) {
            sender.sendMessage(ChatColor.YELLOW + " - Уменьшите view-distance в server.properties");
            sender.sendMessage(ChatColor.YELLOW + " - Используйте /ultimacore config chunkmaster чтобы настроить квоты чанков");
        }
    }
    
    
    private void analyzeHeavyChunks(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== " + ChatColor.YELLOW + "Анализ тяжёлых чанков" + ChatColor.GOLD + " ===");
        
        try {
            me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule chunkMaster = 
                    (me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule) plugin.getModule("chunkmaster");
            
            if (chunkMaster == null) {
                sender.sendMessage(ChatColor.RED + "Модуль ChunkMaster не найден или отключен");
                return;
            }
            
            List<me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkCoord> heavyChunks = chunkMaster.getHeavyChunks();
            
            if (heavyChunks.isEmpty()) {
                sender.sendMessage(ChatColor.GREEN + "Тяжёлых чанков не обнаружено");
                return;
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Всего обнаружено: " + heavyChunks.size() + " тяжёлых чанков");
            sender.sendMessage(ChatColor.GOLD + "Топ-10 самых тяжёлых чанков:");
            
                        int maxToShow = Math.min(heavyChunks.size(), 10);
            int shown = 0;
            
            for (me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkCoord coord : heavyChunks) {
                if (shown >= maxToShow) break;
                
                World world = Bukkit.getWorld(coord.worldId);
                if (world != null) {
                    me.wth.ultimaCore.modules.chunkmaster.ChunkMasterModule.ChunkMetrics metrics = 
                            chunkMaster.getChunkMetrics(world, coord.x, coord.z);
                    
                    if (metrics != null) {
                                                String color;
                        if (metrics.getHeavinessScore() > 80) {
                            color = ChatColor.RED.toString();
                        } else if (metrics.getHeavinessScore() > 50) {
                            color = ChatColor.YELLOW.toString();
                        } else {
                            color = ChatColor.GOLD.toString();
                        }
                        
                        sender.sendMessage(color + (shown + 1) + ". Мир: " + world.getName() + 
                                ", чанк [" + coord.x + ", " + coord.z + "]");
                        sender.sendMessage(color + "   Сущности: " + metrics.entityCount + 
                                ", Блок-сущности: " + metrics.tileEntityCount);
                        sender.sendMessage(color + "   Время обновления: " + (metrics.updateTime / 1000) + " мкс" + 
                                ", Тяжесть: " + metrics.getHeavinessScore() + "%");
                        
                                                if (sender instanceof Player) {
                            sender.sendMessage(ChatColor.GRAY + "   Для телепортации: /tp " + 
                                    (coord.x * 16 + 8) + " ~ " + (coord.z * 16 + 8));
                        }
                        
                        shown++;
                    }
                }
            }
            
                        sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "Рекомендации по оптимизации:");
            sender.sendMessage(ChatColor.YELLOW + " - Проверьте на фермы или скопления мобов");
            sender.sendMessage(ChatColor.YELLOW + " - Сократите количество сложных механизмов в одном чанке");
            sender.sendMessage(ChatColor.YELLOW + " - Распределите активность по нескольким чанкам");
            sender.sendMessage(ChatColor.YELLOW + " - Используйте /ultimacore config chunkmaster для тонкой настройки");
            
        } catch (Exception e) {
            LoggerUtil.severe("Ошибка при анализе тяжёлых чанков", e);
            sender.sendMessage(ChatColor.RED + "Произошла ошибка при анализе. Проверьте логи сервера.");
        }
    }
    
    @Override
    public String getName() {
        return "analyze";
    }
    
    @Override
    public String getDescription() {
        return "Анализ производительности сервера";
    }
    
    @Override
    public String getSyntax() {
        return "/ultimacore analyze [опция]";
    }
    
    @Override
    public String getPermission() {
        return "ultimacore.command.analyze";
    }
    
    @Override
    public String[] getAliases() {
        return new String[]{"a", "analysis"};
    }
    
    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.asList("tps", "memory", "worlds", "entities", "chunks", "heavychunks", "full");
            String arg = args[0].toLowerCase();
            
            return options.stream()
                    .filter(option -> option.startsWith(arg))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
} 