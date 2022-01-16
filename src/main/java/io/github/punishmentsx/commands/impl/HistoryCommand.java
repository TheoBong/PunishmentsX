package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.menus.HistoryMenu;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.PlayerUtil;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HistoryCommand extends BaseCommand {

    private final PunishmentsX plugin;

    public HistoryCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setAliases("c", "checkpunishments");
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (sender instanceof Player && !sender.hasPermission(Locale.HISTORY_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        if (sender instanceof Player) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /history <player>");
                return;
            }

            ThreadUtil.runTask(true, plugin, () -> {
                Profile targetProfile = PlayerUtil.findPlayer(plugin, args[0]);

                if (targetProfile == null) {
                    sender.sendMessage("Player has never logged on the server!");
                    sender.sendMessage("Names are case-sensitive for offline players!");
                    return;
                }

                HistoryMenu.openHistoryMenu(plugin, (Player) sender, targetProfile, null);
            });
        }
    }
}
