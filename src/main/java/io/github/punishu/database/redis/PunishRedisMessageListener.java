package io.github.punishu.database.redis;

import com.google.gson.JsonObject;
import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.profiles.Profile;
import io.github.punishu.utils.ClickableMessage;
import io.github.punishu.utils.Colors;
import org.bukkit.entity.Player;

public class PunishRedisMessageListener implements RedisMessageListener {

    private final PunishU plugin;

    public PunishRedisMessageListener(PunishU plugin) {
        this.plugin = plugin;
        plugin.getRedisSubscriber().getListeners().add(this);
    }

    @Override
    public void onReceive(RedisMessage redisMessage) {
        JsonObject json = redisMessage.getElements();
        if(redisMessage.getInternalChannel().equals(Locale.REDIS_CHANNEL.format(plugin))) {
            RedisAction action = RedisAction.valueOf(json.get("action").getAsString());

            switch(action) {
                case PUNISHMENT:
                    System.out.println("PUNISHMENT RECIEVED FROM REDIS!");

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
                    System.out.println("SILENT RECIEVED FROM REDIS!");

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
