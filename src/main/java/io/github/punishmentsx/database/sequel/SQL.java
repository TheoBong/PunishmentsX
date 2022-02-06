package io.github.punishmentsx.database.sequel;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;

import io.github.punishmentsx.PunishmentsX;
import io.github.punishmentsx.database.Database;
import io.github.punishmentsx.database.mongo.MongoDeserializedResult;
import io.github.punishmentsx.profiles.Profile;
import io.github.punishmentsx.punishments.Punishment;
import org.bukkit.configuration.file.FileConfiguration;

public class SQL extends Database {

    private final PunishmentsX plugin;
    private Connection connection;
    private final FileConfiguration config;
    private final Type type;

    public SQL(PunishmentsX plugin, Type type) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.type = type;

        openDatabaseConnection();
    }

    public Type type() {
        return type;
    }

    public Profile loadProfile(boolean async, String name, boolean store, MongoDeserializedResult mdr) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM profiles WHERE name = ?");
            ps.setString(1, name.toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
                rs.next();
            } else {
                if (!rs.next()) {
                    plugin.getLogger().log(Level.WARNING, "Rs doesn't have next");
                    return null;
                }
            }

            UUID uuid = UUID.fromString(rs.getString("id"));
            String currentIp = rs.getString("current_ip");


            List<String> ipHistory = Arrays.asList(rs.getString("ip_history").split("\\s*,\\s*"));
            List<String> punishmentsStrings = Arrays.asList(rs.getString("punishments").split("\\s*,\\s*"));

            List<UUID> punishments = new ArrayList<>();
            for (String string : punishmentsStrings) {
                punishments.add(UUID.fromString(string));
            }

            Profile profile = new Profile(plugin, uuid);
            importSQL(profile, name, currentIp, ipHistory, punishments);

            for(UUID u : profile.getPunishments()) {
                plugin.getStorage().loadPunishment(false, u, true);
            }

            if(store) {
                plugin.getProfileManager().getProfiles().put(profile.getUuid(), profile);
            }

            return profile;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Profile loadProfile(boolean async, UUID uuid, boolean store, MongoDeserializedResult mdr) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM profiles WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
                rs.next();
            } else {
                if (!rs.next()) {
                    plugin.getLogger().log(Level.WARNING, "Rs doesn't have next");
                    return null;
                }
            }

            List<UUID> punishments = new ArrayList<>();
            String punishmentsString = rs.getString("punishments");
            if (punishmentsString != null) {
                String[] punishmentsStrings = punishmentsString.split("\\s*,\\s*");
                for (String string : punishmentsStrings) {
                    punishments.add(UUID.fromString(string));
                }
            } else {
                punishments = null;
            }

            String ipHistoryString = rs.getString("ip_history");
            List<String> ipHistory = null;
            if (ipHistoryString != null) {
                ipHistory = Arrays.asList(ipHistoryString.split("\\s*,\\s*"));
            }

            String name = rs.getString("name");
            String currentIp = rs.getString("current_ip");

            Profile profile = new Profile(plugin, uuid);
            importSQL(profile, name, currentIp, ipHistory, punishments);

            for(UUID u : profile.getPunishments()) {
                plugin.getStorage().loadPunishment(false, u, true);
            }

            if(store) {
                plugin.getProfileManager().getProfiles().put(profile.getUuid(), profile);
            }

            return profile;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveProfile(boolean async, Profile profile) {
        List<String> punishments2 = new ArrayList<>();
        for (UUID uuid : profile.getPunishments()) {
            punishments2.add(uuid.toString());
        }

        String ipHistoryString = String.join(",", profile.getIpHistory());
        String punishmentsString = profile.getPunishments().isEmpty() ? null : String.join(",", punishments2);
        try {
            PreparedStatement ps = getConnection().prepareStatement("REPLACE INTO profiles(id, ip_history, punishments, name, current_ip) VALUES (?,?,?,?,?)");
            ps.setString(1, profile.getUuid().toString());
            ps.setString(2, ipHistoryString);
            ps.setString(3, punishmentsString);
            ps.setString(4, profile.getName().toLowerCase());
            ps.setString(5, profile.getCurrentIp());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadPunishment(boolean async, UUID uuid, boolean store) {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT * FROM punishments WHERE id = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (plugin.getStorage().type() == Database.Type.MySQL) {
                rs.beforeFirst();
                rs.next();
            } else {
                if (!rs.next()) {
                    plugin.getLogger().log(Level.WARNING, "Rs doesn't have next");
                    return;
                }
            }

            UUID victim = UUID.fromString(rs.getString("victim"));
            UUID issuer = UUID.fromString(rs.getString("issuer"));
            UUID pardoner = rs.getString("pardoner") == null ? null : UUID.fromString(rs.getString("pardoner"));
            String stack = rs.getString("stack");
            String issueReason = rs.getString("issue_reason");
            String pardonReason = rs.getString("pardon_reason");
            Date issued = rs.getTime("issued");
            Date expires = rs.getTime("expires");
            Date pardoned = rs.getTime("pardoned");
            String punishmentType = rs.getString("type");
            boolean silentIssue = rs.getBoolean("silent_issue");
            boolean silentPardon = rs.getBoolean("silent_pardon");

            Punishment punishment = new Punishment(plugin, uuid);

            punishment.setVictim(victim);
            punishment.setIssuer(issuer);
            punishment.setPardoner(pardoner);

            punishment.setStack(stack);
            punishment.setIssueReason(issueReason);
            punishment.setPardonReason(pardonReason);
            punishment.setIssued(issued);
            punishment.setExpires(expires);
            punishment.setPardoned(pardoned);
            punishment.setType(Punishment.Type.valueOf(punishmentType));
            punishment.setSilentIssue(silentIssue);
            punishment.setSilentPardon(silentPardon);

            if (store) {
                plugin.getPunishmentManager().getPunishments().put(punishment.getUuid(), punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePunishment(boolean async, Punishment punishment) {
        try {
            java.sql.Time expirySQL = null;
            if (punishment.getExpires() != null) expirySQL = new java.sql.Time(punishment.getExpires().getTime());
            java.sql.Time issuedSQL = new java.sql.Time(punishment.getIssued().getTime());
            java.sql.Time pardonedSQL = null;
            if (punishment.getPardoned() != null) pardonedSQL = new java.sql.Time(punishment.getPardoned().getTime());

            String pardonerString = punishment.getPardoner() == null ? null : punishment.getPardoner().toString();

            PreparedStatement ps = getConnection().prepareStatement("REPLACE INTO punishments(id, pardoner, stack, expires, issue_reason, silent_pardon, victim, silent_issue, pardon_reason, issued, pardoned, type, issuer) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, punishment.getUuid().toString());
            ps.setString(2, pardonerString);
            ps.setString(3, punishment.getStack());
            ps.setTime(4, expirySQL);
            ps.setString(5, punishment.getIssueReason());
            ps.setBoolean(6, punishment.isSilentPardon());
            ps.setString(7, punishment.getVictim().toString());
            ps.setBoolean(8, punishment.isSilentIssue());
            ps.setString(9, punishment.getPardonReason());
            ps.setTime(10, issuedSQL);
            ps.setTime(11, pardonedSQL);
            ps.setString(12, punishment.getType().toString());
            ps.setString(13, punishment.getIssuer() == null ? null : punishment.getIssuer().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        closeConnection();
    }

    public void importSQL(Profile profile, String name, String currentIp, List<String> ipHistory, List<UUID> punishments) {
        profile.setName(name);
        profile.setCurrentIp(currentIp);
        profile.setIpHistory(ipHistory);
        if (punishments == null) {
            profile.setPunishments(new ArrayList<>());
        } else {
            profile.setPunishments(punishments);
        }
    }

    private void openDatabaseConnection() {
        try {
            if (type.equals(Type.SQLite)) {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException localClassNotFoundException) {
                    localClassNotFoundException.printStackTrace();
                }
                File localFile = new File(plugin.getDataFolder().toString() + File.separator + "data.db");
                if (!localFile.exists()) {
                    try {
                        localFile.createNewFile();
                    } catch (IOException localIOException) {
                        localIOException.printStackTrace();
                    }
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + localFile.getAbsolutePath());
            } else {
                connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("DATABASE.MYSQL.HOST") + ":" + config.getString("DATABASE.MYSQL.PORT") + "/" + config.getString("DATABASE.MYSQL.DATABASE") + "?autoReconnect=true", config.getString("DATABASE.MYSQL.USER"), config.getString("DATABASE.MYSQL.PASSWORD"));
            }

            Connection con = getConnection();

            PreparedStatement stat = con.prepareStatement("CREATE TABLE IF NOT EXISTS punishments (id VARCHAR(36) PRIMARY KEY, pardoner VARCHAR(36), stack VARCHAR(36), expires TIME, issue_reason LONGTEXT, silent_pardon BOOLEAN, victim VARCHAR(36), silent_issue BOOLEAN, pardon_reason LONGTEXT, issued TIME, pardoned TIME, type VARCHAR(16), issuer VARCHAR(36));");
            stat.execute();
            PreparedStatement stat2 = con.prepareStatement("CREATE TABLE IF NOT EXISTS profiles (id VARCHAR(36) PRIMARY KEY, ip_history LONGTEXT, punishments LONGTEXT, name VARCHAR(36), current_ip VARCHAR(45));");
            stat2.execute();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            if ((connection == null) || (connection.isClosed())) {
                openDatabaseConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

}


