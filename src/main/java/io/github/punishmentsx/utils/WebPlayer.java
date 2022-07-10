package io.github.punishmentsx.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Data;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public @Data class WebPlayer {

    private String name;
    private UUID uuid;
    private boolean valid;

    public WebPlayer(String name) {
        fromUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
    }

    private void fromUrl(String s) {
        try {

            URL url = new URL(s);
            HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setHostnameVerifier((hostname, session) -> true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            int cp;
            while ((cp = reader.read()) != -1) {
                stringBuilder.append((char) cp);
            }

            String jsonString = stringBuilder.toString();
            JsonObject json = new JsonParser().parse(jsonString).getAsJsonObject();
            if (json != null) {
                this.uuid = UUID.fromString(json.get("id").getAsString().replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                        "$1-$2-$3-$4-$5"
                ));
                this.name = json.get("name").getAsString();
                this.valid = true;
            } else {
                this.name = null;
                this.uuid = null;
                this.valid = false;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
