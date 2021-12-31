package io.github.punishu.database.redis;

public interface RedisMessageListener {
    void onReceive(RedisMessage redisMessage);
}
