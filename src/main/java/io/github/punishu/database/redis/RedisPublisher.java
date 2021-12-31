package io.github.punishu.database.redis;

import io.github.punishu.PunishU;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.Queue;

public class RedisPublisher {

    private Jedis jedis;
    private final PunishU plugin;
    private @Getter Queue<RedisMessage> messageQueue;
    public RedisPublisher(Jedis jedis, PunishU plugin) {
        this.jedis = jedis;
        this.plugin = plugin;
        this.messageQueue = new LinkedList<>();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, ()-> {
            if(!messageQueue.isEmpty()) {
                RedisMessage redisMessage = messageQueue.poll();
                jedis.publish(plugin.getConfig().getString("networking.redis.channel"), redisMessage.getMessage().toString());
            }
        }, 1, 1);
    }
}
