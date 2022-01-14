package io.github.punishmentsx.listeners;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.Colors;
import io.github.punishmentsx.utils.Stackables;
import io.github.punishmentsx.utils.TranslatePunishment;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final PunishmentsX plugin;

    private final Pattern isInvalid = Pattern.compile("[^\\x00-\\x7F]+");

    public ChatListener(PunishmentsX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        Punishment mute = profile.getActivePunishment(Punishment.Type.MUTE);
        if(mute != null) {
            event.setCancelled(true);

            for (String string : Locale.MUTE_MESSAGE.formatLines(plugin)) {
                player.sendMessage(string
                        .replace("%expirationDate%", mute.expiry())
                        .replace("%reason%", mute.getIssueReason()));
            }
            return;
        }

        if (plugin.getConfig().getBoolean("FILTER.ENABLED")) {
            if (plugin.getConfig().getBoolean("FILTER.BLOCK_UNICODE")) {
                if (Arrays.stream(event.getMessage().split("")).map(isInvalid::matcher).anyMatch(Matcher::matches)) {
                    event.setCancelled(true);
                    player.sendMessage(Colors.get("&cYou may not use unicode characters in chat."));
                    return;
                }
            }

            String filteredReason = plugin.getFilter().isFiltered(event.getMessage());
            Configuration config = plugin.getConfig();
            if (filteredReason == null) {
                return;
            }

            event.setCancelled(true);

            String stack;
            ConfigurationSection section;
            ConfigurationSection stackSection;
            String reason;
            boolean silent;
            int offenses;
            String punishmentString;
            List<String> punishmentsList;

            switch (filteredReason) {
                case "Advertising":
                    section = config.getConfigurationSection("FILTER.ANTI_ADVERTISING");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Advertising (Message: " + event.getMessage() + ")";

                        stackSection = config.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

                        offenses = Stackables.offenseNumber(profile, stackSection.getName());
                        punishmentsList = stackSection.getStringList("PUNISHMENTS");

                        try {
                            punishmentString = punishmentsList.get(offenses);
                        } catch (IndexOutOfBoundsException e) {
                            punishmentString = punishmentsList.get(punishmentsList.size() - 1);
                        }

                        Punishment.Type type = TranslatePunishment.type(punishmentString);
                        Date expiration = TranslatePunishment.expires(punishmentString);

                        profile.punish(type, stackSection.getName(), null, reason, expiration, silent);
                    } else {
                        player.sendMessage("");
                        player.sendMessage(Colors.get("&cYour message: \"" + event.getMessage() + "\" has been filtered for advertising. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                case "NegativeWordPair":
                    section = config.getConfigurationSection("FILTER.NEGATIVE_WORD_PAIR");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Server Disrespect (Message: " + event.getMessage() + ")";

                        stackSection = config.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

                        offenses = Stackables.offenseNumber(profile, stackSection.getName());
                        punishmentsList = stackSection.getStringList("PUNISHMENTS");

                        try {
                            punishmentString = punishmentsList.get(offenses);
                        } catch (IndexOutOfBoundsException e) {
                            punishmentString = punishmentsList.get(punishmentsList.size() - 1);
                        }

                        Punishment.Type type = TranslatePunishment.type(punishmentString);
                        Date expiration = TranslatePunishment.expires(punishmentString);

                        profile.punish(type, stackSection.getName(), null, reason, expiration, silent);
                    } else {
                        player.sendMessage("");
                        player.sendMessage(Colors.get("&cYour message: \"" + event.getMessage() + "\" has been filtered for server disrespect. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                case "BlacklistedWord":
                    section = config.getConfigurationSection("FILTER.BLACKLISTED_WORDS");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Foul Language (Message: " + event.getMessage() + ")";

                        stackSection = config.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

                        offenses = Stackables.offenseNumber(profile, stackSection.getName());
                        punishmentsList = stackSection.getStringList("PUNISHMENTS");

                        try {
                            punishmentString = punishmentsList.get(offenses);
                        } catch (IndexOutOfBoundsException e) {
                            punishmentString = punishmentsList.get(punishmentsList.size() - 1);
                        }

                        Punishment.Type type = TranslatePunishment.type(punishmentString);
                        Date expiration = TranslatePunishment.expires(punishmentString);

                        profile.punish(type, stackSection.getName(), null, reason, expiration, silent);
                    } else {
                        player.sendMessage("");
                        player.sendMessage(Colors.get("&cYour message: \"" + event.getMessage() + "\" has been filtered for foul language. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
