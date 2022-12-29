
package io.github.punishmentsx.listeners;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.filter.Filter;
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
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        Punishment mute = profile.getActivePunishment(Punishment.Type.MUTE);
        if(mute != null) {
            event.setCancelled(true);

            for (String string : Locale.MUTE_MESSAGE.formatLines(plugin)) {
                player.sendMessage(string
                        .replace("%expirationDate%", mute.expiry())
                        .replace("%expiry%", mute.duration())
                        .replace("%reason%", mute.getIssueReason()));
            }
            return;
        }

        if (plugin.getConfig().getBoolean("FILTER.ENABLED") && !player.hasPermission(plugin.getMessagesFile().getString("PERMISSIONS.BYPASS_FILTER"))) {
            if (plugin.getConfig().getBoolean("FILTER.BLOCK_UNICODE")) {
                if (Arrays.stream(event.getMessage().split("")).map(isInvalid::matcher).anyMatch(Matcher::matches)) {
                    event.setCancelled(true);
                    player.sendMessage(Colors.convertLegacyColors("&cYou may not use unicode characters in chat."));
                    return;
                }
            }

            Filter.Reason filteredReason = plugin.getFilter().filteredMessage(event.getMessage());
            Configuration config = plugin.getConfig();
            Configuration messages = plugin.getMessagesFile();
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
                case ADVERTISING:
                    section = config.getConfigurationSection("FILTER.ANTI_ADVERTISING");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Advertising (Message: " + event.getMessage() + ")";

                        stackSection = messages.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

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
                        player.sendMessage(Colors.convertLegacyColors("&cYour message: \"" + event.getMessage() + "\" has been filtered for advertising. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                case NEGATIVE_WORD_PAIR:
                    section = config.getConfigurationSection("FILTER.NEGATIVE_WORD_PAIR");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Server Disrespect (Message: " + event.getMessage() + ")";

                        stackSection = messages.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

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
                        player.sendMessage(Colors.convertLegacyColors("&cYour message: \"" + event.getMessage() + "\" has been filtered for server disrespect. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                case BLACKLISTED_WORD:
                    section = config.getConfigurationSection("FILTER.BLACKLISTED_WORDS");

                    if (section.getBoolean("PUNISHMENT_ENABLED")) {
                        stack = section.getString("PUNISHMENT_STACK");
                        silent = section.getBoolean("PUNISHMENT_SILENT");

                        reason = "[AutoMute] Foul Language (Message: " + event.getMessage() + ")";

                        stackSection = messages.getConfigurationSection("MENUS.PUNISH.SLOTS." + stack);

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
                        player.sendMessage(Colors.convertLegacyColors("&cYour message: \"" + event.getMessage() + "\" has been filtered for foul language. Attempting to bypass the filter will result in punishment."));
                        player.sendMessage("");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
