package io.github.punishmentsx.utils;

import io.github.punishmentsx.PunishmentsX;
import okhttp3.*;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.logging.Level;

public class WebHook {
    private static ConfigurationSection config;

    @SuppressWarnings("unchecked")
    public static void sendWebhook(PunishmentsX plugin, String type, String victimName, String issueReason, String issuerName, String pardonReason, String expiry) {
        config = plugin.getConfig().getConfigurationSection("GENERAL.DISCORD_WEBHOOK");

        if (!config.getBoolean("ENABLED")) {
            return;
        }

        if (config.getString("LINK") == null || config.getString("LINK").isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Your discord webhook link is invalid!");
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("title", victimName + " has been " + type + "!");
            jsonObject.put("description", "A player has been " + type + " on your server!");
            jsonObject.put("color", config.getString("COLOR"));
            JSONArray fields = new JSONArray();

            JSONObject field1 = new JSONObject();
            field1.put("name", "Victim");
            field1.put("value", victimName);
            field1.put("inline", true);
            fields.add(field1);

            JSONObject field2 = new JSONObject();
            field2.put("name", "Original Reason");
            field2.put("value", issueReason);
            fields.add(field2);

            JSONObject field3 = new JSONObject();
            field3.put("name", "Pardoner");
            field3.put("value", issuerName);
            field3.put("inline", true);
            fields.add(field3);

            if (pardonReason != null) {
                JSONObject field4 = new JSONObject();
                field4.put("name", "Pardon Reason");
                field4.put("value", pardonReason);
                fields.add(field4);
            }

            if (expiry != null) {
                JSONObject field4 = new JSONObject();
                field4.put("name", "Expires");
                field4.put("value", expiry);
                fields.add(field4);
            }

            jsonObject.put("fields", fields);

            JSONObject footer = new JSONObject();
            footer.put("text", config.getString("SERVER_DOMAIN"));
            footer.put("icon_url", config.getString("SERVER_ICON"));
            jsonObject.put("footer", footer);

            jsonArray.add(jsonObject);
            sendWebhook("Punishments", config.getString("AVATAR"), "", jsonArray);
        });
    }

    @SuppressWarnings("unchecked")
    private static void sendWebhook(String username, String avatar_url, String content, JSONArray embeds) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("username", username);
            obj.put("avatar_url", avatar_url);
            obj.put("content", content);
            obj.put("embeds", embeds);

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), obj.toString());
            Request request = new Request.Builder()
                    .url(config.getString("LINK"))
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            System.out.println("PunishmentsX failed to send webhook! Please review config.");
            e.printStackTrace();
        }
    }
}
