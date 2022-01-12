package io.github.punishmentsx.utils;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerUtil {
    public static Profile findPlayer(PunishmentsX plugin, String target) {
        Player targetPlayer = Bukkit.getPlayer(target);
        Profile targetProfile;

        if (targetPlayer == null) {
            targetProfile = plugin.getProfileManager().find(target, false);
        } else {
            try {
                targetProfile = plugin.getProfileManager().get(targetPlayer.getUniqueId());
            } catch (Exception e) {
                return null;
            }
        }

        return targetProfile;
    }

    public static Profile findPlayer(PunishmentsX plugin, UUID uuid) {
        Player targetPlayer = Bukkit.getPlayer(uuid);
        Profile targetProfile;

        if (targetPlayer == null) {
            targetProfile = plugin.getProfileManager().find(uuid, false);
        } else {
            try {
                targetProfile = plugin.getProfileManager().get(targetPlayer.getUniqueId());
            } catch (Exception e) {
                return null;
            }
        }

        return targetProfile;
    }
}
