package io.github.punishmentsx.profiles;

import com.google.gson.JsonObject;
import io.github.punishmentsx.ConfigValues;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.Database;
import io.github.punishmentsx.database.redis.RedisAction;
import io.github.punishmentsx.database.redis.RedisMessage;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;

public class ProfileManager {

    private final PunishmentsX plugin;
    private @Getter Map<UUID, Profile> profiles;
    public ProfileManager(PunishmentsX plugin) {
        this.plugin = plugin;
        this.profiles = new HashMap<>();
    }

    public Profile createProfile(UUID uuid) {
        Profile profile = new Profile(plugin, uuid);
        profiles.put(profile.getUuid(), profile);

        push(true, profile, false);
        return profile;
    }

    public Profile find(UUID uuid, boolean store) {
        if (plugin.getStorage().type() == Database.Type.MONGO) {
            Profile[] profile = {profiles.get(uuid)};
            if(profile[0] == null) {
                plugin.getStorage().loadProfile(false, uuid, store, mdr -> {
                    if(mdr instanceof Profile) {
                        profile[0] = (Profile) mdr;
                    }
                });
            }

            return profile[0];
        } else {
            return plugin.getStorage().loadProfile(false, uuid, store, obj -> {});
        }
    }

    public Profile find(String name, boolean store) {
        if (plugin.getStorage().type() == Database.Type.MONGO) {
            Profile[] profile = {null};

            plugin.getStorage().loadProfile(false, name, store, mdr -> {
                if (mdr instanceof Profile) {
                    profile[0] = (Profile) mdr;
                }
           });

            return profile[0];
        } else {
            return plugin.getStorage().loadProfile(false, name, store, obj -> {});
        }
    }

    public Profile get(UUID uuid) {
        return profiles.get(uuid);
    }

    public void push(boolean async, Profile profile, boolean unload) {
        plugin.getStorage().saveProfile(async, profile);

        if (plugin.getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            JsonObject json = new JsonObject();
            json.addProperty("action", RedisAction.PROFILE_UPDATE.toString());
            json.addProperty("fromServer", plugin.getConfig().getString("GENERAL.SERVER_NAME"));
            json.addProperty("uuid", profile.getUuid().toString());

            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage(ConfigValues.REDIS_CHANNEL.format(plugin), json));
        }

        if(unload) {
            profiles.remove(profile.getUuid());
        }
    }

    public void shutdown() {
        HashSet<Profile> toRemove = new HashSet<>();

        for(Profile profile : profiles.values()) {
            Player player = profile.getPlayer();
            if(player != null && player.isOnline()) {
                toRemove.add(profile);
            }
        }

        toRemove.forEach(profile -> push(false, profile, true));
    }
}
