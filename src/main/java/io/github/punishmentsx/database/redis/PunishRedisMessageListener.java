package io.github.punishmentsx.database.redis;

import com.google.gson.JsonObject;
import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.ClickableMessage;
import io.github.punishmentsx.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PunishRedisMessageListener implements RedisMessageListener {

    private final PunishmentsX plugin;

    public PunishRedisMessageListener(PunishmentsX plugin) {
        this.plugin = plugin;
        plugin.getRedisSubscriber().getListeners().add(this);
    }

    @Override
    public void onReceive(RedisMessage redisMessage) {
        JsonObject json = redisMessage.getElements();
        if(redisMessage.getInternalChannel().equals(Locale.REDIS_CHANNEL.format(plugin))) {
            RedisAction action = RedisAction.valueOf(json.get("action").getAsString());

            String fromServer = json.get("fromServer") == null ? null : json.get("fromServer").getAsString();

            if(fromServer != null) {
                boolean thisServer = fromServer.equals(Locale.SERVER_NAME.format(plugin));
                if(!thisServer) {
                    UUID uuid = UUID.fromString(json.get("uuid").getAsString());
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        plugin.getProfileManager().pull(true, uuid, true, obj -> {
                        });
                    } else {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null && p.isOnline()) {
                                plugin.getProfileManager().pull(true, uuid, true, obj -> {
                                });
                            }
                        }, 10);
                    }
                }
                return;
            }

            switch(action) {
                case PUNISHMENT:
                    for (Profile profile : plugin.getProfileManager().getProfiles().values()) {
                        Player player = profile.getPlayer();
                        if (player != null && player.isOnline() && player.hasPermission(Locale.SILENT_PERMISSION.format(plugin))) {
                            ClickableMessage message = new ClickableMessage(Colors.get(json.get("message").getAsString()))
                                    .hover(Colors.get(json.get("hover").getAsString()));
                            message.sendToPlayer(player);
                        } else if (player != null) {
                            player.sendMessage(Colors.get(json.get("message").getAsString()));
                        }
                    }
                    break;
                case PUNISHMENT_SILENT:
                    for (Profile profile : plugin.getProfileManager().getProfiles().values()) {
                        Player player = profile.getPlayer();
                        if (player != null && player.isOnline() && player.hasPermission(Locale.SILENT_PERMISSION.format(plugin))) {
                            ClickableMessage message = new ClickableMessage(Colors.get(json.get("message").getAsString()))
                                    .hover(Colors.get(json.get("hover").getAsString()));
                            message.sendToPlayer(player);
                        }
                    }
                    break;
            }
        }
    }

    public void close() {
        plugin.getRedisSubscriber().getListeners().remove(this);
    }
}
