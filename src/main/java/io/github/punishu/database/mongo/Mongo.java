package io.github.punishu.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import io.github.punishu.PunishU;
import io.github.punishu.utils.ThreadUtil;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mongo {
    private final PunishU plugin;
    private final MongoDatabase mongoDatabase;

    public Mongo(PunishU plugin) {
        this.plugin = plugin;

        MongoClient mongoClient;
        if (plugin.getConfig().getBoolean("networking.mongo.localhost")) {
            mongoClient = MongoClients.create(MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build());
            mongoDatabase = mongoClient.getDatabase(plugin.getConfig().getString("networking.mongo.db"));
        } else {
            MongoClientSettings mcs = MongoClientSettings.builder()
                    .uuidRepresentation(UuidRepresentation.STANDARD)
                    .applyConnectionString(new ConnectionString(plugin.getConfig().getString("networking.mongo.uri")))
                    .build();

            mongoClient = MongoClients.create(mcs);
            mongoDatabase = mongoClient.getDatabase(plugin.getConfig().getString("networking.mongo.db"));
        }
    }


    public void deleteDocument(boolean async, String collectionName, Object id) {
        ThreadUtil.runTask(async, plugin, () -> {
            MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
            Document document = new Document("_id", id);

            collection.deleteMany(document);
        });
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

    public void createCollection(boolean async, String collectionName) {
        ThreadUtil.runTask(async, plugin, () -> {
            AtomicBoolean exists = new AtomicBoolean(false);
            mongoDatabase.listCollectionNames().forEach(s -> {
                if(s.equals(collectionName)) {
                    exists.set(true);
                }
            });

            if(!exists.get()) {
                mongoDatabase.createCollection(collectionName);
            }
        });
    }

    public void getCollectionIterable(boolean async, String collectionName, MongoIterableResult mir) {
        ThreadUtil.runTask(async, plugin, ()-> mir.call(mongoDatabase.getCollection(collectionName).find()));
    }
}
