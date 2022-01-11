package io.github.punishu.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.utils.ThreadUtil;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.Map;

public class Mongo {
    private final PunishU plugin;
    private final MongoDatabase mongoDatabase;

    public Mongo(PunishU plugin) {
        this.plugin = plugin;

        MongoClient mongoClient;
        if (plugin.getConfig().getBoolean("DATABASE.MONGO.LOCALHOST_NO_AUTH")) {
            mongoClient = MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build());
            mongoDatabase = mongoClient.getDatabase(Locale.MONGO_DATABASE.format(plugin));
        } else {
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applyConnectionString(new ConnectionString(plugin.getConfig().getString(Locale.MONGO_URI.format(plugin))))
                    .build();

            mongoClient = MongoClients.create(mcs);
            mongoDatabase = mongoClient.getDatabase(Locale.MONGO_DATABASE.format(plugin));
        }
    }

    public void getDocument(boolean async, String collectionName, Object id, MongoResult mongoResult) {
        ThreadUtil.runTask(async, plugin, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            if (collection.find(Filters.eq("_id", id)).iterator().hasNext()) {
                mongoResult.call(collection.find(Filters.eq("_id", id)).first());
            } else {
                mongoResult.call(null);
            }
        });
    }

    public void massUpdate(boolean async, MongoUpdate mongoUpdate) {
        massUpdate(async, mongoUpdate.getCollectionName(), mongoUpdate.getId(), mongoUpdate.getUpdate());
    }

    public void massUpdate(boolean async, String collectionName, Object id, Map<String, Object> updates) throws LinkageError {
        ThreadUtil.runTask(async, plugin, () -> {
            final MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);

            Document document = collection.find(new Document("_id", id)).first();
            if(document == null) {
                collection.insertOne(new Document("_id", id));
            }

            updates.forEach((key, value) -> collection.updateOne(Filters.eq("_id", id), Updates.set(key, value)));
        });
    }
}
