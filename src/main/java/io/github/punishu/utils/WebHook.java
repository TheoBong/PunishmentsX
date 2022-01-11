package io.github.punishu.utils;

import io.github.punishu.PunishU;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WebHook {

    @SuppressWarnings("unchecked")
    public static void sendWebhook(PunishU plugin, String type, String victimName, String issueReason, String issuerName, String pardonReason, String expiry) {
        ThreadUtil.runTask(true, plugin, () -> {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("title", victimName + " has been " + type + "!");
            jsonObject.put("description", "A player has been " + type + " on your server!");
            jsonObject.put("color", 15258703);
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
            footer.put("text", "YourServer.com");
            footer.put("icon_url", "https://cdn.discordapp.com/avatars/553044388540973105/2334d4f212566d16c069d00620d7e4cc.png?size=1024");
            jsonObject.put("footer", footer);

            jsonArray.add(jsonObject);
            sendWebhook("Punishments", "https://i.pinimg.com/originals/fe/43/35/fe4335a9ced740248c304e8ad83cc8ea.jpg", "", jsonArray);
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
                    .url("https://discord.com/api/webhooks/923027883067330620/ObC1mAQyRi0XpJgsTY4FVWkyljEwq0TmTOJ2kql25iyfdK1bfnzPZvKN_JqBSzNnAa-D")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
