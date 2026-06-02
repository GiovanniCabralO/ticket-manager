package com.ticketmanager;

import com.ticketmanager.database.DatabaseManager;
import com.ticketmanager.model.Ticket;
import com.ticketmanager.repository.TicketRepository;

import org.junit.jupiter.api.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TicketRepository.
 *
 * Each test runs against an in-memory SQLite database,
 * so no file is created and tests are fully isolated.
 *
 * Lifecycle:
 *   @BeforeEach  → fresh in-memory DB + tables
 *   @AfterEach   → connection closed
 */
class TicketRepositoryTest {

    private TicketRepository repo;

    @BeforeEach
    void setUp() {
        DatabaseManager.useInMemory();
        DatabaseManager.initializeDatabase();
        repo = new TicketRepository();
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.closeConnection();
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Test
    @DisplayName("create() — should return ticket with generated ID")
    void create_shouldReturnTicketWithId() {
        Ticket t = repo.create("Login bug", "Error on mobile", Ticket.Priority.HIGH);

        assertNotNull(t);
        assertTrue(t.getId() > 0);
        assertEquals("Login bug", t.getTitle());
        assertEquals("Error on mobile", t.getDescription());
        assertEquals(Ticket.Status.OPEN, t.getStatus());
        assertEquals(Ticket.Priority.HIGH, t.getPriority());
    }

    @Test
    @DisplayName("create() — should throw when title is empty")
    void create_shouldThrowOnEmptyTitle() {
        assertThrows(IllegalArgumentException.class,
            () -> repo.create("", "Some desc", Ticket.Priority.LOW));
    }

    @Test
    @DisplayName("create() — should throw when title is blank (spaces only)")
    void create_shouldThrowOnBlankTitle() {
        assertThrows(IllegalArgumentException.class,
            () -> repo.create("   ", "desc", Ticket.Priority.MEDIUM));
    }

    @Test
    @DisplayName("create() — should throw when title exceeds 100 chars")
    void create_shouldThrowOnTitleTooLong() {
        String longTitle = "A".repeat(101);
        assertThrows(IllegalArgumentException.class,
            () -> repo.create(longTitle, "", Ticket.Priority.LOW));
    }

    // =========================================================================
    // FIND BY ID
    // =========================================================================

    @Test
    @DisplayName("findById() — should return correct ticket")
    void findById_shouldReturnTicket() {
        Ticket created = repo.create("Crash on save", "", Ticket.Priority.CRITICAL);
        Ticket found   = repo.findById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Crash on save", found.getTitle());
    }

    @Test
    @DisplayName("findById() — should return null for non-existent ID")
    void findById_shouldReturnNullIfNotFound() {
        assertNull(repo.findById(9999));
    }

    @Test
    @DisplayName("findById() — should throw on ID <= 0")
    void findById_shouldThrowOnInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> repo.findById(0));
        assertThrows(IllegalArgumentException.class, () -> repo.findById(-1));
    }

    // =========================================================================
    // FIND ALL
    // =========================================================================

    @Test
    @DisplayName("findAll() — should return all created tickets")
    void findAll_shouldReturnAllTickets() {
        repo.create("Ticket A", "", Ticket.Priority.LOW);
        repo.create("Ticket B", "", Ticket.Priority.MEDIUM);
        repo.create("Ticket C", "", Ticket.Priority.HIGH);

        List<Ticket> all = repo.findAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("findAll() — should return empty list when no tickets exist")
    void findAll_shouldReturnEmptyList() {
        assertTrue(repo.findAll().isEmpty());
    }

    // =========================================================================
    // FILTER BY STATUS
    // =========================================================================

    @Test
    @DisplayName("findByStatus() — should return only matching tickets")
    void findByStatus_shouldFilterCorrectly() {
        Ticket t1 = repo.create("T1", "", Ticket.Priority.LOW);
        Ticket t2 = repo.create("T2", "", Ticket.Priority.LOW);
        repo.create("T3", "", Ticket.Priority.LOW);

        // Move T1 and T2 to IN_PROGRESS
        repo.updateStatus(t1.getId(), Ticket.Status.IN_PROGRESS);
        repo.updateStatus(t2.getId(), Ticket.Status.IN_PROGRESS);

        List<Ticket> inProgress = repo.findByStatus(Ticket.Status.IN_PROGRESS);
        assertEquals(2, inProgress.size());

        List<Ticket> open = repo.findByStatus(Ticket.Status.OPEN);
        assertEquals(1, open.size());
    }

    // =========================================================================
    // FILTER BY PRIORITY
    // =========================================================================

    @Test
    @DisplayName("findByPriority() — should return only matching tickets")
    void findByPriority_shouldFilterCorrectly() {
        repo.create("T1", "", Ticket.Priority.HIGH);
        repo.create("T2", "", Ticket.Priority.HIGH);
        repo.create("T3", "", Ticket.Priority.LOW);

        List<Ticket> high = repo.findByPriority(Ticket.Priority.HIGH);
        assertEquals(2, high.size());
    }

    // =========================================================================
    // SEARCH
    // =========================================================================

    @Test
    @DisplayName("search() — should find tickets matching keyword in title")
    void search_shouldMatchTitle() {
        repo.create("Login bug", "Error on auth", Ticket.Priority.HIGH);
        repo.create("Payment issue", "Card declined", Ticket.Priority.MEDIUM);

        List<Ticket> results = repo.search("login");
        assertEquals(1, results.size());
        assertEquals("Login bug", results.get(0).getTitle());
    }

    @Test
    @DisplayName("search() — should find tickets matching keyword in description")
    void search_shouldMatchDescription() {
        repo.create("Ticket A", "database connection error", Ticket.Priority.HIGH);
        repo.create("Ticket B", "timeout on dashboard", Ticket.Priority.LOW);

        List<Ticket> results = repo.search("database");
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("search() — should return empty list when no match")
    void search_shouldReturnEmptyWhenNoMatch() {
        repo.create("Login bug", "", Ticket.Priority.HIGH);

        List<Ticket> results = repo.search("payment");
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("search() — should throw on blank keyword")
    void search_shouldThrowOnBlankKeyword() {
        assertThrows(IllegalArgumentException.class, () -> repo.search(""));
        assertThrows(IllegalArgumentException.class, () -> repo.search("   "));
    }

    // =========================================================================
    // UPDATE STATUS
    // =========================================================================

    @Test
    @DisplayName("updateStatus() — should change ticket status")
    void updateStatus_shouldChangeStatus() {
        Ticket t = repo.create("Deploy issue", "", Ticket.Priority.HIGH);

        boolean ok = repo.updateStatus(t.getId(), Ticket.Status.IN_PROGRESS);

        assertTrue(ok);
        assertEquals(Ticket.Status.IN_PROGRESS, repo.findById(t.getId()).getStatus());
    }

    @Test
    @DisplayName("updateStatus() — should throw when ticket is already CLOSED")
    void updateStatus_shouldThrowIfClosed() {
        Ticket t = repo.create("Old ticket", "", Ticket.Priority.LOW);
        repo.close(t.getId());

        assertThrows(IllegalStateException.class,
            () -> repo.updateStatus(t.getId(), Ticket.Status.OPEN));
    }

    // =========================================================================
    // UPDATE DETAILS
    // =========================================================================

    @Test
    @DisplayName("updateDetails() — should update title and description")
    void updateDetails_shouldUpdateFields() {
        Ticket t = repo.create("Original title", "Original desc", Ticket.Priority.LOW);

        boolean ok = repo.updateDetails(t.getId(), "Updated title", "Updated desc");

        assertTrue(ok);
        Ticket updated = repo.findById(t.getId());
        assertEquals("Updated title", updated.getTitle());
        assertEquals("Updated desc", updated.getDescription());
    }

    @Test
    @DisplayName("updateDetails() — should keep old value when new input is blank")
    void updateDetails_shouldKeepOldValueWhenBlank() {
        Ticket t = repo.create("Keep this title", "Keep this desc", Ticket.Priority.LOW);

        // Pass blank for both — nothing should change
        boolean ok = repo.updateDetails(t.getId(), "", "");

        // Returns true (rows updated = 0 means ticket not found, but here it does update
        // with same values — behavior may vary; check title is unchanged)
        Ticket after = repo.findById(t.getId());
        assertEquals("Keep this title", after.getTitle());
    }

    @Test
    @DisplayName("updateDetails() — should throw when editing a closed ticket")
    void updateDetails_shouldThrowIfClosed() {
        Ticket t = repo.create("Closed ticket", "", Ticket.Priority.LOW);
        repo.close(t.getId());

        assertThrows(IllegalStateException.class,
            () -> repo.updateDetails(t.getId(), "New title", ""));
    }

    // =========================================================================
    // CLOSE
    // =========================================================================

    @Test
    @DisplayName("close() — should set status to CLOSED")
    void close_shouldSetStatusToClosed() {
        Ticket t = repo.create("To be closed", "", Ticket.Priority.MEDIUM);

        boolean ok = repo.close(t.getId());

        assertTrue(ok);
        assertEquals(Ticket.Status.CLOSED, repo.findById(t.getId()).getStatus());
    }

    @Test
    @DisplayName("close() — should return false for non-existent ticket")
    void close_shouldReturnFalseIfNotFound() {
        assertFalse(repo.close(9999));
    }

    // =========================================================================
    // STATS
    // =========================================================================

    @Test
    @DisplayName("getStats() — should reflect current counts correctly")
    void getStats_shouldReflectCounts() {
        Ticket t1 = repo.create("T1", "", Ticket.Priority.LOW);
        Ticket t2 = repo.create("T2", "", Ticket.Priority.LOW);
        repo.create("T3", "", Ticket.Priority.LOW);

        repo.updateStatus(t1.getId(), Ticket.Status.IN_PROGRESS);
        repo.close(t2.getId());

        String stats = repo.getStats();

        assertTrue(stats.contains("OPEN: 1"));
        assertTrue(stats.contains("IN_PROGRESS: 1"));
        assertTrue(stats.contains("CLOSED: 1"));
    }
}
