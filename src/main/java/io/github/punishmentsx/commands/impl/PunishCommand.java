package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.menus.PunishMenu;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PunishCommand extends BaseCommand {
    private final PunishmentsX plugin;

    private String notes = null;

    public PunishCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("p");
    }

    @Override
    protected void execute(CommandSender sender, String[] args, String alias) {
        if (!sender.hasPermission(Locale.PUNISH_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        if (getPlayer(sender) == null) {
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: / " + getName() + " <player> [notes]");
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            if (args.length > 1) {
                StringBuilder sb = new StringBuilder();
                for(int i = 1; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i + 1 != args.length) {
                        sb.append(" ");
                    }
                }
                notes = sb.toString();
            } else {
                notes = "None";
            }

            Profile targetProfile = getProfile(sender, plugin, args[0]);
            if (targetProfile == null) {
                return;
            }

            PunishMenu.openPunishMenu(plugin, (Player) sender, targetProfile, notes);
        });
    }
}
