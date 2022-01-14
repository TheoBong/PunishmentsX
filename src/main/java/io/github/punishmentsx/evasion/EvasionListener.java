package io.github.punishmentsx.evasion;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.mongo.MongoUpdate;
import io.github.punishmentsx.profiles.Profile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EvasionListener implements Listener {
    private final PunishmentsX plugin;

    public EvasionListener(PunishmentsX plugin) {
        this.plugin = plugin;
        plugin.registerListener(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        UUID uuid = event.getUniqueId();
        String ip = event.getAddress().getHostAddress();

        plugin.getMongo().getDocument(false, "profiles", "_id", uuid, document -> {
            if (document != null) {
                List<String> list = document.getList("ip_history", String.class);
                if (!list.contains(ip)) {
                    list.add(ip);

                    MongoUpdate mu = new MongoUpdate("profiles", uuid);
                    Map<String, Object> map = new HashMap<>();
                    map.put("ip_history", list);

                    mu.setUpdate(map);
                    plugin.getMongo().massUpdate(false, mu);
                }
            }
        });

        EvasionCheck evasionCheck = new EvasionCheck(uuid, ip, plugin, false);
        String message = evasionCheck.getMessage();

        if (message != null) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);

            if (plugin.getProfileManager().getProfiles().containsKey(uuid)) {
                Profile profile = plugin.getProfileManager().get(uuid);
                plugin.getProfileManager().push(true, profile, true);
            }
        }
    }
}

