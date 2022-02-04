package io.github.punishmentsx.evasion;

import io.github.punishmentsx.Locale;
import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.Database;
import io.github.punishmentsx.database.mongo.MongoUpdate;
import io.github.punishmentsx.punishments.Punishment;
import io.github.punishmentsx.utils.DocumentUtil;
import io.github.punishmentsx.utils.TimeUtil;
import lombok.Data;
import org.bson.Document;

import java.util.*;

public @Data class EvasionCheck {
    
    private String message = null;
    private String why = null;
    private UUID punishment = null;
    private boolean exempt = false;

    public EvasionCheck(UUID uuid, String ip, PunishmentsX plugin) {
        evading(uuid, ip, plugin);
    }

    public void evading(UUID uuid, String ip, PunishmentsX plugin) {
        DocumentUtil documentUtil1 = new DocumentUtil(plugin, "iplog", "_id", ip);
        Document document1 = documentUtil1.getDocument();

        List<UUID> accounts;

        if (document1 == null) {
            accounts = new ArrayList<>();
            accounts.add(uuid);

            MongoUpdate mu = new MongoUpdate("iplog", ip);
            Map<String, Object> map = new HashMap<>();
            map.put("accounts", accounts);

            mu.setUpdate(map);
            plugin.getMongo().massUpdate(false, mu);
        } else {
            accounts = document1.getList("accounts", UUID.class);
        }

        if (!accounts.contains(uuid)) {
            accounts.add(uuid);

            MongoUpdate mu = new MongoUpdate("iplog", ip);
            Map<String, Object> map = new HashMap<>();
            map.put("accounts", accounts);

            mu.setUpdate(map);
            plugin.getMongo().massUpdate(false, mu);
        }

        for (UUID account : accounts) {
            DocumentUtil documentUtil2 = new DocumentUtil(plugin, "profiles", "_id", account);
            Document document2 = documentUtil2.getDocument();

            if (document2 == null) continue;

            List<UUID> punishments = document2.getList("punishments", UUID.class);
            List<String> ips = document2.getList("ip_history", String.class);

            for (UUID punishmentUUID : punishments) {
                DocumentUtil documentUtil3 = new DocumentUtil(plugin, "punishments", "_id", punishmentUUID);
                Document document3 = documentUtil3.getDocument();

                if (document3 == null) continue;

                Punishment punishment = new Punishment(plugin, punishmentUUID);
                punishment.importFromDocument(document3);

                String type = punishment.getType().toString();
                Date expires = punishment.getExpires();
                Date pardoned = punishment.getPardoned();
                String reason = punishment.getIssueReason();
                List<UUID> exemptions = document3.getList("exemptions", UUID.class);

                this.punishment = punishmentUUID;

                boolean exempted = false;
                if (exemptions != null && !exemptions.isEmpty()) {
                    if (exemptions.contains(uuid)) {
                        this.exempt = true;
                        exempted = true;
                    }
                }

                if (exempted) continue;
                if (!isActive(type, expires, pardoned)) continue;

                this.why = document2.getString("name") + " -> " + type + " : " + reason;

                List<String> list = new ArrayList<>();

                if (type.equals("BLACKLIST")) {
                    for (String string : Locale.BLACKLIST_MESSAGE.formatLines(plugin)) {
                        list.add(string
                                .replace("%reason%", reason));
                    }
                    this.message = String.join("\n", list);
                } else if (type.equals("BAN")) {
                    for (String string : Locale.BAN_MESSAGE.formatLines(plugin)) {
                        list.add(string
                                .replace("%expirationDate%", punishment.expiry())
                                .replace("%expiry%", punishment.duration())
                                .replace("%reason%", reason));
                    }
                    this.message = String.join("\n", list);
                }

                return;
            }

            if (!plugin.getConfig().getBoolean("ANTI_EVASION.LONG_ARMS")) continue;

            // Long-arms past this point!
            for (String ip1 : ips) {
                if (ip1.equals(ip)) {
                    continue;
                }

                DocumentUtil documentUtil4 = new DocumentUtil(plugin, "iplog", "_id", ip1);
                Document document4 = documentUtil4.getDocument();

                List<UUID> accounts1;

                if (document4 == null) {
                    accounts1 = new ArrayList<>();
                    accounts1.add(uuid);

                    MongoUpdate mu = new MongoUpdate("iplog", ip1);
                    Map<String, Object> map = new HashMap<>();
                    map.put("accounts", accounts1);

                    mu.setUpdate(map);
                    plugin.getMongo().massUpdate(false, mu);
                } else {
                    accounts1 = document4.getList("accounts", UUID.class);
                }

                if (!accounts1.contains(uuid)) {
                    accounts1.add(uuid);

                    MongoUpdate mu = new MongoUpdate("iplog", ip1);
                    Map<String, Object> map = new HashMap<>();
                    map.put("accounts", accounts1);

                    mu.setUpdate(map);
                    plugin.getMongo().massUpdate(false, mu);
                }

                for (UUID account1 : accounts1) {
                    DocumentUtil documentUtil5 = new DocumentUtil(plugin, "profiles", "_id", account1);
                    Document document5 = documentUtil5.getDocument();

                    if (document5 == null) continue;

                    List<UUID> punishments2 = document5.getList("punishments", UUID.class);

                    for (UUID punishmentUUID : punishments2) {
                        DocumentUtil documentUtil6 = new DocumentUtil(plugin, "punishments", "_id", punishmentUUID);
                        Document document6 = documentUtil6.getDocument();

                        Punishment punishment = new Punishment(plugin, punishmentUUID);
                        punishment.importFromDocument(document6);

                        String type = punishment.getType().toString();
                        Date expires = punishment.getExpires();
                        Date pardoned = punishment.getPardoned();
                        String reason = punishment.getIssueReason();
                        List<UUID> exemptions = document6.getList("exemptions", UUID.class);

                        this.punishment = punishmentUUID;

                        boolean exempted = false;
                        if (exemptions != null && !exemptions.isEmpty()) {
                            if (exemptions.contains(uuid)) {
                                this.exempt = true;
                                exempted = true;
                            }
                        }

                        if (exempted) continue;
                        if (!isActive(type, expires, pardoned)) continue;

                        this.why = "[LongArms] " + document2.getString("name") + " -> " + ip1 + " -> " + document5.getString("name") + " -> " + type + " : " + reason;

                        List<String> list = new ArrayList<>();

                        if (type.equals("BLACKLIST")) {
                            for (String string : Locale.BLACKLIST_MESSAGE.formatLines(plugin)) {
                                list.add(string
                                        .replace("%reason%", reason));
                            }
                            this.message = String.join("\n", list);
                        } else if (type.equals("BAN")) {
                            for (String string : Locale.BAN_MESSAGE.formatLines(plugin)) {
                                list.add(string
                                        .replace("%expirationDate%", punishment.expiry())
                                        .replace("%expiry%", punishment.duration())
                                        .replace("%reason%", reason));
                            }
                            this.message = String.join("\n", list);
                        }

                        return;
                    }
                }
            }
        }
    }

    public boolean isActive(String type, Date expires, Date pardoned) {
        boolean b = !type.equals("KICK") && !type.equals("WARN") && !type.equals("MUTE");

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
}
