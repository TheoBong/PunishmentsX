package io.github.punishu.commands.impl;

import io.github.punishu.Locale;
import io.github.punishu.PunishU;
import io.github.punishu.commands.BaseCommand;
import io.github.punishu.profiles.Profile;
import io.github.punishu.punishments.Punishment;
import io.github.punishu.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import xyz.leuo.gooey.button.Button;
import xyz.leuo.gooey.gui.GUI;
import xyz.leuo.gooey.gui.GUIUpdate;

import java.util.*;

public class PunishCommand extends BaseCommand {
    private final PunishU plugin;
    private final Configuration config;

    private boolean silent = true;

    public PunishCommand(PunishU plugin, String name) {
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
                sender.sendMessage(ChatColor.RED + "Usage: /punish <player> <notes>");
                return;
            }

            ThreadUtil.runTask(true, plugin, () -> {
                Player staff = (Player) sender;
                UUID uuid;
                String targetName;
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    WebPlayer webPlayer = new WebPlayer(args[0]);
                    uuid = webPlayer.getUuid();
                    targetName = webPlayer.getName();
                } else {
                    uuid = target.getUniqueId();
                    targetName = target.getName();
                }

                Profile targetProfile;
                targetProfile = plugin.getProfileManager().get(uuid);
                if (targetProfile == null) {
                    targetProfile = plugin.getProfileManager().find(uuid, false);
                }

                String notes = null;
                if (args.length > 2) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 1; i < args.length; i++) {
                        String s = args[i];
                        sb.append(args[i]);
                        if (i + 1 != args.length) {
                            sb.append(" ");
                        }
                    }
                    notes = sb.toString();
                }

                GUI gui = new GUI(Locale.PUNISH_TITLE.format(plugin).replace("%player%", targetName), 45);
                silent = true;

                gui.setUpdate(new GUIUpdate() {
                    @Override
                    public void onUpdate(GUI gui) {
                        String name = silent ? "&a&lSilent Punishment (ON)" : "&C&lSilent Punishment (OFF)";

                        Button button3 = new Button(Material.FEATHER, name);
                        button3.setLore("&fClick to toggle silent punishments");
                        button3.setButtonAction((player, gui1, b, event) -> {
                            silent = !silent;
                            b.setName(silent ? "&a&lSilent Punishment (ON)" : "&C&lSilent Punishment (OFF)");
                            gui.update();
                        });
                        gui.setButton(3, button3);
                    }
                });

                gui.setButton(0, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(1, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(2, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(6, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(7, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(8, new Button(Material.STAINED_GLASS_PANE, ""));

                String name = silent ? "&a&lSilent Punishment (ON)" : "&C&lSilent Punishment (OFF)";

                Button button3 = new Button(Material.FEATHER, name);
                button3.setLore("&fClick to toggle silent punishments");
                button3.setButtonAction((player, gui1, b, event) -> {
                    silent = !silent;
                    b.setName(silent ? "&a&lSilent Punishment (ON)" : "&C&lSilent Punishment (OFF)");
                    gui.update();
                });
                gui.setButton(3, button3);

                Button button4 = new Button(Material.PAPER, "&c&lNotes");
                button4.setLore("&f" + notes);
                gui.setButton(4, button4);

                Button button5 = new Button(Material.BOOK_AND_QUILL, "&6&lHistory");
                button5.setLore("&fCheck the players punishment history.");
                button5.setButtonAction((player, gui1, b, event) -> {
                    player.closeInventory();
                    player.performCommand("history " + targetName);
                });
                gui.setButton(5, button5);

                gui.setButton(9, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(10, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(11, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(12, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(13, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(14, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(15, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(16, new Button(Material.STAINED_GLASS_PANE, ""));
                gui.setButton(17, new Button(Material.STAINED_GLASS_PANE, ""));

                for (String key : config.getConfigurationSection("MENUS.PUNISH.SLOTS").getKeys(false)) {
                    ConfigurationSection section = config.getConfigurationSection("MENUS.PUNISH.SLOTS." + key);
                    gui.addButton(createButton(targetProfile, staff, notes, section));
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    gui.open(staff);
                });
            });
        }
    }

    public Button createButton(Profile targetProfile, Player issuer, String notes, ConfigurationSection section) {
        Button button = new Button(Material.valueOf(section.getString("MATERIAL")), section.getString("NAME"));
        int offenses = Stackables.offenseNumber(targetProfile, section.getName());

        List<String> lore = new ArrayList<>();
        for (String string : section.getStringList("LORE")) {
            lore.add(Colors.get(string.replace("%offenses%", offenses + "")));
        }

        button.setLore(lore);

        String reason = notes == null ? section.getString("DEFAULT_REASON") : notes;
        List<String> punishmentsList = section.getStringList("PUNISHMENTS");

        String punishmentString;
        try {
            punishmentString = punishmentsList.get(offenses);
        } catch (IndexOutOfBoundsException e) {
            punishmentString = punishmentsList.get(punishmentsList.size() - 1);
        }

        Punishment.Type type = TranslatePunishment.type(punishmentString);
        Date expiration = TranslatePunishment.expires(punishmentString);

        button.setButtonAction((player, gui, b, event) -> {
            targetProfile.punish(type, section.getName(), issuer.getUniqueId(), reason, expiration, silent);
            if (expiration != null) {
                issuer.sendMessage(Colors.get("&aYou have temporarily " + type.pastMessage() + " " + targetProfile.getName() + " for: " + reason + "."));
            } else {
                issuer.sendMessage(Colors.get("&aYou have " + type.pastMessage() + " " + targetProfile.getName() + " for:&f " + reason + "."));
            }

        });
        return button;
    }
}
