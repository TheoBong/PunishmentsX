package io.github.punishu;

import io.github.punishu.commands.BaseCommand;
import io.github.punishu.commands.impl.*;
import io.github.punishu.database.mongo.Mongo;
import io.github.punishu.database.redis.PunishRedisMessageListener;
import io.github.punishu.database.redis.RedisPublisher;
import io.github.punishu.database.redis.RedisSubscriber;
import io.github.punishu.filter.Filter;
import io.github.punishu.listeners.ChatListener;
import io.github.punishu.listeners.JoinListener;
import io.github.punishu.listeners.QuitListener;
import io.github.punishu.profiles.ProfileManager;
import io.github.punishu.punishments.PunishmentManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import xyz.leuo.gooey.Gooey;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class PunishU extends JavaPlugin {
    private CommandMap commandMap;

    @Getter private Mongo mongo;

    @Getter private RedisPublisher redisPublisher;
    @Getter private RedisSubscriber redisSubscriber;

    public Gooey gooey;

    @Getter private ProfileManager profileManager;
    @Getter private PunishmentManager punishmentManager;

    @Getter private Filter filter;

    private PunishRedisMessageListener punishRedisMessageListener;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        // Mongo
        if (getConfig().getString("DATABASE.USE").equals("mongo")) {
            this.mongo = new Mongo(this);
        } else if (getConfig().getString("DATABASE.USE").equals("mysql")) {
            //do something
        } else {
            getLogger().log(Level.SEVERE, "YOU MUST SELECT EITHER  ");
            onDisable();
        }

        // Redis
        if (getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            redisPublisher = new RedisPublisher(new Jedis(getConfig().getString("DATABASE.REDIS.HOST"), getConfig().getInt("DATABASE.REDIS.PORT")), this);
            redisSubscriber = new RedisSubscriber(new Jedis(getConfig().getString("DATABASE.REDIS.HOST"), getConfig().getInt("DATABASE.REDIS.PORT")), this);
        }

        this.gooey = new Gooey(this);

        // Managers
        this.profileManager = new ProfileManager(this);
        this.punishmentManager = new PunishmentManager(this);

        this.filter = new Filter();

        // Registering Commandmap
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Listeners
        new ChatListener(this);
        new JoinListener(this);
        new QuitListener(this);

        // Commands
        registerCommand(new HistoryCommand(this, "history"));
        registerCommand(new PunishCommands(this, "punishments"));
        registerCommand(new PunishCommand(this, "punish"));
        registerCommand(new TempPunishCommands(this, "temppunishments"));
        registerCommand(new UnpunishCommands(this, "unpunishments"));

        this.punishRedisMessageListener = new PunishRedisMessageListener(this);
    }

    @Override
    public void onDisable() {
        profileManager.shutdown();

        punishRedisMessageListener.close();

        this.saveConfig();
    }

    public void registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

}
