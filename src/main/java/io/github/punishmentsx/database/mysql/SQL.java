package io.github.punishmentsx.database.mysql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.github.punishmentsx.PunishmentsX;
import org.bukkit.configuration.file.FileConfiguration;

public class SQL {
    private final PunishmentsX plugin;
    private Connection connection;
    private final FileConfiguration config;
    public boolean usingLite;

    public SQL(PunishmentsX plugin, boolean usingLite) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.usingLite = usingLite;
    }

    public void openDatabaseConnection() {
        try {
            if (usingLite) {
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

            PreparedStatement stat = con.prepareStatement("CREATE TABLE IF NOT EXISTS punishments (id VARCHAR(36) PRIMARY KEY, pardoner VARCHAR(36), stack VARCHAR(36), expires DATE, issue_reason LONGTEXT, silent_pardon BOOLEAN, victim VARCHAR(36), silent_issue BOOLEAN, pardon_reason LONGTEXT, issued DATE, pardoned DATE, type VARCHAR(16), issuer VARCHAR(36));");
            stat.execute();
            PreparedStatement stat2 = con.prepareStatement("CREATE TABLE IF NOT EXISTS profiles (id VARCHAR(36) PRIMARY KEY, ip_history LONGTEXT, punishments LONGTEXT, name VARCHAR(36), current_ip VARCHAR(45));");
            stat2.execute();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException localSQLException) {
            localSQLException.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if ((connection == null) || (connection.isClosed())) {
                openDatabaseConnection();
            }
        } catch (SQLException ignored) {}
        return connection;
    }

}


