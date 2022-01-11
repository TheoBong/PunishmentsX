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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempPunishCommands extends BaseCommand {

    private final PunishU plugin;

    public TempPunishCommands(PunishU plugin, String name) {
        super(name);
        this.plugin = plugin;

        this.setAliases("tempban", "tban", "tempmute", "tmute");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission(Locale.MANUAL_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        String label = alias.toLowerCase();
        if(args.length > 2) {
            Player target = Bukkit.getPlayer(args[0]);
            Profile profile = null;
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

            Punishment punishment = profile.getActivePunishment(punishmentType);
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
                    profile.punish(punishmentType, "MANUAL", issuer, sb.toString(), calendar.getTime(), silent);
                    sender.sendMessage(Colors.get("&aYou have temporarily " + punishmentType.pastMessage() + " " + profile.getName() + " for:&f " + sb.toString() + "."));
                } else {
                    sender.sendMessage(ChatColor.RED + "You did not specify a valid timeframe.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "The target you specified already has an active punishment of that type.");
            }

        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <target> <timeframe> <reason>");
        }
    }
}
