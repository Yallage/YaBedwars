package me.ram.bedwarsitemaddon.utils;

import org.bukkit.ChatColor;

public class ColorUtil {
  public static String color(String s) {
    return ChatColor.translateAlternateColorCodes('&', s);
  }
}
