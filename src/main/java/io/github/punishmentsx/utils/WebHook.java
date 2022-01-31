package io.github.punishmentsx.utils;

import io.github.punishmentsx.PunishmentsX;
import org.bukkit.configuration.ConfigurationSection;

import java.awt.*;
import java.util.UUID;
import java.util.logging.Level;

public class WebHook {
    public static void sendWebhook(PunishmentsX plugin, UUID punishmentUUID, String duration, String stack, String type, String victimName, String issueReason, String issuerName, String pardonReason, String expiry) {
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("GENERAL.DISCORD_WEBHOOK");
        String server = plugin.getConfig().getString("GENERAL.SERVER_NAME");

        if (!config.getBoolean("ENABLED")) {
            return;
        }

        if (config.getString("LINK") == null || config.getString("LINK").isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Your discord webhook link is invalid!");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(config.getString("LINK"));
        webhook.setContent("");
        webhook.setAvatarUrl(config.getString("AVATAR"));
        webhook.setUsername("Punishments");
        webhook.setTts(true);

        if (pardonReason == null) {
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(victimName + " has been " + type + " (" + duration + ")!")
                    .setDescription("Punishment UUID: " + punishmentUUID)
                    .setColor(Color.RED)
                    .addField("Victim", victimName, true)
                    .addField("Reason", issueReason, true)
                    .addField("Issuer", issuerName, true)
                    .addField("Expiry", expiry, true)
                    .addField("Server", server, true)
                    .addField("Stack", stack, true)
                    .setFooter(config.getString("SERVER_DOMAIN"), config.getString("SERVER_ICON")));
        } else {
            webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    .setTitle(victimName + " has been " + type + " (" + duration + ")!")
                    .setDescription("Punishment UUID: " + punishmentUUID)
                    .setColor(Color.GREEN)
                    .addField("Victim", victimName, true)
                    .addField("Original Reason", issueReason, true)
                    .addField("Pardoner", issuerName, true)
                    .addField("Pardon Reason", pardonReason, true)
                    .addField("Server", server, true)
                    .addField("Stack", stack, true)
                    .setFooter(config.getString("SERVER_DOMAIN"), config.getString("SERVER_ICON")));
        }

        ThreadUtil.runTask(true, plugin, () -> {
            try {
                webhook.execute();
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Discord webhook is not working correctly! Contact author!");
                e.printStackTrace();
            }
        });
    }
}
