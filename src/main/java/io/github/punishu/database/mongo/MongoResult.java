package io.github.punishu.database.mongo;

import org.bson.Document;

public interface MongoResult {
    void call(Document document);
}
