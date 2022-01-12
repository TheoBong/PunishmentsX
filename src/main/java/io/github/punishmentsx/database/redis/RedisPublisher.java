package io.github.punishmentsx.database.redis;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import lombok.Getter;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.Queue;

public class RedisPublisher {

    private Jedis jedis;
    private final PunishmentsX plugin;
    private @Getter Queue<RedisMessage> messageQueue;
    public RedisPublisher(Jedis jedis, PunishmentsX plugin) {
        this.jedis = jedis;
        this.plugin = plugin;
        this.messageQueue = new LinkedList<>();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, ()-> {
            if(!messageQueue.isEmpty()) {
                RedisMessage redisMessage = messageQueue.poll();
                jedis.publish(Locale.REDIS_CHANNEL.format(plugin), redisMessage.getMessage().toString());
            }
        }, 1, 1);
    }
}
