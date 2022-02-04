package io.github.punishmentsx.database;

import io.github.punishmentsx.database.mongo.MongoDeserializedResult;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import java.util.UUID;

public abstract class Database {
    public enum Type {
        MONGO, SQLite, MySQL
    }

    public abstract Type type();

    public abstract Profile loadProfile(boolean async, String name, boolean store, MongoDeserializedResult mdr);
    public abstract Profile loadProfile(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr);

    public abstract void saveProfile(boolean async, Profile profile);

    public abstract void loadPunishment(boolean async, UUID uuid, boolean store);
    public abstract void savePunishment(boolean async, Punishment punishment);

    public abstract void close();
}
