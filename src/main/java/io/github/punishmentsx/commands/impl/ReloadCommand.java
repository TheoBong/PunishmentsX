package io.github.punishmentsx.commands.impl;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.commands.BaseCommand;
import io.github.punishmentsx.utils.Colors;
import io.github.punishmentsx.utils.ThreadUtil;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends BaseCommand {

    private final PunishmentsX plugin;

    public ReloadCommand(PunishmentsX plugin, String name) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String alias) {
        if (!sender.hasPermission(Locale.RELOAD_PERMISSION.format(plugin))) {
            sender.sendMessage(Locale.NO_PERMISSION.format(plugin));
            return;
        }

        ThreadUtil.runTask(true, plugin, () -> {
            plugin.reloadMessages();
            sender.sendMessage(Colors.convertLegacyColors("&aReloading messages.yml file!"));
        });
    }
}
