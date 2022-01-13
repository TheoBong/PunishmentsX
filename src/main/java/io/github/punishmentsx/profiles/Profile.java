package io.github.punishmentsx.profiles;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.punishments.Punishment;
import lombok.Data;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public @Data class Profile {

    private final PunishmentsX plugin;
    private final UUID uuid;
    private String name;
    private String currentIp;
    private List<String> ipHistory;
    private List<UUID> punishments;

    public Profile(PunishmentsX plugin, UUID uuid) {
        this.plugin = plugin;
        this.name = "null";
        this.uuid = uuid;
        this.ipHistory = new ArrayList<>();
        this.punishments = new ArrayList<>();
    }

    public Profile(PunishmentsX plugin, Player player) {
        this(plugin, player.getUniqueId());
        this.name = player.getName();
        this.currentIp = player.getAddress().getAddress().getHostAddress();
    }

    public Punishment getActivePunishment(Punishment.Type type) {
        for(Punishment punishment : getPunishments(type)) {
            if (punishment.isActive()) {
                return punishment;
            }
        }
        return null;
    }

    public List<Punishment> getPunishments(Punishment.Type type) {
        List<Punishment> punishments = new ArrayList<>();
        for(UUID uuid : this.punishments) {
            Punishment punishment = plugin.getPunishmentManager().getPunishment(uuid);
            if(punishment != null && punishment.getType().equals(type)) {
                punishments.add(punishment);
            }
        }
        return punishments;
    }

    public List<Punishment> getPunishmentsHistory() {
        List<Punishment> punishments = new ArrayList<>();
        for(UUID uuid : this.punishments) {
            Punishment punishment = plugin.getPunishmentManager().getPunishment(uuid);
            if(punishment != null) {
                punishments.add(punishment);
            }
        }
        return punishments;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void addIp(String ip) {
        this.currentIp = ip;
        if(!ipHistory.contains(ip)) {
            ipHistory.add(ip);
        }
    }

    public Punishment punish(Punishment.Type type, String stack, UUID issuer, String reason, Date expires, boolean silent) {
        Punishment punishment = plugin.getPunishmentManager().create(type, stack, this, issuer, reason, expires, silent);
        if(punishment != null) {
            punishment.execute();
        }
        return punishment;
    }

    public void update() {
        punishments.removeIf(uuid -> plugin.getPunishmentManager().getPunishment(uuid) == null);
    }

    public void importFromDocument(Document d) {
        setName(d.getString("name"));
        setCurrentIp(d.getString("current_ip"));
        setIpHistory(d.getList("ip_history", String.class));
        setPunishments(d.getList("punishments", UUID.class));
    }

    public void importSQL(String name, String currentIp, List<String> ipHistory, List<UUID> punishments) {
        setName(name);
        setCurrentIp(currentIp);
        setIpHistory(ipHistory);
        if (punishments == null) {
            setPunishments(new ArrayList<>());
        } else {
            setPunishments(punishments);
        }
    }

    public Map<String, Object> export() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("current_ip", currentIp);
        map.put("ip_history", ipHistory);
        map.put("punishments", punishments);
        return map;
    }

    public void exportSQL() {
        List<String> punishments2 = new ArrayList<>();
        for (UUID uuid : punishments) {
            punishments2.add(uuid.toString());
        }

        String ipHistoryString = String.join(",", ipHistory);
        String punishmentsString = punishments.isEmpty() ? null : String.join(",", punishments2);
        try {
            PreparedStatement ps = plugin.getSql().getConnection().prepareStatement("INSERT OR REPLACE INTO profiles(id, ip_history, punishments, name, current_ip) VALUES (?,?,?,?,?)");
            ps.setString(1, getUuid().toString());
            ps.setString(2, ipHistoryString);
            ps.setString(3, punishmentsString);
            ps.setString(4, getName());
            ps.setString(5, getCurrentIp());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
