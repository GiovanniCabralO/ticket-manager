package com.ticketmanager.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DEFAULT_URL = "jdbc:sqlite:tickets.db";
    private static String dbUrl = DEFAULT_URL;
    private static Connection connection;

    // ------------------------------------------------------------------
    // Used by tests to switch to an in-memory database (no file created)
    // ------------------------------------------------------------------
    public static void useInMemory() {
        dbUrl = "jdbc:sqlite::memory:";
        connection = null; // force a new connection on next call
    }

    // ------------------------------------------------------------------
    // Restores the default file-based database
    // ------------------------------------------------------------------
    public static void resetToDefault() {
        closeConnection();
        dbUrl = DEFAULT_URL;
    }

    // ------------------------------------------------------------------
    // Returns (or opens) the shared connection — Singleton
    // ------------------------------------------------------------------
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
        }
        return connection;
    }

    // ------------------------------------------------------------------
    // Creates all tables if they don't exist yet
    // ------------------------------------------------------------------
    public static void initializeDatabase() {
        String createTickets = """
            CREATE TABLE IF NOT EXISTS tickets (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                title       TEXT    NOT NULL,
                description TEXT    NOT NULL DEFAULT '',
                status      TEXT    NOT NULL DEFAULT 'OPEN',
                priority    TEXT    NOT NULL DEFAULT 'MEDIUM',
                created_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
                updated_at  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
            );
            """;

        // Every status change is recorded here for full audit history
        String createLogs = """
            CREATE TABLE IF NOT EXISTS ticket_logs (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                ticket_id   INTEGER NOT NULL,
                action      TEXT    NOT NULL,
                details     TEXT,
                logged_at   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
                FOREIGN KEY (ticket_id) REFERENCES tickets(id)
            );
            """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createTickets);
            stmt.execute(createLogs);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
