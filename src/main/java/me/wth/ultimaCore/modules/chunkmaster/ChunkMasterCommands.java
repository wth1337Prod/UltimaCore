package me.wth.ultimaCore.modules.chunkmaster;

import me.wth.ultimaCore.api.AbstractCommand;
import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ChunkMasterCommands extends AbstractCommand {
    private final ChunkMasterModule chunkMaster;

    public ChunkMasterCommands(ChunkMasterModule chunkMaster) {
        super("chunkmaster", "Управление модулем ChunkMaster", "/chunkmaster <subcommand>", "ultimacore.chunkmaster");
        this.chunkMaster = chunkMaster;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "info":
                return handleInfo(sender);
            case "preload":
                return handlePreload(sender, args);
            case "analyze":
                return handleAnalyze(sender, args);
            case "optimize":
                return handleOptimize(sender, args);
            case "stats":
                return handleStats(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(List.of("reload", "info", "preload", "analyze", "optimize", "stats"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "preload":
                    completions.addAll(List.of("player", "chunk"));
                    break;
                case "analyze":
                    Bukkit.getWorlds().forEach(world -> completions.add(world.getName()));
                    break;
            }
        }
        
        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6===== ChunkMaster Commands =====");
        sender.sendMessage("§e/chunkmaster reload §7- Перезагрузить конфигурацию");
        sender.sendMessage("§e/chunkmaster info §7- Информация о модуле");
        sender.sendMessage("§e/chunkmaster preload <player/chunk> §7- Предзагрузка чанков");
        sender.sendMessage("§e/chunkmaster analyze <world> §7- Анализ чанков в мире");
        sender.sendMessage("§e/chunkmaster optimize §7- Оптимизация тяжёлых чанков");
        sender.sendMessage("§e/chunkmaster stats §7- Статистика модуля");
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ultimacore.chunkmaster.reload")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        chunkMaster.onDisable();
        chunkMaster.onEnable();
        sender.sendMessage("§aМодуль ChunkMaster успешно перезагружен!");
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        if (!sender.hasPermission("ultimacore.chunkmaster.info")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        sender.sendMessage("§6===== ChunkMaster Info =====");
        sender.sendMessage("§eСтатус: §aАктивен");
        sender.sendMessage("§eЗагружено чанков: §a" + getTotalLoadedChunks());
        sender.sendMessage("§eТяжёлых чанков: §a" + getHeavyChunksCount());
        return true;
    }

    private boolean handlePreload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimacore.chunkmaster.preload")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /chunkmaster preload <player/chunk>");
            return true;
        }

        if (args[1].equalsIgnoreCase("player") && sender instanceof Player) {
            Player player = (Player) sender;
            chunkMaster.preloadChunks(player.getWorld(), player.getLocation().getChunk().getX(),
                    player.getLocation().getChunk().getZ(), 3, ChunkPriority.HIGH);
            sender.sendMessage("§aЧанки вокруг вас предзагружены!");
        } else {
            sender.sendMessage("§cЭта команда доступна только для игроков!");
        }
        return true;
    }

    private boolean handleAnalyze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimacore.chunkmaster.analyze")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        World world = args.length > 1 ? Bukkit.getWorld(args[1]) : null;
        if (world == null) {
            sender.sendMessage("§cУкажите корректный мир!");
            return true;
        }

        int heavyChunks = 0;
        for (Chunk chunk : world.getLoadedChunks()) {
            if (chunkMaster.isHeavyChunk(chunk)) {
                heavyChunks++;
            }
        }

        sender.sendMessage("§6===== Анализ мира " + world.getName() + " =====");
        sender.sendMessage("§eЗагружено чанков: §a" + world.getLoadedChunks().length);
        sender.sendMessage("§eТяжёлых чанков: §a" + heavyChunks);
        return true;
    }

    private boolean handleOptimize(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimacore.chunkmaster.optimize")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        int optimized = chunkMaster.optimizeHeavyChunks();
        sender.sendMessage("§aОптимизировано §e" + optimized + " §aтяжёлых чанков");
        return true;
    }

    private boolean handleStats(CommandSender sender) {
        if (!sender.hasPermission("ultimacore.chunkmaster.stats")) {
            sender.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }

        sender.sendMessage("§6===== Статистика ChunkMaster =====");
        sender.sendMessage("§eВсего загружено чанков: §a" + getTotalLoadedChunks());
        sender.sendMessage("§eТяжёлых чанков: §a" + getHeavyChunksCount());
        
        for (World world : Bukkit.getWorlds()) {
            sender.sendMessage("§eМир " + world.getName() + ": §a" + world.getLoadedChunks().length + " §eчанков");
        }
        return true;
    }

    private int getTotalLoadedChunks() {
        int total = 0;
        for (World world : Bukkit.getWorlds()) {
            total += world.getLoadedChunks().length;
        }
        return total;
    }

    private int getHeavyChunksCount() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                if (chunkMaster.isHeavyChunk(chunk)) {
                    count++;
                }
            }
        }
        return count;
    }
} 