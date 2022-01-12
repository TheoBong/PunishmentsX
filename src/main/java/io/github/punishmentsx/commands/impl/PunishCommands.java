package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PunishCommands extends BaseCommand {

    private final PunishmentsX plugin;

    public PunishCommands(PunishmentsX plugin, String name) {
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
            Profile targetProfile = PlayerUtil.findPlayer(plugin, args[0]);

            if (targetProfile == null) {
                sender.sendMessage("Player has never logged on the server!");
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
            String issuerName = Locale.CONSOLE_NAME.format(plugin);
            if(sender instanceof Player) {
                Player player = (Player) sender;

                issuerName = player.getName();
                issuer = player.getUniqueId();

            }

            Punishment punishment = targetProfile.getActivePunishment(punishmentType);
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

                targetProfile.punish(punishmentType, "MANUAL", issuer, sb.toString(), null, silent);

                sender.sendMessage(Locale.PUNISHMENT_SUCCESS.format(plugin)
                        .replace("%type%", punishmentType.pastMessage())
                        .replace("%target%", targetProfile.getName())
                        .replace("%reason%", sb.toString()));
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type.");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <target> <reason>");
        }
    }
}
