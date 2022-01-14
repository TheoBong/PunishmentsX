package io.github.punishmentsx.profiles;

import com.google.gson.JsonObject;
import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.mongo.MongoDeserializedResult;
import io.github.punishmentsx.database.mongo.MongoUpdate;
import io.github.punishmentsx.database.redis.RedisAction;
import io.github.punishmentsx.database.redis.RedisMessage;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        if (plugin.usingMongo) {
            Profile[] profile = {profiles.get(uuid)};
            if(profile[0] == null) {
                pull(false, uuid, store, mdr -> {
                    if(mdr instanceof Profile) {
                        profile[0] = (Profile) mdr;
                    }
                });
            }
            return profile[0];
        } else {
            return pullSQL(uuid, store);
        }
    }

    public Profile find(String name, boolean store) {
        if (plugin.usingMongo) {
            Profile[] profile = {null};

            pull(false, name, store, mdr -> {
                if (mdr instanceof Profile) {
                    profile[0] = (Profile) mdr;
                }
            });

            return profile[0];
        } else {
            return pullSQL(name, store);
        }
    }

    public Profile get(UUID uuid) {
        return profiles.get(uuid);
    }

    public void pull(boolean async, String name, boolean store, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(false, "profiles", "name", name, document -> {
            if(document != null) {
                UUID uuid = (UUID) document.get("_id");
                Profile profile = new Profile(plugin, uuid);
                profile.importFromDocument(document);

                for(UUID u : profile.getPunishments()) {
                    plugin.getPunishmentManager().pull(false, u, true, obj -> {});
                }

                mdr.call(profile);
                if(store) {
                    profiles.put(profile.getUuid(), profile);
                }
            } else {
                mdr.call(null);
            }
        });
    }

    public void pull(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, "profiles", "_id", uuid, document -> {
            if(document != null) {
                Profile profile = new Profile(plugin, uuid);
                profile.importFromDocument(document);

                for(UUID u : profile.getPunishments()) {
                    plugin.getPunishmentManager().pull(false, u, true, obj -> {});
                }

                mdr.call(profile);
                if(store) {
                    profiles.put(profile.getUuid(), profile);
                }
            } else {
                mdr.call(null);
            }
        });
    }

    public Profile pullSQL(String name, boolean store) {
        try {
            PreparedStatement ps = plugin.getSql().getConnection().prepareStatement("SELECT * FROM profiles WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                UUID uuid = UUID.fromString(rs.getString("id"));
                String currentIp = rs.getString("current_ip");

                Profile profile = new Profile(plugin, uuid);
                List<String> ipHistory = Arrays.asList(rs.getString("ip_history").split("\\s*,\\s*"));
                List<String> punishmentsStrings = Arrays.asList(rs.getString("punishments").split("\\s*,\\s*"));

                List<UUID> punishments = new ArrayList<>();
                for (String string : punishmentsStrings) {
                    punishments.add(UUID.fromString(string));
                }

                profile.importSQL(name, currentIp, ipHistory, punishments);

                if(store) {
                    profiles.put(profile.getUuid(), profile);
                }

                return profile;
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public Profile pullSQL(UUID uuid, boolean store) {
        try {
            PreparedStatement ps = plugin.getSql().getConnection().prepareStatement("SELECT * FROM profiles WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            List<UUID> punishments = new ArrayList<>();
            String punishmentsString = rs.getString("punishments");
            if (punishmentsString != null) {
                List<String> punishmentsStrings = Arrays.asList(punishmentsString.split("\\s*,\\s*"));
                for (String string : punishmentsStrings) {
                    punishments.add(UUID.fromString(string));
                }
            } else {
                punishments = null;
            }

            String ipHistoryString = rs.getString("ip_history");
            List<String> ipHistory = null;
            if (ipHistoryString != null) {
                ipHistory = Arrays.asList(ipHistoryString.split("\\s*,\\s*"));
            }

            String name = rs.getString("name");
            String currentIp = rs.getString("current_ip");

            Profile profile = new Profile(plugin, uuid);

            profile.importSQL(name, currentIp, ipHistory, punishments);

            if(store) {
                profiles.put(profile.getUuid(), profile);
            }

            return profile;
        } catch (SQLException ignored) {}
        return null;
    }

    public void push(boolean async, Profile profile, boolean unload) {
        if (!plugin.usingMongo) {
            profile.exportSQL();
        } else {
            MongoUpdate mu = new MongoUpdate("profiles", profile.getUuid());
            mu.setUpdate(profile.export());
            plugin.getMongo().massUpdate(async, mu);
        }

        if (plugin.getConfig().getBoolean("DATABASE.REDIS.ENABLED")) {
            JsonObject json = new JsonObject();
            json.addProperty("action", RedisAction.PROFILE_UPDATE.toString());
            json.addProperty("fromServer", plugin.getConfig().getString("GENERAL.SERVER_NAME"));
            json.addProperty("uuid", profile.getUuid().toString());

            plugin.getRedisPublisher().getMessageQueue().add(new RedisMessage(Locale.REDIS_CHANNEL.format(plugin), json));
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
