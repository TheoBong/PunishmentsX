package io.github.punishmentsx.punishments;

import io.github.punishmentsx.ConfigValues;
import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.utils.Notifications;
import io.github.punishmentsx.utils.PlayerUtil;
import io.github.punishmentsx.utils.TimeUtil;
import io.github.punishmentsx.utils.WebHook;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.leuo.gooey.button.Button;

import java.util.*;
import java.util.List;

public @Data class Punishment {

    public enum Type {
        BAN, BLACKLIST, KICK, MUTE, WARN;

        public String pastMessage() {
            switch(this) {
                case BAN:
                    return "banned";
                case BLACKLIST:
                    return "blacklisted";
                case KICK:
                    return "kicked";
                case MUTE:
                    return "muted";
                case WARN:
                    return "warned";
                default:
                    return "null";
            }
        }

        public String permission(PunishmentsX plugin) {
            switch(this) {
                case BAN:
                    return Locale.BAN_PERMISSION.format(plugin);
                case BLACKLIST:
                    return Locale.BLACKLIST_PERMISSION.format(plugin);
                case KICK:
                    return Locale.KICK_PERMISSION.format(plugin);
                case MUTE:
                    return Locale.MUTE_PERMISSION.format(plugin);
                case WARN:
                    return Locale.WARN_PERMISSION.format(plugin);
                default:
                    return "null";
            }
        }

        public Button getButton(PunishmentsX plugin, Punishment punishment) {
            String uuid = punishment.getUuid().toString();
            switch (this) {
                case BLACKLIST:
                    return new Button(Material.valueOf(Locale.HISTORY_BLACKLIST_MATERIAL.format(plugin)),
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
                case BAN:
                    return new Button(Material.valueOf(Locale.HISTORY_BAN_MATERIAL.format(plugin)),
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
                case MUTE:
                    return new Button(Material.valueOf(Locale.HISTORY_MUTE_MATERIAL.format(plugin)),
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
                case KICK:
                    return new Button(Material.valueOf(Locale.HISTORY_KICK_MATERIAL.format(plugin)),
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
                case WARN:
                    return new Button(Material.valueOf(Locale.HISTORY_WARN_MATERIAL.format(plugin)),
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
                default:
                    return new Button(Material.WOOD_SWORD,
                        punishment.isActive() ? Locale.HISTORY_ACTIVE_NAME.format(plugin).replace("%uuid%", uuid) : Locale.HISTORY_INACTIVE_NAME.format(plugin).replace("%uuid%", uuid));
            }
        }

        public void action(PunishmentsX plugin, String expiry, String duration, String issueReason, Player player) {
            List<String> list = new ArrayList<>();
            switch (this) {
                case BAN:
                    for (String string : Locale.BAN_MESSAGE.formatLines(plugin)) {
                        list.add(string
                                .replace("%expirationDate%", expiry)
                                .replace("%expiry%", duration)
                                .replace("%reason%", issueReason));
                    }
                    player.kickPlayer(String.join("\n", list));
                    break;
                case BLACKLIST:
                    for (String string : Locale.BLACKLIST_MESSAGE.formatLines(plugin)) {
                        list.add(string.replace("%reason%", issueReason));
                    }
                    player.kickPlayer(String.join("\n", list));
                    break;
                case MUTE:
                    for (String string : Locale.MUTE_MESSAGE.formatLines(plugin)) {
                        list.add(string
                                .replace("%expirationDate%", expiry)
                                .replace("%expiry%", duration)
                                .replace("%reason%", issueReason));
                    }
                    player.sendMessage(String.join("\n", list));
                    break;
                case KICK:
                    for (String string : Locale.KICK_MESSAGE.formatLines(plugin)) {
                        list.add(string.replace("%reason%", issueReason));
                    }
                    player.kickPlayer(String.join("\n", list));
                    break;
                case WARN:
                    for (String string : Locale.WARN_MESSAGE.formatLines(plugin)) {
                        list.add(string.replace("%reason%", issueReason));
                    }
                    player.sendMessage(String.join("\n", list));
                    break;
            }
        }
    }

    private final PunishmentsX plugin;
    private final UUID uuid;
    private UUID victim, issuer, pardoner;
    private String stack, issueReason, pardonReason;
    private Date issued, expires, pardoned;
    private Type type;
    private boolean silentIssue, silentPardon;

    public String expiry() {
        if (expires == null) {
            return "Never";
        } else {
            return expires.toString();
//            DateFormat outputFormat;
//
//            switch (plugin.getConfig().getString("GENERAL.DATE_FORMAT")) {
//                case "AMERICAN":
//                    outputFormat = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
//                    return outputFormat.format(expires);
//                case "EUROPEAN":
//                    outputFormat = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a");
//                    return outputFormat.format(expires);
//                default:
//                    return expires.toString();
//            }
        }
    }
    
    public String duration() {
        if (expires == null) {
            return "Permanent";
        } else {
            return TimeUtil.formatTimeMillis(expires.getTime() - System.currentTimeMillis());
        }
    }

    public String originalDuration() {
        if (expires == null) {
            return "Permanent";
        } else {
            return TimeUtil.formatTimeMillis(expires.getTime() - issued.getTime());
        }
    }

    public boolean isActive() {
        boolean b = true;

        if(expires != null) {
            if(expires.before(new Date())) {
                b = false;
            }
        }

        if(pardoned != null) {
            b = false;
        }

        return b;
    }

    @SuppressWarnings("unchecked")
    public void execute() {
        Player player = Bukkit.getPlayer(victim);

        String victimName = null, issuerName;

        if(isActive()) {
            if (issuer != null) {
                Player p = Bukkit.getPlayer(issuer);
                issuerName = p.getName();
            } else {
                issuerName = ConfigValues.CONSOLE_NAME.format(plugin);
            }
        } else {
            if (pardoner != null) {
                Player p = Bukkit.getPlayer(pardoner);
                issuerName = p.getName();
            } else {
                issuerName = ConfigValues.CONSOLE_NAME.format(plugin);
            }
        }

        if(player != null && player.isOnline()) {
            victimName = player.getName();
            if(isActive()) {
                type.action(plugin, expiry(), originalDuration(), issueReason, player);
            }
        } else {
            Profile victimProfile = PlayerUtil.findPlayer(plugin, victim);
            if (victimProfile != null) {
                victimName = victimProfile.getName();
            }
        }

        String hover;
        if (isActive()) {
            List<String> list = new ArrayList<>();
            for (String string : Locale.PUNISHMENT_HOVER.formatLines(plugin)) {
                list.add(string
                        .replace("%type%", StringUtils.capitalize(type.toString().toLowerCase()))
                        .replace("%duration%", originalDuration())
                        .replace("%silentPrefix%", silentIssue ? Locale.SILENT_PREFIX.format(plugin) : "")
                        .replace("%victimName%", victimName)
                        .replace("%issuerName%", issuerName)
                        .replace("%expiry%", expiry())
                        .replace("%reason%", issueReason));
            }

            hover = String.join("\n", list);
            WebHook.sendWebhook(plugin, victim, uuid, originalDuration(), stack, type.pastMessage(), victimName, issueReason, issuerName, null, expiry());
        } else {
            List<String> list = new ArrayList<>();
            for (String string : Locale.UNPUNISHMENT_HOVER.formatLines(plugin)) {
                list.add(string
                        .replace("%type%", StringUtils.capitalize(type.toString().toLowerCase()))
                        .replace("%silentPrefix%", silentPardon ? Locale.SILENT_PREFIX.format(plugin) : "")
                        .replace("%victimName%", victimName)
                        .replace("%reason%", issueReason)
                        .replace("%issuerName%", issuerName)
                        .replace("%pardonReason%", pardonReason));
            }

            hover = String.join("\n", list);
            WebHook.sendWebhook(plugin, victim, uuid, originalDuration(), stack, "un" + type.pastMessage(), victimName, issueReason, issuerName, pardonReason, null);
        }

        String typeString = type.equals(Type.KICK) || type.equals(Type.WARN) ? type.pastMessage() : (isActive() ? (expires == null ? "permanently " : "temporarily ") : "un") + type.pastMessage();

        String message = Locale.BROADCAST.format(plugin)
                .replace("%duration%", originalDuration())
                .replace("%silentPrefix%", silentIssue ? Locale.SILENT_PREFIX.format(plugin) : "")
                .replace("%expiry%", expiry())
                .replace("%reason%", issueReason)
                .replace("%target%", victimName)
                .replace("%type%", typeString)
                .replace("%issuer%", issuerName);

        boolean silent = this.isActive() ? silentIssue : silentPardon;
        Notifications.sendMessage(plugin, silent, message, hover);
    }

    @Deprecated
    public void importFromDocument(Document d) {
        setVictim(d.get("victim", UUID.class));
        setIssuer(d.get("issuer", UUID.class));
        setPardoner(d.get("pardoner", UUID.class));

        setStack(d.getString("stack"));
        setIssueReason(d.getString("issue_reason"));
        setPardonReason(d.getString("pardon_reason"));
        setIssued(d.getDate("issued"));
        setExpires(d.getDate("expires"));
        setPardoned(d.getDate("pardoned"));
        setType(Type.valueOf(d.getString("type")));
        setSilentIssue(d.getBoolean("silent_issue"));
        setSilentPardon(d.getBoolean("silent_pardon"));
    }
}
