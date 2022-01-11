package io.github.punishu.listeners;

import io.github.punishu.PunishU;
import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;
import io.github.punishu.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {
    private final PunishU plugin;

    public JoinListener(PunishU plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (plugin.getProfileManager().getProfiles().containsKey(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Your profile is already loaded, please relog!");

            Profile profile = plugin.getProfileManager().get(uuid);

            if (profile == null) {
                plugin.getProfileManager().getProfiles().remove(uuid);
            } else {
                plugin.getProfileManager().push(false, profile, true);
            }
        } else {
            Profile profile = plugin.getProfileManager().find(uuid, true);
            if (profile == null) {
                profile = plugin.getProfileManager().createProfile(uuid);
            }

            Punishment blacklist = profile.getActivePunishment(Punishment.Type.BLACKLIST);
            Punishment ban = profile.getActivePunishment(Punishment.Type.BAN);
            if (blacklist != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&4You are blacklisted!\n&fReason: " + blacklist.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
            }

            if (ban != null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        Colors.get("&cYou are banned (Expiry: " + ban.expiry() + ")!\n&fReason: " + ban.getIssueReason()));
                profile.addIp(event.getAddress().getHostAddress());
                plugin.getProfileManager().push(true, profile, true);
            }

            if (ban == null && blacklist == null) {
                profile.addIp(event.getAddress().getHostAddress());
                event.allow();
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        if(profile == null) {
            player.kickPlayer(ChatColor.RED + "Your profile did not load properly, please relog.");
            return;
        }

        profile.setName(player.getName());
        profile.addIp(player.getAddress().getAddress().getHostAddress());

        profile.update();
    }
}
