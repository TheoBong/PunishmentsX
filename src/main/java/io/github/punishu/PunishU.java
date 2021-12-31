package io.github.punishu;

import io.github.punishu.commands.BaseCommand;
import io.github.punishu.database.redis.RedisSubscriber;
import lombok.Getter;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public class PunishU extends JavaPlugin {
    private CommandMap commandMap;

    private @Getter RedisSubscriber redisSubscriber;

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public void registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
    }
}
