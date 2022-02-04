package io.github.punishmentsx;

import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.commands.impl.*;
import io.github.punishmentsx.database.Database;
import io.github.punishmentsx.database.mongo.Mongo;
import io.github.punishmentsx.database.sequel.SQL;
import io.github.punishmentsx.database.redis.PunishRedisMessageListener;
import io.github.punishmentsx.database.redis.RedisPublisher;
import io.github.punishmentsx.database.redis.RedisSubscriber;
import io.github.punishmentsx.evasion.EvasionListener;
import io.github.punishmentsx.filter.Filter;
import io.github.punishmentsx.listeners.ChatListener;
import io.github.punishmentsx.listeners.JoinListener;
import io.github.punishmentsx.listeners.QuitListener;
import io.github.punishmentsx.profiles.ProfileManager;
import io.github.punishmentsx.punishments.PunishmentManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import xyz.leuo.gooey.Gooey;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class PunishmentsX extends JavaPlugin {
    private CommandMap commandMap;

    @Getter private RedisPublisher redisPublisher;
    @Getter private RedisSubscriber redisSubscriber;

    @Deprecated @Getter private Mongo mongo;

    public Gooey gooey;

    @Getter private ProfileManager profileManager;
    @Getter private PunishmentManager punishmentManager;

    @Getter private Filter filter;

    private PunishRedisMessageListener punishRedisMessageListener;

    @Getter private Database storage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        switch (getConfig().getString("DATABASE.USE")) {
            case "mongo":
                mongo = new Mongo(this);
                storage = mongo;
                break;
            case "mysql":
                storage = new SQL(this, Database.Type.MySQL);
                break;
            case "sqlite":
                storage = new SQL(this, Database.Type.SQLite);
                break;
            default:
                getLogger().log(Level.SEVERE, "YOU MUST SELECT EITHER MONGO, MYSQL, OR SQLITE IN THE CONFIG!");
                onDisable();
                break;
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

        if (getConfig().getBoolean("FILTER.ENABLED")) {
            this.filter = new Filter(this);
        }

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
        registerCommand(new CmdPunishCommand(this, "cmdpunish"));

        if (getConfig().getBoolean("ANTI_EVASION.ENABLED")) {
            if (getConfig().getString("DATABASE.USE").equals("mongo")) {
                registerListener(new EvasionListener(this));
                registerCommand(new WhyBannedCommand(this, "whybanned"));
                registerCommand(new ExemptCommand(this, "exempt"));
                registerCommand(new UnexemptCommand(this, "unexempt"));
            } else {
                getLogger().log(Level.WARNING, "PunishmentsX did not enable anti evasion because it requires MongoDB!");
                getLogger().log(Level.WARNING, "Anti evasion will support MySQL and SQLite in a newer update!");
            }
        }

        if (getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            this.punishRedisMessageListener = new PunishRedisMessageListener(this);
        }

    }

    @Override
    public void onDisable() {
        profileManager.shutdown();

        if (getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            punishRedisMessageListener.close();
        }

        storage.close();
    }

    public void registerCommand(BaseCommand command) {
        commandMap.register(command.getName(), command);
    }

    public void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

}
