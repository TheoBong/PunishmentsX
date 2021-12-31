package io.github.punishu.database.redis;

import com.google.gson.JsonObject;
import io.github.punishu.PunishU;

public class CoreRedisMessageListener implements RedisMessageListener {

    private final PunishU plugin;
    private String serverName;

    public CoreRedisMessageListener(PunishU plugin) {
        this.plugin = plugin;
        plugin.getRedisSubscriber().getListeners().add(this);

        this.serverName = plugin.getConfig().getString("general.server_name");
    }

    @Override
    public void onReceive(RedisMessage redisMessage) {
        JsonObject json = redisMessage.getElements();
        if(redisMessage.getInternalChannel().equals("punishu")) {
            CoreRedisAction action = CoreRedisAction.valueOf(json.get("action").getAsString());
            String fromServer = json.get("fromServer") == null ? null : json.get("fromServer").getAsString();
        }
    }

    public void close() {
        plugin.getRedisSubscriber().getListeners().remove(this);
    }
}
