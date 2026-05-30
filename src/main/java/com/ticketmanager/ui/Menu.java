package com.ticketmanager.ui;

import com.ticketmanager.model.Ticket;
import com.ticketmanager.repository.TicketRepository;

import java.util.List;
import java.util.Scanner;

public class Menu {

    private final TicketRepository repo = new TicketRepository();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1  -> createTicket();
                case 2  -> listTickets();
                case 3  -> filterMenu();
                case 4  -> searchTickets();
                case 5  -> updateStatus();
                case 6  -> editTicket();
                case 7  -> viewTicketDetail();
                case 8  -> closeTicket();
                case 9  -> exportCsv();
                case 0  -> running = false;
                default -> System.out.println("  Invalid option. Try again.");
            }
        }
        System.out.println("\nGoodbye!");
    }

    // -------------------------------------------------------------------------
    // MENU HEADER
    // -------------------------------------------------------------------------

    private void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("        TICKET MANAGER  v1.0            ");
        System.out.println("========================================");

        // Stats line — shows current counts at a glance
        try {
            System.out.println("  " + repo.getStats());
        } catch (Exception e) {
            // Silently skip stats if DB not ready yet
        }

        System.out.println("----------------------------------------");
        System.out.println("  1. Create ticket");
        System.out.println("  2. List all tickets");
        System.out.println("  3. Filter tickets (status / priority)");
        System.out.println("  4. Search tickets");
        System.out.println("  5. Update ticket status");
        System.out.println("  6. Edit ticket (title / description)");
        System.out.println("  7. View ticket details + history");
        System.out.println("  8. Close ticket");
        System.out.println("  9. Export report (CSV)");
        System.out.println("  0. Exit");
        System.out.println("----------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 1. CREATE
    // -------------------------------------------------------------------------

    private void createTicket() {
        System.out.println("\n-- New Ticket --");

        String title = "";
        while (title.isBlank()) {
            title = readString("Title (max 100 chars): ");
            if (title.isBlank()) System.out.println("  Title cannot be empty.");
            if (title.length() > 100) {
                System.out.println("  Title too long. Try again.");
                title = "";
            }
        }

        String desc = readString("Description (optional, press Enter to skip): ");
        Ticket.Priority priority = readPriority();

        try {
            Ticket t = repo.create(title, desc, priority);
            System.out.println("\n  Ticket created successfully!");
            System.out.println("  " + t);
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. LIST ALL
    // -------------------------------------------------------------------------

    private void listTickets() {
        List<Ticket> tickets = repo.findAll();
        System.out.println("\n-- All Tickets (" + tickets.size() + ") --");
        if (tickets.isEmpty()) {
            System.out.println("  No tickets found.");
            return;
        }
        printTicketHeader();
        tickets.forEach(t -> System.out.println("  " + t));
    }

    // -------------------------------------------------------------------------
    // 3. FILTER
    // -------------------------------------------------------------------------

    private void filterMenu() {
        System.out.println("\n  1. Filter by status");
        System.out.println("  2. Filter by priority");
        int choice = readInt("Choose: ");

        List<Ticket> result;
        String label;

        if (choice == 1) {
            Ticket.Status status = readStatus();
            result = repo.findByStatus(status);
            label = "Status: " + status;
        } else if (choice == 2) {
            Ticket.Priority priority = readPriority();
            result = repo.findByPriority(priority);
            label = "Priority: " + priority;
        } else {
            System.out.println("  Invalid option.");
            return;
        }

        System.out.println("\n-- Filter: " + label + " (" + result.size() + " result(s)) --");
        if (result.isEmpty()) {
            System.out.println("  No tickets found.");
        } else {
            printTicketHeader();
            result.forEach(t -> System.out.println("  " + t));
        }
    }

    // -------------------------------------------------------------------------
    // 4. SEARCH
    // -------------------------------------------------------------------------

    private void searchTickets() {
        System.out.println("\n-- Search Tickets --");
        String keyword = readString("Keyword: ");

        if (keyword.isBlank()) {
            System.out.println("  Keyword cannot be empty.");
            return;
        }

        try {
            List<Ticket> result = repo.search(keyword);
            System.out.println("  Found " + result.size() + " result(s) for \"" + keyword + "\":");
            if (result.isEmpty()) {
                System.out.println("  No tickets match your search.");
            } else {
                printTicketHeader();
                result.forEach(t -> System.out.println("  " + t));
            }
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 5. UPDATE STATUS
    // -------------------------------------------------------------------------

    private void updateStatus() {
        System.out.println("\n-- Update Ticket Status --");
        int id = readInt("Ticket ID: ");

        Ticket t = repo.findById(id);
        if (t == null) { System.out.println("  Ticket #" + id + " not found."); return; }

        System.out.println("  Current status: " + t.getStatus());

        try {
            Ticket.Status newStatus = readStatus();
            boolean ok = repo.updateStatus(id, newStatus);
            System.out.println(ok ? "  Status updated to " + newStatus + "!"
                                  : "  Failed to update.");
        } catch (IllegalStateException e) {
            System.out.println("  " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 6. EDIT TITLE / DESCRIPTION
    // -------------------------------------------------------------------------

    private void editTicket() {
        System.out.println("\n-- Edit Ticket --");
        int id = readInt("Ticket ID: ");

        Ticket t = repo.findById(id);
        if (t == null) { System.out.println("  Ticket #" + id + " not found."); return; }

        System.out.println("  Current title:       " + t.getTitle());
        System.out.println("  Current description: " + t.getDescription());
        System.out.println("  (Press Enter to keep the current value)");

        String newTitle = readString("New title: ");
        String newDesc  = readString("New description: ");

        if (newTitle.isBlank() && newDesc.isBlank()) {
            System.out.println("  Nothing changed.");
            return;
        }

        try {
            boolean ok = repo.updateDetails(id, newTitle, newDesc);
            System.out.println(ok ? "  Ticket updated!" : "  Failed to update.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 7. VIEW DETAILS + LOG
    // -------------------------------------------------------------------------

    private void viewTicketDetail() {
        System.out.println("\n-- Ticket Details --");
        int id = readInt("Ticket ID: ");

        Ticket t = repo.findById(id);
        if (t == null) { System.out.println("  Ticket #" + id + " not found."); return; }

        System.out.println();
        System.out.println("  ID:          " + t.getId());
        System.out.println("  Title:       " + t.getTitle());
        System.out.println("  Description: " + t.getDescription());
        System.out.println("  Status:      " + t.getStatus());
        System.out.println("  Priority:    " + t.getPriority());
        System.out.println("  Created at:  " + t.getCreatedAt());
        System.out.println("  Updated at:  " + t.getUpdatedAt());

        repo.printLogs(id);
    }

    // -------------------------------------------------------------------------
    // 8. CLOSE
    // -------------------------------------------------------------------------

    private void closeTicket() {
        System.out.println("\n-- Close Ticket --");
        int id = readInt("Ticket ID to close: ");

        Ticket t = repo.findById(id);
        if (t == null) { System.out.println("  Ticket #" + id + " not found."); return; }
        if (t.getStatus() == Ticket.Status.CLOSED) {
            System.out.println("  Ticket #" + id + " is already closed.");
            return;
        }

        try {
            boolean ok = repo.close(id);
            System.out.println(ok ? "  Ticket #" + id + " closed." : "  Failed to close.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 9. EXPORT CSV
    // -------------------------------------------------------------------------

    private void exportCsv() {
        System.out.println("\n-- Export CSV --");
        String filename = readString("Filename (e.g. report.csv): ");

        try {
            repo.exportCsv(filename);
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers — input reading
    // -------------------------------------------------------------------------

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Ticket.Priority readPriority() {
        System.out.println("  Priority:  1=LOW  2=MEDIUM  3=HIGH  4=CRITICAL");
        return switch (readInt("  Choose: ")) {
            case 1  -> Ticket.Priority.LOW;
            case 3  -> Ticket.Priority.HIGH;
            case 4  -> Ticket.Priority.CRITICAL;
            default -> Ticket.Priority.MEDIUM;
        };
    }

    private Ticket.Status readStatus() {
        System.out.println("  Status:  1=OPEN  2=IN_PROGRESS  3=RESOLVED  4=CLOSED");
        return switch (readInt("  Choose: ")) {
            case 2  -> Ticket.Status.IN_PROGRESS;
            case 3  -> Ticket.Status.RESOLVED;
            case 4  -> Ticket.Status.CLOSED;
            default -> Ticket.Status.OPEN;
        };
    }

    // Header row for ticket lists
    private void printTicketHeader() {
        System.out.printf("  %-6s %-32s %-13s %-10s %s%n",
            "ID", "TITLE", "STATUS", "PRIORITY", "CREATED AT");
        System.out.println("  " + "-".repeat(80));
    }
}
