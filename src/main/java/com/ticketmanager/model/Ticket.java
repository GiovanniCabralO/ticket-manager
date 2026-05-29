package com.ticketmanager.model;

import java.time.LocalDateTime;

public class Ticket {

    // Enum for ticket status
    public enum Status {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    // Enum for ticket priority
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    private int id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private String createdAt;
    private String updatedAt;

    // Constructor for creating a new ticket (no ID yet)
    public Ticket(String title, String description, Priority priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = Status.OPEN;
    }

    // Constructor for loading from database (has ID)
    public Ticket(int id, String title, String description,
                  Status status, Priority priority,
                  String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId()           { return id; }
    public String getTitle()     { return title; }
    public String getDescription() { return description; }
    public Status getStatus()    { return status; }
    public Priority getPriority() { return priority; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(int id)             { this.id = id; }
    public void setTitle(String title)    { this.title = title; }
    public void setDescription(String d)  { this.description = d; }
    public void setStatus(Status status)  { this.status = status; }
    public void setPriority(Priority p)   { this.priority = p; }
    public void setCreatedAt(String c)    { this.createdAt = c; }
    public void setUpdatedAt(String u)    { this.updatedAt = u; }

    @Override
    public String toString() {
        return String.format(
            "[#%d] %-30s | %-11s | %-8s | Created: %s",
            id, title, status, priority, createdAt
        );
    }
}
