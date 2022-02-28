package io.github.punishmentsx.commands.impl;


import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CmdPunishCommand extends BaseCommand {
    private final PunishmentsX plugin;

    private String notes = null;

    public CmdPunishCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("cmdp");
    }

    @Override
    protected void execute(CommandSender sender, String[] args, String alias) {
        if (!sender.hasPermission(Locale.PUNISH_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /cmdpunish <player> <stack> [notes] [-s]");
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            if (args.length > 2) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i + 1 != args.length) {
                        sb.append(" ");
                    }
                }
                notes = sb.toString();
            } else {
                notes = "None";
            }

            boolean silent = false;
            for(int i = 1; i < args.length; i++) {
                String s = args[i];
                if (s.equalsIgnoreCase("-s")) {
                    silent = true;
                    break;
                }
            }

            Profile targetProfile = getProfile(sender, plugin, args[0]);
            if (targetProfile == null) {
                return;
            }

            ConfigurationSection section = plugin.getMessagesFile().getConfigurationSection(args[1]);
            if (section == null) {
                sender.sendMessage("Invalid stack! (Case sensitive, try all caps!)");
                return;
            }

            if (section.getString("PERMISSION") != null && !sender.hasPermission(section.getString("PERMISSION"))) {
                sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                return;
            }

            int offenses = Stackables.offenseNumber(targetProfile, section.getName());

            String reason = notes == null ? section.getString("DEFAULT_REASON") : notes;
            List<String> punishmentsList = section.getStringList("PUNISHMENTS");

            String punishmentString;
            try {
                punishmentString = punishmentsList.get(offenses);
            } catch (IndexOutOfBoundsException e) {
                punishmentString = punishmentsList.get(punishmentsList.size() - 1);
            }

            Punishment.Type type = TranslatePunishment.type(punishmentString);
            Date expiration = TranslatePunishment.expires(punishmentString);

            UUID issuerUUID = null;
            if (sender instanceof Player) {
                Player issuerPlayer = (Player) sender;
                issuerUUID = issuerPlayer.getUniqueId();
            }

            if (targetProfile.getActivePunishment(type) == null || type.equals(Punishment.Type.KICK) || type.equals(Punishment.Type.WARN)) {
                targetProfile.punish(type, section.getName(), issuerUUID, reason, expiration, silent);

                sender.sendMessage(Locale.PUNISHMENT_SUCCESS.format(plugin)
                        .replace("%type%", type.pastMessage())
                        .replace("%target%", targetProfile.getName())
                        .replace("%reason%", reason));
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type. You must unmute/unban that player first!");;
            }
        });
    }
}