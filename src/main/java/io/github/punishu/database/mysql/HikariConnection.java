package io.github.punishu.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.punishu.PunishU;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HikariConnection {

    private final PunishU plugin;
    public HikariDataSource hikariDataSource;

    private final String host;
    private final String database;
    private final String port;
    private final String username;
    private final String password;

    public HikariConnection(PunishU plugin, String host, String database, String port, String username, String password) {
        this.plugin = plugin;

        this.host = host;
        this.database = database;
        this.port = port;
        this.username = username;
        this.password = password;

        connect();
    }

    private boolean connect() {
        if (host.isEmpty() || database.isEmpty() || port.isEmpty() || username.isEmpty() || password.isEmpty())
            return false;

        try {
            final HikariConfig hikariConfig = new HikariConfig();

            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(10);

            hikariDataSource = new HikariDataSource(hikariConfig);

            plugin.getServer().getConsoleSender().sendMessage("§8[§3PunishU§8] §aSuccessfully connected to MySQL!");

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void disconnect() {
        if (hikariDataSource != null)
            hikariDataSource.close();
    }

    public void saveData(final Player player, final PlayerData playerData) {
        this.asyncUpdate("UPDATE hsurvivalgames_stats SET "
                + "points = " + playerData.getPoints() + ","
                + "chatcolor = '" + playerData.getChatColor() + "',"
                + "disguise = " + playerData.isDisguise() + ","
                + "silentjoin = " + playerData.isSilentJoin() + ","
                + "kills = " + playerData.getKills() + ","
                + "deaths = " + playerData.getDeaths() + " WHERE uuid = '" + player.getUniqueId() + "'");
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public void createTable() {
        this.asyncUpdate("CREATE TABLE IF NOT EXISTS hsurvivalgames_stats (id INT PRIMARY KEY AUTO_INCREMENT, uuid varchar(36), name varchar(16), kills int, deaths int, wins int, points long, silentjoin int, disguise int, chatcolor varchar(2))");
    }

    public void asyncUpdate(final String query) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> this.update(query));
    }

    public void update(final String sql) {
        try (Connection connection = hikariDataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
