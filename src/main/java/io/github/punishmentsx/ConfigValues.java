package io.github.punishmentsx;

import io.github.punishmentsx.utils.Colors;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ConfigValues {
    REDIS_CHANNEL("DATABASE.REDIS.CHANNEL"),
    MONGO_DATABASE("DATABASE.MONGO.DB"),
    MONGO_URI("DATABASE.MONGO.URI"),

    CONSOLE_NAME("GENERAL.CONSOLE_NAME");

    private String path;

    public String format(PunishmentsX plugin) {
        return Colors.convertLegacyColors(plugin.getConfig().getString(path));
    }
}