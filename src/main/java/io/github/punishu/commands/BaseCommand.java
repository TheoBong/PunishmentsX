package io.github.punishu.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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

    protected abstract void execute(CommandSender sender, String[] args, String alias);
}
