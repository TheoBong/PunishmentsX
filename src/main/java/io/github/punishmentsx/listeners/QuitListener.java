package io.github.punishmentsx.listeners;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final PunishmentsX plugin;
    public QuitListener(PunishmentsX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    public void onDisconnect(Player player) {
        Profile profile = plugin.getProfileManager().get(player.getUniqueId());

        if (profile == null) {
            return;
        }

        plugin.getProfileManager().push(true, profile, true);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onDisconnect(event.getPlayer());
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        onDisconnect(event.getPlayer());
        event.setLeaveMessage(null);
    }
}
