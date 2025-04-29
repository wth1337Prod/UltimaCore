package me.wth.ultimaCore.modules.physicsoptimizer;

import me.wth.ultimaCore.utils.LoggerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PhysicsOptimizerCommand implements CommandExecutor, TabCompleter {
    private final PhysicsOptimizerModule module;
    private final List<String> subCommands = Arrays.asList(
            "stats", "settings", "reload", "toggle", "help", "reset"
    );
    
    
    public PhysicsOptimizerCommand(PhysicsOptimizerModule module) {
        this.module = module;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimacore.physics")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для использования этой команды!");
            return true;
        }
        
        if (args.length == 0) {
                        showStatus(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "stats":
                                showPhysicsStatistics(sender);
                return true;
                
            case "settings":
                                handleSettingsCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                return true;
                
            case "reload":
                                module.loadConfig();
                sender.sendMessage(ChatColor.GREEN + "Настройки модуля PhysicsOptimizer перезагружены.");
                return true;
                
            case "toggle":
                                boolean newState = !module.getSettings().isEnabled();
                module.setEnabled(newState);
                sender.sendMessage(ChatColor.GREEN + "Модуль PhysicsOptimizer " + 
                        (newState ? "включен" : "выключен") + ".");
                return true;
                
            case "reset":
                                module.resetEventCounters();
                sender.sendMessage(ChatColor.GREEN + "Счетчики событий физики сброшены.");
                return true;
                
            case "help":
                                showHelp(sender);
                return true;
                
            default:
                sender.sendMessage(ChatColor.RED + "Неизвестная подкоманда: " + subCommand);
                sender.sendMessage(ChatColor.RED + "Используйте /physics help для получения справки.");
                return true;
        }
    }
    
    
    private void showStatus(CommandSender sender) {
        PhysicsOptimizerSettings settings = module.getSettings();
        
        sender.sendMessage(ChatColor.GREEN + "===== Статус модуля PhysicsOptimizer =====");
        sender.sendMessage(ChatColor.GOLD + "Статус: " + (settings.isEnabled() ? 
                ChatColor.GREEN + "Включен" : ChatColor.RED + "Выключен"));
        
                sender.sendMessage(ChatColor.GOLD + "Основные ограничения:");
        sender.sendMessage(ChatColor.YELLOW + " • Ограничение всей физики: " + getBooleanColor(settings.isLimitAllPhysics()));
        sender.sendMessage(ChatColor.YELLOW + " • Порог событий физики: " + ChatColor.WHITE + settings.getPhysicsEventThreshold());
        sender.sendMessage(ChatColor.YELLOW + " • Макс. физики на чанк: " + ChatColor.WHITE + settings.getMaxPhysicsPerChunk());
        
                sender.sendMessage(ChatColor.GOLD + "Активные оптимизации:");
        sender.sendMessage(ChatColor.YELLOW + " • Падающие блоки: " + getBooleanColor(settings.isLimitFallingBlocks()));
        sender.sendMessage(ChatColor.YELLOW + " • Жидкости: " + getBooleanColor(settings.isLimitFluidFlow()));
        sender.sendMessage(ChatColor.YELLOW + " • Взрывы: " + getBooleanColor(settings.isOptimizeExplosions()));
        sender.sendMessage(ChatColor.YELLOW + " • Ограничение скорости физики: " + getBooleanColor(settings.isEnablePhysicsRateLimit()));
        
                Map<String, PhysicsOptimizerModule.PhysicsEventCounter> counters = module.getEventCounters();
        int totalEvents = counters.values().stream().mapToInt(PhysicsOptimizerModule.PhysicsEventCounter::getCount).sum();
        
        sender.sendMessage(ChatColor.GOLD + "Статистика:");
        sender.sendMessage(ChatColor.YELLOW + " • Всего событий: " + ChatColor.WHITE + totalEvents);
        
        if (totalEvents > 0) {
            if (counters.containsKey("block_physics")) {
                sender.sendMessage(ChatColor.YELLOW + " • События физики блоков: " + 
                        ChatColor.WHITE + counters.get("block_physics").getCount());
            }
            if (counters.containsKey("fluid_flow")) {
                sender.sendMessage(ChatColor.YELLOW + " • События течения жидкости: " + 
                        ChatColor.WHITE + counters.get("fluid_flow").getCount());
            }
            if (counters.containsKey("falling_block")) {
                sender.sendMessage(ChatColor.YELLOW + " • События падающих блоков: " + 
                        ChatColor.WHITE + counters.get("falling_block").getCount());
            }
            if (counters.containsKey("explosion")) {
                sender.sendMessage(ChatColor.YELLOW + " • События взрывов: " + 
                        ChatColor.WHITE + counters.get("explosion").getCount());
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    
    private void showPhysicsStatistics(CommandSender sender) {
        Map<String, PhysicsOptimizerModule.PhysicsEventCounter> counters = module.getEventCounters();
        
        sender.sendMessage(ChatColor.GREEN + "===== Статистика событий физики =====");
        
        if (counters.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Статистика пуста. Событий физики не зарегистрировано.");
        } else {
            for (PhysicsOptimizerModule.PhysicsEventCounter counter : counters.values()) {
                String eventType = counter.getEventType();
                int count = counter.getCount();
                
                String displayName = switch (eventType) {
                    case "block_physics" -> "Физика блоков";
                    case "fluid_flow" -> "Течение жидкости";
                    case "falling_block" -> "Падающие блоки";
                    case "explosion" -> "Взрывы";
                    case "explosion_prime" -> "Начала взрывов";
                    default -> eventType;
                };
                
                sender.sendMessage(ChatColor.GOLD + displayName + ": " + ChatColor.WHITE + count);
            }
        }
        
        sender.sendMessage(ChatColor.GREEN + "====================================");
    }
    
    
    private void handleSettingsCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ultimacore.physics.admin")) {
            sender.sendMessage(ChatColor.RED + "У вас нет прав для изменения настроек!");
            return;
        }
        
        PhysicsOptimizerSettings settings = module.getSettings();
        
        if (args.length == 0) {
                        showSettings(sender, settings);
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Неверное количество аргументов!");
            sender.sendMessage(ChatColor.RED + "Использование: /physics settings <параметр> <значение>");
            return;
        }
        
        String param = args[0].toLowerCase();
        String value = args[1];
        
        try {
            switch (param) {
                                case "enabled":
                    settings.setEnabled(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Модуль " + 
                            (settings.isEnabled() ? "включен" : "выключен") + ".");
                    break;
                    
                case "limitallphysics":
                    settings.setLimitAllPhysics(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Ограничение всей физики " + 
                            (settings.isLimitAllPhysics() ? "включено" : "выключено") + ".");
                    break;
                    
                case "physicseventthreshold":
                    settings.setPhysicsEventThreshold(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Порог событий физики установлен на " + value + ".");
                    break;
                    
                case "eventthresholdcancelchance":
                    settings.setEventThresholdCancelChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс отмены события при превышении порога установлен на " + value + ".");
                    break;
                
                                case "chunkcooldownticks":
                    settings.setChunkCooldownTicks(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Время охлаждения чанка установлено на " + value + " тиков.");
                    break;
                    
                case "chunkcooldowncancelchance":
                    settings.setChunkCooldownCancelChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс отмены событий в чанке на охлаждении установлен на " + value + ".");
                    break;
                    
                case "maxphysicsperchunk":
                    settings.setMaxPhysicsPerChunk(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Максимальное количество событий физики в чанке установлено на " + value + ".");
                    break;
                
                                case "enablephysicsratelimit":
                    settings.setEnablePhysicsRateLimit(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Ограничение скорости обработки физики " + 
                            (settings.isEnablePhysicsRateLimit() ? "включено" : "выключено") + ".");
                    break;
                    
                case "physicsprocessinginterval":
                    settings.setPhysicsProcessingInterval(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Интервал обработки физики установлен на " + value + " тиков.");
                    break;
                    
                case "maxqueuedphysicsupdates":
                    settings.setMaxQueuedPhysicsUpdates(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Максимальное количество отложенных обновлений физики установлено на " + value + ".");
                    break;
                
                                case "limitfallingblocks":
                    settings.setLimitFallingBlocks(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Ограничение падающих блоков " + 
                            (settings.isLimitFallingBlocks() ? "включено" : "выключено") + ".");
                    break;
                    
                case "maxfallingblocksperworld":
                    settings.setMaxFallingBlocksPerWorld(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Максимальное количество падающих блоков в мире установлено на " + value + ".");
                    break;
                    
                case "fallingblocklimitationchance":
                    settings.setFallingBlockLimitationChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс отмены падения блока при превышении лимита установлен на " + value + ".");
                    break;
                    
                case "mergefallingblocks":
                    settings.setMergeFallingBlocks(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Объединение падающих блоков " + 
                            (settings.isMergeFallingBlocks() ? "включено" : "выключено") + ".");
                    break;
                    
                case "fallingblockmergethreshold":
                    settings.setFallingBlocksMergeThreshold(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Порог для начала объединения падающих блоков установлен на " + value + ".");
                    break;
                    
                case "fallingblockmergechance":
                    settings.setFallingBlocksMergeChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс объединения близких падающих блоков установлен на " + value + ".");
                    break;
                
                                case "limitfluidflow":
                    settings.setLimitFluidFlow(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Ограничение течения жидкостей " + 
                            (settings.isLimitFluidFlow() ? "включено" : "выключено") + ".");
                    break;
                    
                case "maxfluidupdatespersecond":
                    settings.setMaxFluidUpdatesPerSecond(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Максимальное количество обновлений жидкости в секунду установлено на " + value + ".");
                    break;
                    
                case "fluidlimitationchance":
                    settings.setFluidLimitationChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс ограничения потока жидкости установлен на " + value + ".");
                    break;
                    
                case "lavaflowreducechance":
                    settings.setLavaFlowReduceChance(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Шанс дополнительного ограничения потока лавы установлен на " + value + ".");
                    break;
                
                                case "optimizeexplosions":
                    settings.setOptimizeExplosions(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Оптимизация взрывов " + 
                            (settings.isOptimizeExplosions() ? "включена" : "выключена") + ".");
                    break;
                    
                case "limitexplosionblocks":
                    settings.setLimitExplosionBlocks(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Ограничение количества блоков, разрушаемых взрывом, " + 
                            (settings.isLimitExplosionBlocks() ? "включено" : "выключено") + ".");
                    break;
                    
                case "maxblocksperexplosion":
                    settings.setMaxBlocksPerExplosion(Integer.parseInt(value));
                    sender.sendMessage(ChatColor.GREEN + "Максимальное количество блоков, разрушаемых одним взрывом, установлено на " + value + ".");
                    break;
                    
                case "reduceexplosionradius":
                    settings.setReduceExplosionRadius(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Уменьшение радиуса взрыва " + 
                            (settings.isReduceExplosionRadius() ? "включено" : "выключено") + ".");
                    break;
                    
                case "explosionradiusreduction":
                    settings.setExplosionRadiusReduction(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Коэффициент уменьшения радиуса взрыва установлен на " + value + ".");
                    break;
                    
                case "reduceexplosionfire":
                    settings.setReduceExplosionFire(Boolean.parseBoolean(value));
                    sender.sendMessage(ChatColor.GREEN + "Уменьшение распространения огня от взрыва " + 
                            (settings.isReduceExplosionFire() ? "включено" : "выключено") + ".");
                    break;
                    
                case "firespreadreduction":
                    settings.setFireSpreadReduction(Double.parseDouble(value));
                    sender.sendMessage(ChatColor.GREEN + "Коэффициент уменьшения распространения огня установлен на " + value + ".");
                    break;
                    
                default:
                    sender.sendMessage(ChatColor.RED + "Неизвестный параметр: " + param);
                    return;
            }
            
                        module.saveConfig();
            
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Неверный формат значения: " + value);
            LoggerUtil.warning("Ошибка при изменении настроек PhysicsOptimizer: " + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Произошла ошибка при изменении настроек: " + e.getMessage());
            LoggerUtil.warning("Ошибка при изменении настроек PhysicsOptimizer", e);
        }
    }
    
    
    private void showSettings(CommandSender sender, PhysicsOptimizerSettings settings) {
        sender.sendMessage(ChatColor.GREEN + "===== Настройки PhysicsOptimizer =====");
        
                sender.sendMessage(ChatColor.GOLD + "Общие настройки:");
        sender.sendMessage(ChatColor.YELLOW + "  Модуль активен: " + getBooleanColor(settings.isEnabled()));
        sender.sendMessage(ChatColor.YELLOW + "  Ограничивать всю физику: " + getBooleanColor(settings.isLimitAllPhysics()));
        sender.sendMessage(ChatColor.YELLOW + "  Порог событий физики: " + ChatColor.WHITE + settings.getPhysicsEventThreshold());
        
                sender.sendMessage(ChatColor.GOLD + "Настройки чанков:");
        sender.sendMessage(ChatColor.YELLOW + "  Время охлаждения чанка: " + ChatColor.WHITE + settings.getChunkCooldownTicks() + " тиков");
        sender.sendMessage(ChatColor.YELLOW + "  Макс. физики на чанк: " + ChatColor.WHITE + settings.getMaxPhysicsPerChunk());
        
                sender.sendMessage(ChatColor.GOLD + "Настройки обработки физики:");
        sender.sendMessage(ChatColor.YELLOW + "  Ограничение скорости: " + getBooleanColor(settings.isEnablePhysicsRateLimit()));
        sender.sendMessage(ChatColor.YELLOW + "  Интервал обработки: " + ChatColor.WHITE + settings.getPhysicsProcessingInterval() + " тиков");
        
                sender.sendMessage(ChatColor.GOLD + "Настройки падающих блоков:");
        sender.sendMessage(ChatColor.YELLOW + "  Ограничивать падающие блоки: " + getBooleanColor(settings.isLimitFallingBlocks()));
        sender.sendMessage(ChatColor.YELLOW + "  Макс. падающих блоков на мир: " + ChatColor.WHITE + settings.getMaxFallingBlocksPerWorld());
        sender.sendMessage(ChatColor.YELLOW + "  Объединять падающие блоки: " + getBooleanColor(settings.isMergeFallingBlocks()));
        
                sender.sendMessage(ChatColor.GOLD + "Настройки жидкостей:");
        sender.sendMessage(ChatColor.YELLOW + "  Ограничивать течение: " + getBooleanColor(settings.isLimitFluidFlow()));
        sender.sendMessage(ChatColor.YELLOW + "  Макс. обновлений в секунду: " + ChatColor.WHITE + settings.getMaxFluidUpdatesPerSecond());
        
                sender.sendMessage(ChatColor.GOLD + "Настройки взрывов:");
        sender.sendMessage(ChatColor.YELLOW + "  Оптимизировать взрывы: " + getBooleanColor(settings.isOptimizeExplosions()));
        sender.sendMessage(ChatColor.YELLOW + "  Ограничить количество блоков: " + getBooleanColor(settings.isLimitExplosionBlocks()));
        sender.sendMessage(ChatColor.YELLOW + "  Макс. блоков на взрыв: " + ChatColor.WHITE + settings.getMaxBlocksPerExplosion());
        sender.sendMessage(ChatColor.YELLOW + "  Уменьшать радиус: " + getBooleanColor(settings.isReduceExplosionRadius()));
        sender.sendMessage(ChatColor.YELLOW + "  Уменьшать огонь: " + getBooleanColor(settings.isReduceExplosionFire()));
        
        sender.sendMessage(ChatColor.GREEN + "Используйте /physics settings <параметр> <значение> для изменения настроек.");
        sender.sendMessage(ChatColor.GREEN + "==========================================");
    }
    
    
    private String getBooleanColor(boolean value) {
        return (value ? ChatColor.GREEN + "Включено" : ChatColor.RED + "Выключено");
    }
    
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "===== Справка по команде /physics =====");
        sender.sendMessage(ChatColor.GOLD + "/physics" + ChatColor.WHITE + " - Показать статус модуля");
        sender.sendMessage(ChatColor.GOLD + "/physics stats" + ChatColor.WHITE + " - Показать статистику событий физики");
        sender.sendMessage(ChatColor.GOLD + "/physics settings" + ChatColor.WHITE + " - Показать текущие настройки");
        sender.sendMessage(ChatColor.GOLD + "/physics settings <параметр> <значение>" + ChatColor.WHITE + " - Изменить настройку");
        sender.sendMessage(ChatColor.GOLD + "/physics reload" + ChatColor.WHITE + " - Перезагрузить настройки модуля");
        sender.sendMessage(ChatColor.GOLD + "/physics toggle" + ChatColor.WHITE + " - Включить/выключить модуль");
        sender.sendMessage(ChatColor.GOLD + "/physics reset" + ChatColor.WHITE + " - Сбросить счетчики событий");
        sender.sendMessage(ChatColor.GOLD + "/physics help" + ChatColor.WHITE + " - Показать эту справку");
        sender.sendMessage(ChatColor.GREEN + "========================================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ultimacore.physics")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("settings")) {
            PhysicsOptimizerSettings settings = module.getSettings();
            List<String> settingsParams = Arrays.asList(
                                        "enabled", "limitAllPhysics", "physicsEventThreshold", "eventThresholdCancelChance",
                                        "chunkCooldownTicks", "chunkCooldownCancelChance", "maxPhysicsPerChunk",
                                        "enablePhysicsRateLimit", "physicsProcessingInterval", "maxQueuedPhysicsUpdates",
                                        "limitFallingBlocks", "maxFallingBlocksPerWorld", "fallingBlockLimitationChance", 
                    "mergeFallingBlocks", "fallingBlocksMergeThreshold", "fallingBlocksMergeChance",
                                        "limitFluidFlow", "maxFluidUpdatesPerSecond", "fluidLimitationChance", "lavaFlowReduceChance",
                                        "optimizeExplosions", "limitExplosionBlocks", "maxBlocksPerExplosion", 
                    "reduceExplosionRadius", "explosionRadiusReduction", "reduceExplosionFire", "fireSpreadReduction"
            );
            
            return settingsParams.stream()
                    .filter(param -> param.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("settings")) {
            String param = args[1].toLowerCase();
            
                        if (param.contains("enabled") || param.contains("limit") || param.contains("reduce") || 
                param.contains("merge") || param.contains("optimize")) {
                return Arrays.asList("true", "false").stream()
                        .filter(val -> val.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return new ArrayList<>();
    }
} 