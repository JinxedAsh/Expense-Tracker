package com.expensetracker.database;

import com.expensetracker.models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:finance_tracker.db";
    private Connection conn;

    public Connection getConnection() {
        return conn;
    }

    public DatabaseManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Create connection
            conn = DriverManager.getConnection(DB_URL);

            // Enable foreign keys
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            // Create tables
            createTables();

        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Database driver not found. Please ensure SQLite JDBC is in your classpath.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Could not connect to database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void createTables() {
        try (Statement stmt = conn.createStatement()) {
            // Create users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    name TEXT,
                    monthly_budget REAL DEFAULT 0.0,
                    current_balance REAL DEFAULT 0.0
                )
            """);

            // Create transactions table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    category TEXT,
                    description TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create bills table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bills (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    category TEXT,
                    description TEXT,
                    due_date TEXT NOT NULL,
                    is_paid INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

            // Create budgets table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER,
                    category TEXT NOT NULL,
                    amount REAL NOT NULL,
                    month TEXT NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
            """);

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error creating database tables: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, name) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("name")
                );
                user.setId(rs.getInt("id"));
                user.setMonthlyBudget(rs.getDouble("monthly_budget"));
                user.setCurrentBalance(rs.getDouble("current_balance"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error logging in user: " + e.getMessage());
        }
        return null;
    }

    // Method to close the database connection
    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    public boolean addTransaction(int userId, Transaction transaction) {
        String sql = """
            INSERT INTO transactions (user_id, type, amount, date, category, description)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, transaction.getType());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDate());
            pstmt.setString(5, transaction.getCategory());
            pstmt.setString(6, transaction.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaction(int userId, String type, String date, double amount) {
        String sql = """
            DELETE FROM transactions 
            WHERE user_id = ? AND type = ? AND date = ? AND amount = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);
            pstmt.setString(3, date);
            pstmt.setDouble(4, amount);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactions(int userId, String type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = """
            SELECT * FROM transactions 
            WHERE user_id = ? AND type = ? 
            ORDER BY date DESC
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, type);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction transaction;
                if (type.equals("Income")) {
                    transaction = new Income(
                            rs.getDouble("amount"),
                            rs.getString("date"),
                            rs.getString("category"),
                            rs.getString("description")
                    );
                } else {
                    transaction = new Expense(
                            rs.getDouble("amount"),
                            rs.getString("date"),
                            rs.getString("category"),
                            rs.getString("description")
                    );
                }
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Bill> getBills(int userId) {
        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE user_id = ? ORDER BY due_date";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Bill bill = new Bill(
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("due_date")
                );
                bill.setPaid(rs.getInt("is_paid") == 1);
                bills.add(bill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public boolean addBill(int userId, Bill bill) {
        String sql = """
            INSERT INTO bills (user_id, amount, date, category, description, due_date, is_paid)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, bill.getAmount());
            pstmt.setString(3, bill.getDate());
            pstmt.setString(4, bill.getCategory());
            pstmt.setString(5, bill.getDescription());
            pstmt.setString(6, bill.getDueDate());
            pstmt.setInt(7, bill.isPaid() ? 1 : 0);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBill(int userId, String dueDate, double amount) {
        String sql = "DELETE FROM bills WHERE user_id = ? AND due_date = ? AND amount = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, dueDate);
            pstmt.setDouble(3, amount);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBillStatus(int userId, String dueDate, double amount, boolean isPaid, String paymentDate) {
        String sql = "UPDATE bills SET is_paid = ?, date = ? WHERE user_id = ? AND due_date = ? AND amount = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isPaid ? 1 : 0);
            pstmt.setString(2, paymentDate);  // Add payment date when marking as paid
            pstmt.setInt(3, userId);
            pstmt.setString(4, dueDate);
            pstmt.setDouble(5, amount);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserBudget(int userId, double budget) {
        String sql = "UPDATE users SET monthly_budget = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, budget);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}