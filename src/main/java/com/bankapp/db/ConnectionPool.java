package com.bankapp.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private static HikariDataSource dataSource;

    static {
        try {
            String userHome = System.getProperty("user.home");
            File dbDir = new File(userHome, ".bankapp/data");
            if (!dbDir.exists()) {
                if (dbDir.mkdirs()) {
                    logger.info("Created database directory: {}", dbDir.getAbsolutePath());
                }
            }
            File dbFile = new File(dbDir, "bankdata.db");
            String dbUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            logger.info("Initializing HikariCP connection pool for SQLite at: {}", dbUrl);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // WAL mode (Write-Ahead Logging) is critical for SQLite concurrency
            config.addDataSourceProperty("journal_mode", "WAL");
            config.addDataSourceProperty("synchronous", "NORMAL");

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP connection pool", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed.");
        }
    }
}
