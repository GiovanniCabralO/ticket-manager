package com.ticketmanager;

import com.ticketmanager.database.DatabaseManager;
import com.ticketmanager.ui.Menu;

public class Main {

    public static void main(String[] args) {
        // 1. Connect to SQLite and create tables if needed
        DatabaseManager.initializeDatabase();

        // 2. Start the CLI menu
        new Menu().start();

        // 3. Clean up connection on exit
        DatabaseManager.closeConnection();
    }
}
