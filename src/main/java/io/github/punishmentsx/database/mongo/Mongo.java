package io.github.punishmentsx.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.punishmentsx.ConfigValues;
import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.Database;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Mongo extends Database {
    private final PunishmentsX plugin;
    private final MongoDatabase mongoDatabase;

    public Mongo(PunishmentsX plugin) {
        this.plugin = plugin;

        MongoClient mongoClient;
        if (plugin.getConfig().getBoolean("DATABASE.MONGO.LOCALHOST_NO_AUTH")) {
            mongoClient = MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build());
            mongoDatabase = mongoClient.getDatabase(ConfigValues.MONGO_DATABASE.format(plugin));
        } else {
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applyConnectionString(new ConnectionString(plugin.getConfig().getString(ConfigValues.MONGO_URI.format(plugin))))
                    .build();

            mongoClient = MongoClients.create(mcs);
            mongoDatabase = mongoClient.getDatabase(ConfigValues.MONGO_DATABASE.format(plugin));
        }
    }

    public Type type() {
        return Type.MONGO;
    }

    public Profile loadProfile(boolean async, String name, boolean store, MongoDeserializedResult mdr) {
        getDocument(false, "profiles", "name", name.toLowerCase(), document -> {
            if(document != null) {
                UUID uuid = (UUID) document.get("_id");
                Profile profile = new Profile(plugin, uuid);
                importFromDocument(profile, document);

                for(UUID u : profile.getPunishments()) {
                    plugin.getStorage().loadPunishment(false, u, true);
                }

                mdr.call(profile);
                if(store) {
                    plugin.getProfileManager().getProfiles().put(profile.getUuid(), profile);
                }
            } else {
                mdr.call(null);
            }
        });

        return null;
    }

    public Profile loadProfile(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        getDocument(async, "profiles", "_id", uuid, document -> {
            if(document != null) {
                Profile profile = new Profile(plugin, uuid);
                importFromDocument(profile, document);

                for(UUID u : profile.getPunishments()) {
                    plugin.getStorage().loadPunishment(false, u, true);
                }

                mdr.call(profile);
                if(store) {
                    plugin.getProfileManager().getProfiles().put(profile.getUuid(), profile);
                }
            } else {
                mdr.call(null);
            }
        });

        return null;
    }

    public void saveProfile(boolean async, Profile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", profile.getName().toLowerCase());
        map.put("current_ip", profile.getCurrentIp());
        map.put("ip_history", profile.getIpHistory());
        map.put("punishments", profile.getPunishments());
        
        MongoUpdate mu = new MongoUpdate("profiles", profile.getUuid());
        mu.setUpdate(map);
        massUpdate(async, mu);
    }

    public void loadPunishment(boolean async, UUID uuid, boolean store) {
        getDocument(async, "punishments", "_id", uuid, d -> {
            if(d != null) {
                Punishment punishment = new Punishment(plugin, uuid);

                punishment.setVictim(d.get("victim", UUID.class));
                punishment.setIssuer(d.get("issuer", UUID.class));
                punishment.setPardoner(d.get("pardoner", UUID.class));

                punishment.setStack(d.getString("stack"));
                punishment.setIssueReason(d.getString("issue_reason"));
                punishment.setPardonReason(d.getString("pardon_reason"));
                punishment.setIssued(d.getDate("issued"));
                punishment.setExpires(d.getDate("expires"));
                punishment.setPardoned(d.getDate("pardoned"));
                punishment.setType(Punishment.Type.valueOf(d.getString("type")));
                punishment.setSilentIssue(d.getBoolean("silent_issue"));
                punishment.setSilentPardon(d.getBoolean("silent_pardon"));

                if(store) {
                    plugin.getPunishmentManager().getPunishments().put(punishment.getUuid(), punishment);
                }
            }
        });
    }

    public void savePunishment(boolean async, Punishment punishment) {
        Map<String, Object> map = new HashMap<>();
        map.put("victim", punishment.getVictim());
        map.put("issuer", punishment.getIssuer());
        map.put("pardoner", punishment.getPardoner());

        map.put("stack", punishment.getStack());
        map.put("issue_reason", punishment.getIssueReason());
        map.put("pardon_reason", punishment.getPardonReason());
        map.put("issued", punishment.getIssued());
        map.put("expires", punishment.getExpires());
        map.put("pardoned", punishment.getPardoned());
        map.put("type", punishment.getType().toString());
        map.put("silent_issue", punishment.isSilentIssue());
        map.put("silent_pardon", punishment.isSilentPardon());

        MongoUpdate mu = new MongoUpdate("punishments", punishment.getUuid());
        mu.setUpdate(map);
        massUpdate(async, mu);
    }

    public void close() {

    }

    public void importFromDocument(Profile profile, Document d) {
        profile.setName(d.getString("name"));
        profile.setCurrentIp(d.getString("current_ip"));
        profile.setIpHistory(d.getList("ip_history", String.class));
        profile.setPunishments(d.getList("punishments", UUID.class));
    }

    public void getDocument(boolean async, String collectionName, String fieldName, Object id, MongoResult mongoResult) {
        ThreadUtil.runTask(async, plugin, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            if (collection.find(Filters.eq(fieldName, id)).iterator().hasNext()) {
                mongoResult.call(collection.find(Filters.eq(fieldName, id)).first());
            } else {
                mongoResult.call(null);
            }
        });
    }

    public void massUpdate(boolean async, MongoUpdate mongoUpdate) {
        massUpdate(async, mongoUpdate.getCollectionName(), mongoUpdate.getId(), mongoUpdate.getUpdate());
    }

    private void massUpdate(boolean async, String collectionName, Object id, Map<String, Object> updates) throws LinkageError {
        ThreadUtil.runTask(async, plugin, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            Document document = collection.find(new Document("_id", id)).first();
            if(document == null) {
                collection.insertOne(new Document("_id", id));
            }

            updates.forEach((key, value) -> collection.updateOne(Filters.eq("_id", id), Updates.set(key, value)));
        });
    }
}
