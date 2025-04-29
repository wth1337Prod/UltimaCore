package me.wth.ultimaCore.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GradientUtil {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^:>]+):([^>]+)>(.*?)</gradient>");
    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern BUKKIT_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    
    public static String applyGradients(String text) {
        String result = text;
        
                Matcher gradientMatcher = GRADIENT_PATTERN.matcher(result);
        while (gradientMatcher.find()) {
            String from = gradientMatcher.group(1);
            String to = gradientMatcher.group(2);
            String content = gradientMatcher.group(3);
            
            String coloredText = applyGradient(content, from, to);
            result = result.replace(gradientMatcher.group(0), coloredText);
        }
        
                Matcher hexMatcher = HEX_PATTERN.matcher(result);
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            ChatColor chatColor = ChatColor.of("#" + hex);
            result = result.replace(hexMatcher.group(0), chatColor.toString());
        }
        
        return result;
    }

    
    public static String processColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
                String result = applyGradients(text);
        
                return ChatColor.translateAlternateColorCodes('&', result);
    }

    
    public static List<String> splitLongMessage(String message, int maxLength) {
        List<String> result = new ArrayList<>();
        
        if (message.length() <= maxLength) {
            result.add(message);
            return result;
        }
        
                String currentFormat = "";
        
                int index = 0;
        while (index < message.length()) {
            int endIndex = Math.min(index + maxLength, message.length());
            
                        if (endIndex < message.length()) {
                int spaceIndex = message.lastIndexOf(' ', endIndex);
                if (spaceIndex > index) {
                    endIndex = spaceIndex + 1;
                }
            }
            
                        String part = message.substring(index, endIndex);
            
                        part = currentFormat + part;
            
                        currentFormat = extractLastFormat(part);
            
            result.add(part);
            index = endIndex;
        }
        
        return result;
    }
    
    
    private static String extractLastFormat(String text) {
        StringBuilder result = new StringBuilder();
        
                Matcher matcher = BUKKIT_COLOR_PATTERN.matcher(text);
        int lastIndex = -1;
        
        while (matcher.find()) {
            lastIndex = matcher.start();
        }
        
        if (lastIndex != -1) {
            result.append(text.substring(lastIndex, lastIndex + 2));
        }
        
                Matcher hexMatcher = HEX_PATTERN.matcher(text);
        lastIndex = -1;
        
        while (hexMatcher.find()) {
            lastIndex = hexMatcher.start();
        }
        
        if (lastIndex != -1) {
            result.append(text.substring(lastIndex, lastIndex + 9));
        }
        
        return result.toString();
    }

    
    public static String applyGradient(String text, String fromHex, String toHex) {
                if (fromHex.startsWith("#")) fromHex = fromHex.substring(1);
        if (toHex.startsWith("#")) toHex = toHex.substring(1);
        
                Color fromColor = hexToColor(fromHex);
        Color toColor = hexToColor(toHex);
        
                List<Integer> formatPositions = new ArrayList<>();
        List<String> formatCodes = new ArrayList<>();
        
        Matcher bukkit = BUKKIT_COLOR_PATTERN.matcher(text);
        while (bukkit.find()) {
            formatPositions.add(bukkit.start());
            formatCodes.add(bukkit.group());
        }
        
                String strippedText = text.replaceAll("&[0-9a-fk-orA-FK-OR]", "");
        
                StringBuilder result = new StringBuilder();
        
        char[] chars = strippedText.toCharArray();
        int length = chars.length;
        
        for (int i = 0; i < length; i++) {
                        if (chars[i] == ' ') {
                result.append(' ');
                continue;
            }
            
                        double percent = (double) i / (length - 1);
            Color color = interpolateColor(fromColor, toColor, percent);
            
                        result.append(ChatColor.of(color)).append(chars[i]);
        }
        
                String coloredText = result.toString();
        for (int i = 0; i < formatPositions.size(); i++) {
            int pos = formatPositions.get(i);
            String code = formatCodes.get(i);
            
                        int adjustedPos = pos + (i * 14);             
                        if (adjustedPos < coloredText.length()) {
                coloredText = coloredText.substring(0, adjustedPos) + code + coloredText.substring(adjustedPos);
            }
        }
        
        return coloredText;
    }

    
    public static String highlightKeywords(String text, List<String> keywords, String fromHex, String toHex, boolean caseSensitive) {
        String result = text;
        
        for (String keyword : keywords) {
            if (keyword.isEmpty()) continue;
            
                        Pattern pattern;
            if (caseSensitive) {
                pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            } else {
                pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
            }
            
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String match = matcher.group();
                String replacement = "<gradient:" + fromHex + ":" + toHex + ">" + match + "</gradient>";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            
            matcher.appendTail(sb);
            result = sb.toString();
        }
        
        return result;
    }
    
    
    public static String highlightKeywords(String text, List<String> keywords, String fromHex, String toHex) {
        return highlightKeywords(text, keywords, fromHex, toHex, false);
    }

    
    private static Color hexToColor(String hex) {
        try {
            return new Color(
                    Integer.parseInt(hex.substring(0, 2), 16),
                    Integer.parseInt(hex.substring(2, 4), 16),
                    Integer.parseInt(hex.substring(4, 6), 16)
            );
        } catch (Exception e) {
                        return Color.WHITE;
        }
    }

    
    private static Color interpolateColor(Color start, Color end, double percent) {
        int r = (int) (start.getRed() + percent * (end.getRed() - start.getRed()));
        int g = (int) (start.getGreen() + percent * (end.getGreen() - start.getGreen()));
        int b = (int) (start.getBlue() + percent * (end.getBlue() - start.getBlue()));
        
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }
} 