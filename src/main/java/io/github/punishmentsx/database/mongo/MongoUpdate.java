package io.github.punishmentsx.database.mongo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

public @Data class MongoUpdate {
    private String collectionName;
    private Object id;
    private Map<String, Object> update;

    public MongoUpdate(String collectionName, Object id) {
        this.collectionName = collectionName;
        this.id = id;
        this.update = new HashMap<>();
    }

    public MongoUpdate put(String key, Object value) {
        update.put(key, value);
        return this;
    }
}
