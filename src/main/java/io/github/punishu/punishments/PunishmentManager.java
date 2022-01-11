package io.github.punishu.punishments;

import com.google.gson.JsonObject;
import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.database.mongo.MongoDeserializedResult;
import io.github.punishu.database.mongo.MongoUpdate;
import io.github.punishu.database.redis.RedisAction;
import io.github.punishu.database.redis.RedisMessage;
import io.github.punishu.profiles.Profile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PunishmentManager {

    private final PunishU plugin;
    private Map<UUID, Punishment> punishments;
    public PunishmentManager(PunishU plugin) {
        this.plugin = plugin;
        this.punishments = new HashMap<>();
    }

    public Punishment create(Punishment.Type type, String stack, Profile victim, UUID issuer, String reason, Date expires, boolean silent) {
        for(Punishment punishment : victim.getPunishments(type)) {
            if(punishment.isActive()) {
                return null;
            }
        }

        Punishment punishment = new Punishment(plugin, UUID.randomUUID());
        punishment.setType(type);
        punishment.setStack(stack);
        punishment.setVictim(victim.getUuid());
        punishment.setIssuer(issuer);
        punishment.setIssueReason(reason);
        punishment.setIssued(new Date());
        punishment.setExpires(expires);
        punishment.setSilentIssue(silent);

        punishments.put(punishment.getUuid(), punishment);
        victim.getPunishments().add(punishment.getUuid());

        push(true, punishment, false);

        if(victim.getPlayer() == null) {
            plugin.getProfileManager().push(true, victim, false);
        }

        return punishment;
    }

    public Punishment getPunishment(UUID uuid) {
        return punishments.get(uuid);
    }

    public void pull(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        plugin.getMongo().getDocument(async, Locale.MONGO_DATABASE.format(plugin), uuid, d -> {
            if(d != null) {
                Punishment punishment = new Punishment(plugin, uuid);
                punishment.importFromDocument(d);

                mdr.call(punishment);
                if(store) {
                    punishments.put(punishment.getUuid(), punishment);
                }
            } else {
                mdr.call(null);
            }
        });
    }

    public void push(boolean async, Punishment punishment, boolean unload) {
        MongoUpdate mu = new MongoUpdate(Locale.MONGO_DATABASE.format(plugin), punishment.getUuid());
        mu.setUpdate(punishment.export());
        plugin.getMongo().massUpdate(async, mu);
        
        if(unload) {
            punishments.remove(punishment.getUuid());
        }
    }
}
