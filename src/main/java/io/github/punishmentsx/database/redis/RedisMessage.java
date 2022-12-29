package io.github.punishmentsx.database.redis;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

public class RedisMessage {

    private final @Getter String internalChannel;
    private final @Getter JsonObject elements;

    public RedisMessage(String channel, JsonObject elements) {
        this.internalChannel = channel;
        this.elements = elements;
    }

    public RedisMessage(String toParse) {
        JsonParser jp = new JsonParser();
        JsonObject json = jp.parse(toParse).getAsJsonObject();
        internalChannel = json.get("ichannel").getAsString();
        elements = json.getAsJsonObject("elements");
    }

    public JsonObject getMessage() {
        JsonObject json = new JsonObject();
        json.addProperty("ichannel", internalChannel);
        json.add("elements", elements);
        return json;
    }
}
