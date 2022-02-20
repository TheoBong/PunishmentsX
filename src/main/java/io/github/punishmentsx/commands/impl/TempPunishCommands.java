package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempPunishCommands extends BaseCommand {

    private final PunishmentsX plugin;

    public TempPunishCommands(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;

        this.setAliases("tempban", "tban", "tempmute", "tmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {

        String label = alias.toLowerCase();
        if(args.length > 2) {
            Profile targetProfile = getProfile(sender, plugin, args[0]);
            if (targetProfile == null) {
                return;
            }

            Punishment.Type punishmentType;
            switch (label) {
                case "tempban":
                case "tban":
                    punishmentType = Punishment.Type.BAN;
                    break;
                case "tempmute":
                case "tmute":
                    punishmentType = Punishment.Type.MUTE;
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Available commands: /tempban, /tempmute.");
                    return;
            }

            if (!sender.hasPermission(punishmentType.permission(plugin))) {
                sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
                return;
            }

            UUID issuer = null;
            if(sender instanceof Player) {
                Player player = (Player) sender;
                issuer = player.getUniqueId();
            }

            Pattern p = Pattern.compile("[a-z]+|\\d+");
            Matcher m = p.matcher(args[1].toLowerCase());

            int time = -1;
            String type = null;
            boolean b = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            while (m.find()) {
                String a = m.group();
                try {
                    time = Integer.parseInt(a);
                    if(time < 1) {
                        time = -1;
                    }
                } catch(NumberFormatException e) {
                    type = a;
                }

                if(time > 0 && type != null) {
                    switch(type) {
                        case "seconds": case "second": case "sec": case "s":
                            calendar.add(Calendar.SECOND, time);
                            break;
                        case "minutes": case "minute": case "m":
                            calendar.add(Calendar.MINUTE, time);
                            break;
                        case "hours": case "hrs": case "hr": case "h":
                            calendar.add(Calendar.HOUR, time);
                            break;
                        case "days": case "day": case "d":
                            calendar.add(Calendar.HOUR, time * 24);
                            break;
                        case "weeks": case "week": case "w":
                            calendar.add(Calendar.HOUR, time * 24 * 7);
                            break;
                        case "months": case "month": case "mo":
                            calendar.add(Calendar.MONTH, time);
                            break;
                    }

                    b = true;
                    time = -1;
                    type = null;
                }
            }

            Punishment punishment = targetProfile.getActivePunishment(punishmentType);
            if(punishment == null) {
                StringBuilder sb = new StringBuilder();
                boolean silent = false;
                for(int i = 2; i < args.length; i++) {
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

                if(b) {
                    targetProfile.punish(punishmentType, "MANUAL", issuer, sb.toString(), calendar.getTime(), silent);

                    sender.sendMessage(Locale.PUNISHMENT_SUCCESS.format(plugin)
                            .replace("%type%", punishmentType.pastMessage())
                            .replace("%target%", targetProfile.getName())
                            .replace("%reason%", sb.toString()));
                } else {
                    sender.sendMessage(ChatColor.RED + "You did not specify a valid timeframe.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type.");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <target> <timeframe> <reason> [-s]");
        }
    }
}
