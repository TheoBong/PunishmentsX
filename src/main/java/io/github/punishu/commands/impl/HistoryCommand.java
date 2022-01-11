package io.github.punishu.commands.impl;

import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.commands.BaseCommand;
import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;
import io.github.punishu.utils.Colors;
import io.github.punishu.utils.ThreadUtil;
import io.github.punishu.utils.WebPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import xyz.leuo.gooey.button.Button;
import xyz.leuo.gooey.gui.PaginatedGUI;

import java.util.*;

public class HistoryCommand extends BaseCommand {

    private final PunishU plugin;

    public HistoryCommand(PunishU plugin, String name) {
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

        if(sender instanceof Player) {
            if (args.length != 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /c <player>");
                return;
            }

            ThreadUtil.runTask(true, plugin, () -> {
                Player staff = (Player) sender;
                UUID uuid;
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    WebPlayer webPlayer = new WebPlayer(args[0]);
                    uuid = webPlayer.getUuid();
                } else {
                    uuid = target.getUniqueId();
                }

                Profile profile;
                profile = plugin.getProfileManager().get(uuid);
                if (profile == null) {
                    profile = plugin.getProfileManager().find(uuid, false);
                }

                PaginatedGUI gui = new PaginatedGUI(Locale.HISTORY_TITLE.format(plugin).replace("%player%", profile.getName()), 18);

                List<Punishment> punishments = profile.getPunishmentsHistory();
                TreeMap<Date, Punishment> map = new TreeMap<>();

                for(Punishment p : punishments) {
                    if(p.isActive()) {
                        gui.addButton(createButton(p));
                        continue;
                    }

                    map.put(p.getIssued(), p);
                }

                for(Punishment p : map.descendingMap().values()) {
                    gui.addButton(createButton(p));
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    gui.open(staff);
                });
            });
        }
    }

    public Button createButton(Punishment punishment) {
        String uuid = punishment.getUuid().toString();
        
        Button button;
        switch (punishment.getType()) {
            case BLACKLIST: button = new Button(Material.valueOf(Locale.HISTORY_BLACKLIST_MATERIAL.format(plugin)),
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
            case BAN: button = new Button(Material.valueOf(Locale.HISTORY_BAN_MATERIAL.format(plugin)),
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
            case MUTE: button = new Button(Material.valueOf(Locale.HISTORY_MUTE_MATERIAL.format(plugin)),
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
            case KICK: button = new Button(Material.valueOf(Locale.HISTORY_KICK_MATERIAL.format(plugin)),
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
            case WARN: button = new Button(Material.valueOf(Locale.HISTORY_WARN_MATERIAL.format(plugin)),
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
            default: button = new Button(Material.WOOD_SWORD,
                    punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid)); break;
        }

        if(punishment.isActive()) {
            button.getMeta().addEnchant(Enchantment.DURABILITY, 1, true);
        }

        String issuerName;
        if (punishment.getIssuer() == null) {
            issuerName = Locale.CONSOLE_NAME.format(plugin);
        } else {
            Player issuer = Bukkit.getPlayer(punishment.getIssuer());
            if (issuer != null) {
                issuerName = issuer.getName();
            } else {
                WebPlayer wp = new WebPlayer(punishment.getIssuer());
                issuerName = wp.getName();
            }
        }

        String victimName;
        Player victim = Bukkit.getPlayer(punishment.getVictim());
        if (victim != null) {
            victimName = victim.getName();
        } else {
            WebPlayer wp = new WebPlayer(punishment.getVictim());
            victimName = wp.getName();
        }

        if (punishment.getPardoned() != null) {
            String pardonerName;

            if (punishment.getPardoner() == null) {
                pardonerName = Locale.CONSOLE_NAME.format(plugin);
            } else {
                Player pardoner = Bukkit.getPlayer(punishment.getPardoner());
                if (pardoner != null) {
                    pardonerName = pardoner.getName();
                } else {
                    WebPlayer wp = new WebPlayer(punishment.getPardoner());
                    pardonerName = wp.getName();
                }
            }

            ConfigurationSection section = plugin.getConfig().getConfigurationSection("MENUS.HISTORY.PARDONED");
            List<String> lore = new ArrayList<>();
            for (String string : section.getStringList("LORE")) {
                lore.add(Colors.get(string
                        .replace("%victim%", victimName)
                        .replace("%type%", punishment.getType() + "")
                        .replace("%stack%", punishment.getStack())
                        .replace("%active%", punishment.isActive() + "")
                        .replace("%issuedDate%", punishment.getIssued().toString())
                        .replace("%issuer%", issuerName)
                        .replace("%issueReason%", punishment.getIssueReason())
                        .replace("%expiry%", punishment.expiry())
                        .replace("%pardonDate%", punishment.getPardoned() + "")
                        .replace("%pardoner%", pardonerName)
                        .replace("%pardonReason%", punishment.getPardonReason())));
            }
            button.setLore(lore);
        } else {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("MENUS.HISTORY.REGULAR");
            List<String> lore = new ArrayList<>();
            for (String string : section.getStringList("LORE")) {
                lore.add(Colors.get(string
                        .replace("%victim%", victimName)
                        .replace("%type%", punishment.getType() + "")
                        .replace("%stack%", punishment.getStack())
                        .replace("%active%", punishment.isActive() + "")
                        .replace("%issuedDate%", punishment.getIssued().toString())
                        .replace("%issuer%", issuerName)
                        .replace("%issueReason%", punishment.getIssueReason())
                        .replace("%expiry%", punishment.expiry())));
            }
            button.setLore(lore);
        }
        button.setCloseOnClick(false);

        return button;
    }
}
