package io.github.punishmentsx.menus;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.Colors;
import io.github.punishmentsx.utils.PlayerUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import xyz.leuo.gooey.button.Button;
import xyz.leuo.gooey.gui.PaginatedGUI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class HistoryMenu {
    public static void openHistoryMenu(PunishmentsX plugin, Player staff, Profile targetProfile, String notes) {
        PaginatedGUI gui = new PaginatedGUI(Locale.HISTORY_TITLE.format(plugin).replace("%player%", targetProfile.getName()), 27);

        gui.setButton(0, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(1, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(2, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(3, new Button(Material.STAINED_GLASS_PANE, ""));
        if (notes != null) {
            Button backButton = new Button(Material.REDSTONE, "&c&lBack");
            backButton.setLore("&fClick to return to punish menu");
            backButton.setButtonAction((player, gui1, b, event) -> {
                player.closeInventory();
                PunishMenu.openPunishMenu(plugin, staff, targetProfile, notes);
            });
            gui.setButton(4, backButton);
        } else {
            gui.setButton(4, new Button(Material.STAINED_GLASS_PANE, ""));
        }
        gui.setButton(5, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(6, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(7, new Button(Material.STAINED_GLASS_PANE, ""));
        gui.setButton(8, new Button(Material.STAINED_GLASS_PANE, ""));

        List<Punishment> punishments = targetProfile.getPunishmentsHistory();
        TreeMap<Date, Punishment> map = new TreeMap<>();

        for(Punishment p : punishments) {
            if(p.isActive()) {
                gui.addButton(createButton(plugin, p));
                continue;
            }

            map.put(p.getIssued(), p);
        }

        for(Punishment p : map.descendingMap().values()) {
            gui.addButton(createButton(plugin, p));
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            gui.open(staff);
        });
    }

    private static Button createButton(PunishmentsX plugin, Punishment punishment) {
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

        String issuerName = null;
        if (punishment.getIssuer() == null) {
            issuerName = Locale.CONSOLE_NAME.format(plugin);
        } else {
            Profile issuerProfile = PlayerUtil.findPlayer(plugin, punishment.getIssuer());
            if (issuerProfile != null) {
                issuerName = issuerProfile.getName();
            }
        }

        String victimName = null;
        Profile victimProfile = PlayerUtil.findPlayer(plugin, punishment.getVictim());
        if (victimProfile != null) {
            victimName = victimProfile.getName();
        }

        if (punishment.getPardoned() != null) {
            String pardonerName = null;

            if (punishment.getPardoner() == null) {
                pardonerName = Locale.CONSOLE_NAME.format(plugin);
            } else {
                Profile pardonerProfile = PlayerUtil.findPlayer(plugin, punishment.getPardoner());
                if (pardonerProfile != null) {
                    pardonerName = pardonerProfile.getName();
                }
            }

            ConfigurationSection section = plugin.getConfig().getConfigurationSection("MENUS.HISTORY.PARDONED");
            List<String> lore = new ArrayList<>();
            for (String string : section.getStringList("LORE")) {
                lore.add(Colors.get(string
                        .replace("%victim%", victimName)
                        .replace("%type%", punishment.getType() + "")
                        .replace("%duration%", punishment.duration())
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
                        .replace("%duration%", punishment.duration())
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
