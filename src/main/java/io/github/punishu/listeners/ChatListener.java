package io.github.punishu.listeners;

import io.github.punishu.PunishU;
import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;
import io.github.punishu.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener implements Listener {
    private final PunishU plugin;

    private final Pattern isInvalid = Pattern.compile("[^\\x00-\\x7F]+");

    public ChatListener(PunishU plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        if(profile.getActivePunishment(Punishment.Type.MUTE) != null) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot chat as you are muted.");
            return;
        }

        if (Arrays.stream(event.getMessage().split("")).map(isInvalid::matcher).anyMatch(Matcher::matches)) {
            event.setCancelled(true);
            player.sendMessage(Colors.get("&cYou may not use unicode characters in chat."));
            return;
        }

        String filteredReason = plugin.getFilter().isFiltered(event.getMessage());
        if (filteredReason != null) {
            event.setCancelled(true);

            player.sendMessage("");
            player.sendMessage(Colors.get("&cYour message: \"" + event.getMessage() + "\" has been filtered for " + filteredReason + ". Attempting to bypass the filter will result in punishment."));
            player.sendMessage("");

            // Add here a stacked punishment for Foul Language.
        }
    }
}
