package com.ticketmanager.repository;

import com.ticketmanager.database.DatabaseManager;
import com.ticketmanager.model.Ticket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository {

    // =========================================================================
    // CREATE
    // =========================================================================

    public Ticket create(String title, String description, Ticket.Priority priority) {
        // --- Validation ---
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title cannot be empty.");
        if (title.length() > 100)
            throw new IllegalArgumentException("Title must be 100 characters or less.");
        if (description == null) description = "";

        String sql = """
            INSERT INTO tickets (title, description, status, priority)
            VALUES (?, ?, 'OPEN', ?)
            """;

        try (PreparedStatement stmt = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title.trim());
            stmt.setString(2, description.trim());
            stmt.setString(3, priority.name());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                log(id, "CREATED", "Priority: " + priority);
                return findById(id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating ticket: " + e.getMessage(), e);
        }

        throw new RuntimeException("Failed to retrieve generated ID after insert.");
    }

    // =========================================================================
    // READ — all
    // =========================================================================

    public List<Ticket> findAll() {
        return query("SELECT * FROM tickets ORDER BY id");
    }

    // =========================================================================
    // READ — single by ID
    // =========================================================================

    public Ticket findById(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID must be a positive number.");

        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            List<Ticket> result = mapResultSet(stmt.executeQuery());
            return result.isEmpty() ? null : result.get(0);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding ticket: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // READ — filter by status
    // =========================================================================

    public List<Ticket> findByStatus(Ticket.Status status) {
        if (status == null) throw new IllegalArgumentException("Status cannot be null.");

        String sql = "SELECT * FROM tickets WHERE status = ? ORDER BY id";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status.name());
            return mapResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering by status: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // READ — filter by priority
    // =========================================================================

    public List<Ticket> findByPriority(Ticket.Priority priority) {
        if (priority == null) throw new IllegalArgumentException("Priority cannot be null.");

        String sql = "SELECT * FROM tickets WHERE priority = ? ORDER BY id";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, priority.name());
            return mapResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering by priority: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // READ — full-text search on title and description
    // =========================================================================

    public List<Ticket> search(String keyword) {
        if (keyword == null || keyword.isBlank())
            throw new IllegalArgumentException("Search keyword cannot be empty.");

        // LIKE with wildcards — case-insensitive in SQLite by default for ASCII
        String sql = """
            SELECT * FROM tickets
            WHERE title LIKE ? OR description LIKE ?
            ORDER BY id
            """;
        String pattern = "%" + keyword.trim() + "%";

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            return mapResultSet(stmt.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error searching tickets: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // UPDATE — status
    // =========================================================================

    public boolean updateStatus(int id, Ticket.Status newStatus) {
        if (id <= 0) throw new IllegalArgumentException("ID must be a positive number.");
        if (newStatus == null) throw new IllegalArgumentException("Status cannot be null.");

        // Prevent updating a ticket that is already CLOSED
        Ticket existing = findById(id);
        if (existing == null) return false;
        if (existing.getStatus() == Ticket.Status.CLOSED)
            throw new IllegalStateException("Cannot update a closed ticket.");

        String sql = """
            UPDATE tickets
            SET status = ?, updated_at = datetime('now','localtime')
            WHERE id = ?
            """;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                log(id, "STATUS_CHANGED", existing.getStatus() + " → " + newStatus);
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating status: " + e.getMessage(), e);
        }
        return false;
    }

    // =========================================================================
    // UPDATE — title and/or description
    // =========================================================================

    public boolean updateDetails(int id, String newTitle, String newDescription) {
        if (id <= 0) throw new IllegalArgumentException("ID must be a positive number.");

        Ticket existing = findById(id);
        if (existing == null) return false;
        if (existing.getStatus() == Ticket.Status.CLOSED)
            throw new IllegalStateException("Cannot edit a closed ticket.");

        // Use existing value if the new input is blank (allows partial update)
        String title = (newTitle == null || newTitle.isBlank())
                ? existing.getTitle() : newTitle.trim();
        String desc  = (newDescription == null || newDescription.isBlank())
                ? existing.getDescription() : newDescription.trim();

        if (title.length() > 100)
            throw new IllegalArgumentException("Title must be 100 characters or less.");

        String sql = """
            UPDATE tickets
            SET title = ?, description = ?, updated_at = datetime('now','localtime')
            WHERE id = ?
            """;

        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, desc);
            stmt.setInt(3, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                log(id, "DETAILS_UPDATED", "Title: \"" + title + "\"");
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating details: " + e.getMessage(), e);
        }
        return false;
    }

    // =========================================================================
    // CLOSE — sets status to CLOSED (soft delete)
    // =========================================================================

    public boolean close(int id) {
        return updateStatus(id, Ticket.Status.CLOSED);
    }

    // =========================================================================
    // STATS — count per status
    // =========================================================================

    public String getStats() {
        String sql = "SELECT status, COUNT(*) as total FROM tickets GROUP BY status";
        int open = 0, inProgress = 0, resolved = 0, closed = 0;

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int count = rs.getInt("total");
                switch (rs.getString("status")) {
                    case "OPEN"        -> open       = count;
                    case "IN_PROGRESS" -> inProgress = count;
                    case "RESOLVED"    -> resolved   = count;
                    case "CLOSED"      -> closed     = count;
                }
            }
        } catch (SQLException e) {
            return "Stats unavailable.";
        }

        return String.format(
            "[ OPEN: %d | IN_PROGRESS: %d | RESOLVED: %d | CLOSED: %d ]",
            open, inProgress, resolved, closed
        );
    }

    // =========================================================================
    // LOGS — fetch change history for a ticket
    // =========================================================================

    public void printLogs(int ticketId) {
        if (ticketId <= 0) throw new IllegalArgumentException("ID must be a positive number.");

        String sql = "SELECT * FROM ticket_logs WHERE ticket_id = ? ORDER BY id";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Change log for Ticket #" + ticketId + " ---");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("[%s] %-20s %s%n",
                    rs.getString("logged_at"),
                    rs.getString("action"),
                    rs.getString("details"));
            }
            if (!found) System.out.println("No log entries found.");

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching logs: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // EXPORT — CSV report
    // =========================================================================

    public void exportCsv(String filename) {
        if (filename == null || filename.isBlank())
            throw new IllegalArgumentException("Filename cannot be empty.");
        if (!filename.endsWith(".csv")) filename = filename + ".csv";

        List<Ticket> all = findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Description,Status,Priority,CreatedAt,UpdatedAt\n");

        for (Ticket t : all) {
            // Wrap text fields in quotes to handle commas inside values
            sb.append(String.format("%d,\"%s\",\"%s\",%s,%s,%s,%s\n",
                t.getId(),
                t.getTitle().replace("\"", "\"\""),
                t.getDescription().replace("\"", "\"\""),
                t.getStatus(),
                t.getPriority(),
                t.getCreatedAt(),
                t.getUpdatedAt()));
        }

        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of(filename), sb);
            System.out.println("Exported " + all.size() + " ticket(s) to " + filename);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error writing CSV: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private List<Ticket> query(String sql) {
        try (Statement stmt = DatabaseManager.getConnection().createStatement()) {
            return mapResultSet(stmt.executeQuery(sql));
        } catch (SQLException e) {
            throw new RuntimeException("Query error: " + e.getMessage(), e);
        }
    }

    private List<Ticket> mapResultSet(ResultSet rs) throws SQLException {
        List<Ticket> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Ticket(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                Ticket.Status.valueOf(rs.getString("status")),
                Ticket.Priority.valueOf(rs.getString("priority")),
                rs.getString("created_at"),
                rs.getString("updated_at")
            ));
        }
        return list;
    }

    private void log(int ticketId, String action, String details) {
        String sql = "INSERT INTO ticket_logs (ticket_id, action, details) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Log failure is non-fatal — just warn
            System.err.println("Warning: failed to write log entry: " + e.getMessage());
        }
    }
}
