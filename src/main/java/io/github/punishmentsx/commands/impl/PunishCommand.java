package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.menus.PunishMenu;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.PlayerUtil;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class PunishCommand extends BaseCommand {
    private final PunishmentsX plugin;
    private final Configuration config;

    private String notes = null;

    public PunishCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.setAliases("p");
    }

    @Override
    protected void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission(Locale.PUNISH_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        if(sender instanceof Player) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /punish <player> [notes]");
                return;
            }

            ThreadUtil.runTask(true, plugin, () -> {
                if (args.length > 1) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        String s = args[i];
                        sb.append(args[i]);
                        if (i + 1 != args.length) {
                            sb.append(" ");
                        }
                    }
                    notes = sb.toString();
                } else {
                    notes = "None";
                }

                Profile targetProfile = PlayerUtil.findPlayer(plugin, args[0]);

                if (targetProfile == null) {
                    sender.sendMessage("Player has never logged on the server!");
                    sender.sendMessage("Names are case-sensitive for offline players!");
                    return;
                }

                PunishMenu.openPunishMenu(plugin, (Player) sender, targetProfile, notes);
            });
        }
    }
}