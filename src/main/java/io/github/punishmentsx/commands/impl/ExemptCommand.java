package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.database.mongo.MongoUpdate;
import io.github.punishmentsx.evasion.EvasionCheck;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.PlayerUtil;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ExemptCommand extends BaseCommand {

    private final PunishmentsX plugin;

    public ExemptCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (!sender.hasPermission(Locale.HISTORY_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /exempt <player>");
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            Profile targetProfile = PlayerUtil.findPlayer(plugin, args[0]);

            if (targetProfile == null) {
                sender.sendMessage("Player has never logged on the server!");
                sender.sendMessage("Names are case-sensitive for offline players!");
                return;
            }

            UUID uuid = targetProfile.getUuid();

            EvasionCheck evasionCheck = new EvasionCheck(uuid, targetProfile.getCurrentIp(), plugin, true);
            String message = evasionCheck.getMessage();
            UUID punishment = evasionCheck.getPunishment();

            if (message == null) {
                sender.sendMessage("Target should be able to join!");
                return;
            }

            plugin.getMongo().getDocument(false, "punishments", "_id", punishment, document -> {
                if (document != null) {
                    List<UUID> list = document.getList("exemptions", UUID.class) == null ? new ArrayList<>() : document.getList("exemptions", UUID.class);

                    if (!list.contains(uuid)) {
                        list.add(uuid);

                        MongoUpdate mu = new MongoUpdate("punishments", punishment);
                        Map<String, Object> map = new HashMap<>();
                        map.put("exemptions", list);

                        mu.setUpdate(map);
                        plugin.getMongo().massUpdate(false, mu);
                        sender.sendMessage("Added exemption.");
                    }
                }
            });
        });
    }
}