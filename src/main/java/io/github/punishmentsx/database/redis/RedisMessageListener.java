package io.github.punishmentsx.database.redis;

public interface RedisMessageListener {
    void onReceive(RedisMessage redisMessage);
}
