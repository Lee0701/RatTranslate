package io.github.ranolp.rattranslate.json;

import io.github.ranolp.rattranslate.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public final class BungeeJsonSender implements JsonSender {
  private BungeeJsonSender() {
  }

  public static final BungeeJsonSender INSTANCE = new BungeeJsonSender();

  @Override
  public void sendMessage(BukkitPlayer target, String message, String onHover) {

    if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(
        "tellraw %s {\"text\":\"%s\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"%s\",\"italic\":true,\"color\":\"gray\"}}}",
        target.getNickname(), ChatColor.translateAlternateColorCodes('&', message),
        ChatColor.translateAlternateColorCodes('&', onHover)))) {
      // fail fallback
      target.sendMessage(message);
    }
  }
}