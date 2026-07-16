package com.bankapp.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationManager {
    private static final Logger logger = LoggerFactory.getLogger(MigrationManager.class);

    public static void runMigrations() {
        try (Connection conn = ConnectionPool.getConnection()) {
            int currentVersion = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {
                if (rs.next()) {
                    currentVersion = rs.getInt(1);
                }
            }

            logger.info("Current DB Schema Version: {}", currentVersion);

            if (currentVersion < 1) {
                logger.info("Running Migration V1 (Initial Schema)...");
                String v1Sql = readResourceFile("/migrations/V1__init_schema.sql");
                executeSqlInTransaction(conn, v1Sql);
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA user_version = 1;");
                }
                logger.info("Successfully migrated DB Schema to Version 1.");
            }
        } catch (Exception e) {
            logger.error("Migration failed!", e);
            throw new RuntimeException("Database migration failed", e);
        }
    }

    private static void executeSqlInTransaction(Connection conn, String sql) throws Exception {
        boolean autoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                // Split queries by semicolon to execute them sequentially
                String[] queries = sql.split(";");
                for (String q : queries) {
                    String trimmed = q.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private static String readResourceFile(String path) throws Exception {
        try (InputStream is = MigrationManager.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalArgumentException("Migration resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
    }
}
