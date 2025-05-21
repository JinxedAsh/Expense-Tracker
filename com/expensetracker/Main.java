package com.expensetracker;

import com.expensetracker.database.DatabaseManager;
import javax.swing.*;

public class Main {
    private static DatabaseManager dbManager;

    public static void main(String[] args) {
        // Set Look and Feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize database manager
        dbManager = new DatabaseManager();

        // Start application on EDT
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });

        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (dbManager != null) {
                dbManager.closeConnection();
            }
        }));
    }

    public static DatabaseManager getDatabase() {
        return dbManager;
    }
}