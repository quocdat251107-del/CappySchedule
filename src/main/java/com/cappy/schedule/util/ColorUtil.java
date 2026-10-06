package com.cappy.schedule.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private ColorUtil() {
    }

    public static String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Replace &#RRGGBB format
        Matcher matcher1 = HEX_PATTERN_1.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher1.find()) {
            String hexCode = matcher1.group(1);
            try {
                matcher1.appendReplacement(sb, ChatColor.of("#" + hexCode).toString());
            } catch (NoSuchMethodError | Exception e) {
                matcher1.appendReplacement(sb, "");
            }
        }
        matcher1.appendTail(sb);
        message = sb.toString();

        // Replace <#RRGGBB> format
        Matcher matcher2 = HEX_PATTERN_2.matcher(message);
        sb = new StringBuilder();
        while (matcher2.find()) {
            String hexCode = matcher2.group(1);
            try {
                matcher2.appendReplacement(sb, ChatColor.of("#" + hexCode).toString());
            } catch (NoSuchMethodError | Exception e) {
                matcher2.appendReplacement(sb, "");
            }
        }
        matcher2.appendTail(sb);
        message = sb.toString();

        // Standard legacy & codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

