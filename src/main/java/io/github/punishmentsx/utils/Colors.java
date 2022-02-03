package io.github.punishmentsx.utils;

import org.bukkit.ChatColor;

public class Colors {
    public static String convertLegacyColors(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String strip(String s) {
        return ChatColor.stripColor(convertLegacyColors(s));
    }
}
