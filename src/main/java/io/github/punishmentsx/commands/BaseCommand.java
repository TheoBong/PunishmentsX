package io.github.punishmentsx.commands;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.PlayerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;

public abstract class BaseCommand extends Command {

    public BaseCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        execute(sender, args, alias);
        return true;
    }

    public void setAliases(final String... aliases) {
        if (aliases.length > 0) {
            this.setAliases(aliases.length == 1 ? Collections.singletonList(aliases[0]) : Arrays.asList(aliases));
        }
    }

    public Player getPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            sender.sendMessage("Only players!");
            return null;
        }
    }

    public Profile getProfile(CommandSender sender, PunishmentsX plugin, String player) {
        Profile targetProfile = PlayerUtil.findPlayer(plugin, player);

        if (targetProfile == null) {
            sender.sendMessage("Player has never logged on the server!");
            return null;
        }

        return targetProfile;
    }

    protected abstract void execute(CommandSender sender, String[] args, String alias);
}
