package io.github.ranolp.rattranslate;


import io.github.ranolp.rattranslate.json.BungeeJsonSender;
import io.github.ranolp.rattranslate.json.JsonSender;
import io.github.ranolp.rattranslate.json.NoJsonSender;
import io.github.ranolp.rattranslate.json.TellrawJsonSender;
import io.github.ranolp.rattranslate.locale.LocaleDelegate;
import io.github.ranolp.rattranslate.locale.ReflectionLocaleDelegate;
import io.github.ranolp.rattranslate.locale.SimpleLocaleDelegate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class BukkitPlayer extends io.github.ranolp.rattranslate.abstraction.Player implements ConfigurationSerializable {
  static final Map<String, BukkitPlayer> PLAYER_MAP = new HashMap<>();
  private static final LocaleDelegate DELEGATE;
  private static final JsonSender SENDER;

  static {
    LocaleDelegate delegate = ReflectionLocaleDelegate.getInstance();
    try {
      Player.class.getMethod("getLocale");
      delegate = SimpleLocaleDelegate.getInstance();
    } catch (NoSuchMethodException e) {
      // ignore, getLocale method not supported
    }
    JsonSender sender = NoJsonSender.INSTANCE;
    if (RatTranslate.getInstance().getPlatform().isJsonMessageAvailable()) {
      sender = TellrawJsonSender.INSTANCE;
      try {
        Class.forName("net.md_5.bungee.api.chat.TextComponent");
        sender = BungeeJsonSender.INSTANCE;
      } catch (ClassNotFoundException e) {
        // ignore, getLocale method not supported
      }
    }
    DELEGATE = delegate;
    SENDER = sender;
  }

  private String nickname;

  private BukkitPlayer(String nickname) {
    this.nickname = nickname;
  }

  public static BukkitPlayer of(Player player) {
    Objects.requireNonNull(player, "player");
    return of(player.getName());
  }

  public static BukkitPlayer of(String nickname) {
    Objects.requireNonNull(nickname, "nickname");
    return PLAYER_MAP.computeIfAbsent(nickname, BukkitPlayer::new);
  }

  public Optional<Player> getPlayer() {
    return Optional.ofNullable(Bukkit.getPlayerExact(nickname));
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String getDisplayName() {
    return getPlayer().map(Player::getDisplayName).orElse(nickname);
  }

  @Override
  public void sendMessage(String message) {
    getPlayer().ifPresent(p -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', message)));
  }

  @Override
  public void sendHoverableMessage(String message, String onHover) {
    SENDER.sendMessage(this, message, onHover);
  }

  @Override
  public boolean isOnline() {
    return getPlayer().isPresent();
  }

  @Override
  public Locale getRealLocale() {
    return getPlayer().map(DELEGATE::getLocale).map(Locale::getByCode).orElse(Locale.AMERICAN_ENGLISH);
  }

  @Override
  public Map<String, Object> serialize() {
    Map<String, Object> result = new HashMap<>();
    result.put("nickname", nickname);
    result.put("translate", getTranslateMode());
    result.put("custom-locale", getCustomLocale().map(Locale::getCode).orElse(null));
    return result;
  }

  /**
   * Required method for deserialization
   *
   * @param args map to deserialize
   * @return deserialized location
   * @throws IllegalArgumentException if the world don't exists
   * @see ConfigurationSerializable
   */
  public static BukkitPlayer deserialize(Map<String, Object> args) {
    BukkitPlayer result = BukkitPlayer.of((String) args.get("nickname"));
    Object translate = args.get("translate");
    if (translate instanceof Boolean) {
      result.setTranslateMode((Boolean) translate);
    }
    Object customLocale = args.get("custom-locale");
    if (customLocale instanceof String) {
      result.setCustomLocale(Locale.getByCode((String) customLocale));
    }
    return result;
  }
}
