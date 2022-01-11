package io.github.punishu.commands.impl;

import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.commands.BaseCommand;
import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;
import io.github.punishu.utils.Colors;
import io.github.punishu.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PunishCommands extends BaseCommand {

    private final PunishU plugin;

    public PunishCommands(PunishU plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("ban", "blacklist", "kick", "mute", "warn");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission(Locale.MANUAL_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        String label = alias.toLowerCase();
        if(args.length > 1) {

            Player target = Bukkit.getPlayer(args[0]);
            Profile profile;
            if(target != null) {
                profile = plugin.getProfileManager().get(target.getUniqueId());
            } else {
                WebPlayer wp = new WebPlayer(args[0]);
                if(wp.isValid()) {
                    profile = plugin.getProfileManager().find(wp.getUuid(), false);
                } else {
                    sender.sendMessage(ChatColor.RED + "The target you specified does not exist.");
                    return;
                }
            }

            if (profile == null) {
                sender.sendMessage(ChatColor.RED + "The target you specified has never joined the server.");
                return;
            }

            Punishment.Type punishmentType;
            switch (label) {
                case "ban":
                    punishmentType = Punishment.Type.BAN;
                    break;
                case "blacklist":
                    punishmentType = Punishment.Type.BLACKLIST;
                    break;
                case "kick":
                    punishmentType = Punishment.Type.KICK;
                    break;
                case "mute":
                    punishmentType = Punishment.Type.MUTE;
                    break;
                case "warn":
                    punishmentType = Punishment.Type.WARN;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Available commands: /ban, /blacklist, /kick, /mute, /warn.");
                    return;
            }

            UUID issuer = null;
            String issuerName = "&4Console";
            if(sender instanceof Player) {
                Player player = (Player) sender;

                issuerName = player.getName();
                issuer = player.getUniqueId();

            }

            Punishment punishment = profile.getActivePunishment(punishmentType);
            if(punishment == null || punishmentType.equals(Punishment.Type.KICK) || punishmentType.equals(Punishment.Type.WARN)) {
                StringBuilder sb = new StringBuilder();
                boolean silent = false;
                for(int i = 1; i < args.length; i++) {
                    String s = args[i];
                    if(s.equalsIgnoreCase("-s")) {
                        silent = true;
                    } else {
                        sb.append(args[i]);
                        if (i + 1 != args.length) {
                            sb.append(" ");
                        }
                    }
                }

                profile.punish(punishmentType, "MANUAL", issuer, sb.toString(), null, silent);

                sender.sendMessage(Colors.get("&aYou have " + punishmentType.pastMessage() + " " + profile.getName() + " for:&f " + sb.toString() + "."));
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type.");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <target> <reason>");
        }
    }
}
