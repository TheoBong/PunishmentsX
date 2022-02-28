package io.github.punishmentsx.utils;

import com.google.gson.JsonObject;
import io.github.punishmentsx.ConfigValues;
import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.redis.RedisAction;
import io.github.punishmentsx.database.redis.RedisMessage;
import io.github.punishmentsx.profiles.Profile;
import org.bukkit.entity.Player;

import java.util.Queue;

public class Notifications {
    public static void sendMessage(PunishmentsX plugin, boolean silent, String message, String hover) {
        if (plugin.getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            JsonObject j = new JsonObject();
            RedisMessage rm = new RedisMessage(ConfigValues.REDIS_CHANNEL.format(plugin), j);

            if(silent) {
                j.addProperty("action", RedisAction.PUNISHMENT_SILENT.toString());
                j.addProperty("message", Locale.SILENT_PREFIX.format(plugin) + message);
                j.addProperty("hover", hover);
            } else {
                j.addProperty("action", RedisAction.PUNISHMENT.toString());
                j.addProperty("message", message);
                j.addProperty("hover", hover);
            }

            Queue<RedisMessage> queue = plugin.getRedisPublisher().getMessageQueue();
            queue.add(rm);
        } else {
            if (silent) {
                for (Profile profile : plugin.getProfileManager().getProfiles().values()) {
                    Player player = profile.getPlayer();
                    if (player != null && player.isOnline() && player.hasPermission(Locale.SILENT_PERMISSION.format(plugin))) {
                        ClickableMessage clickableMessage = new ClickableMessage(Colors.convertLegacyColors(Locale.SILENT_PREFIX.format(plugin) + message))
                                .hover(Colors.convertLegacyColors(hover));
                        clickableMessage.sendToPlayer(player);
                    }
                }
            } else {
                for (Profile profile : plugin.getProfileManager().getProfiles().values()) {
                    Player player = profile.getPlayer();
                    if (player != null && player.isOnline() && player.hasPermission(Locale.SILENT_PERMISSION.format(plugin))) {
                        ClickableMessage clickableMessage = new ClickableMessage(Colors.convertLegacyColors(message))
                                .hover(Colors.convertLegacyColors(hover));
                        clickableMessage.sendToPlayer(player);
                    } else if (player != null) {
                        player.sendMessage(Colors.convertLegacyColors(message));
                    }
                }
            }
        }
    }
}
