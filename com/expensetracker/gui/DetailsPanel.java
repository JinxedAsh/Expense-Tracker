package com.expensetracker.gui;

import com.expensetracker.Main;
import com.expensetracker.MainFrame;
import com.expensetracker.database.DatabaseManager;
import com.expensetracker.models.User;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

 //Panel displaying user's financial overview including balance, income, expenses, savings, and budget information. 
 //Provides functionality for data export and management.

public class DetailsPanel extends JPanel {
     // UI Components
    private JLabel nameLabel;
    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel savingsLabel;
    private JLabel budgetLabel;
    private JButton logoutButton;
    private JButton resetButton;
    private JButton exportButton;
    private JButton refreshButton;
    private JButton setBudgetButton;

    // Data management
    private DatabaseManager dbManager;
    private User currentUser;
    private JLabel currentDateTimeLabel;
    private double monthlyExpenses;
    private double monthlyBills;

    public DetailsPanel() {
        dbManager = Main.getDatabase();
        setLayout(new BorderLayout());
        initializeComponents();
        startTimeUpdater();
    }

    //Initializes and arranges all UI components in the panel including summary information labels and action buttons.
    private void initializeComponents() {
        JPanel summaryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        currentDateTimeLabel = new JLabel();
        updateDateTime();

        nameLabel = new JLabel("Current User's Login: ");
        balanceLabel = new JLabel("Current Balance: Calculating...");
        incomeLabel = new JLabel("This Month's Income: Calculating...");
        expenseLabel = new JLabel("This Month's Expenses: Calculating...");
        savingsLabel = new JLabel("Previous Month's Savings: Calculating...");
        budgetLabel = new JLabel("Monthly Budget: $0.00");

        gbc.gridx = 0; gbc.gridy = 0;
        summaryPanel.add(currentDateTimeLabel, gbc);
        gbc.gridy = 1;
        summaryPanel.add(nameLabel, gbc);
        gbc.gridy = 2;
        summaryPanel.add(balanceLabel, gbc);
        gbc.gridy = 3;
        summaryPanel.add(incomeLabel, gbc);
        gbc.gridy = 4;
        summaryPanel.add(expenseLabel, gbc);
        gbc.gridy = 5;
        summaryPanel.add(savingsLabel, gbc);
        gbc.gridy = 6;
        summaryPanel.add(budgetLabel, gbc);

        JPanel buttonPanel = new JPanel();
        logoutButton = new JButton("Logout");
        resetButton = new JButton("Reset All Data");
        exportButton = new JButton("Export to CSV");
        refreshButton = new JButton("Refresh");
        setBudgetButton = new JButton("Set Budget");

        buttonPanel.add(refreshButton);
        buttonPanel.add(setBudgetButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(exportButton);

        add(summaryPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setupListeners();
    }

    private void updateDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);
        currentDateTimeLabel.setText("Current Date and Time): " + formattedDateTime);
    }

    private void startTimeUpdater() {
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
    }

    private void setupListeners() {
        logoutButton.addActionListener(e -> handleLogout());
        resetButton.addActionListener(e -> handleReset());
        exportButton.addActionListener(e -> handleExport());
        refreshButton.addActionListener(e -> refreshData());
        setBudgetButton.addActionListener(e -> showSetBudgetDialog());
    }

    //Opens a dialog for setting monthly budget with real-time validation and database updates.
    private void showSetBudgetDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Set Monthly Budget", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField budgetField = new JTextField(10);
        if (currentUser.getMonthlyBudget() > 0) {
            budgetField.setText(String.format("%.2f", currentUser.getMonthlyBudget()));
        }

        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Enter Monthly Budget: $"), gbc);
        gbc.gridx = 1;
        dialog.add(budgetField, gbc);

        JButton submitButton = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        dialog.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            try {
                double budget = Double.parseDouble(budgetField.getText());
                if (budget < 0) {
                    throw new NumberFormatException();
                }

                if (updateUserBudget(currentUser.getId(), budget)) {
                    currentUser.setMonthlyBudget(budget);
                    updateBudgetLabel(budget);
                    dialog.dispose();
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to update budget.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid positive number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateBudgetLabel(double budget) {
        double totalExpenses = monthlyExpenses + monthlyBills;
        double remainingBudget = budget - totalExpenses;
        budgetLabel.setText(String.format("Monthly Budget: $%.2f (%s $%.2f)",
                budget,
                remainingBudget >= 0 ? "Remaining:" : "Over by:",
                Math.abs(remainingBudget)));
        budgetLabel.setForeground(remainingBudget >= 0 ?
                new Color(0, 100, 0) : new Color(150, 0, 0));
    }

    private boolean updateUserBudget(int userId, double budget) {
        String sql = "UPDATE users SET monthly_budget = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, budget);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        refreshData();
    }
    
    //Calculates total income for the specified month from transactions table.
    //Returns total income for the month
    private double calculateMonthlyIncome(int userId, String month) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0) as total 
            FROM transactions 
            WHERE user_id = ? 
            AND type = 'Income' 
            AND date LIKE ?
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, month + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble("total");
        }
    }

    //Calculates total expenses including both regular expenses and paid bills for the specified month.
    //Returns combined total of expenses and paid bills
    private double calculateMonthlyExpenses(int userId, String month) throws SQLException {
        // First, get regular expenses
        String expenseSql = """
            SELECT COALESCE(SUM(amount), 0.0) as expense_total 
            FROM transactions 
            WHERE user_id = ? 
            AND type = 'Expense' 
            AND date LIKE ?
        """;
        // Then, get paid bills for the month
        String billsSql = """
            SELECT COALESCE(SUM(amount), 0.0) as bills_total 
            FROM bills 
            WHERE user_id = ? 
            AND is_paid = 1 
            AND date LIKE ?
        """;

        double totalExpenses = 0.0;

        // Get regular expenses
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(expenseSql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, month + "%");
            ResultSet rs = pstmt.executeQuery();
            totalExpenses += rs.getDouble("expense_total");
        }

        // Get paid bills
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(billsSql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, month + "%");
            ResultSet rs = pstmt.executeQuery();
            totalExpenses += rs.getDouble("bills_total");
        }

        return totalExpenses;
    }

    //Updates all financial information displayed in the panel.
    //Calculates current month's income, expenses, and savings using background processing to prevent UI freezing.
    public void refreshData() {
        if (currentUser == null) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private double monthlyIncome;
            private double monthlyExpenses;
            private double monthlyBills;
            private double previousSavings;
            private double currentBalance;

            @Override
            protected Void doInBackground() {
                try {
                    String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    monthlyIncome = calculateMonthlyIncome(currentUser.getId(), currentMonth);
                    monthlyExpenses = calculateRegularExpenses(currentUser.getId(), currentMonth);
                    monthlyBills = calculatePaidBills(currentUser.getId(), currentMonth);
                    previousSavings = getPreviousMonthSavings(currentUser.getId());
                    currentBalance = monthlyIncome - (monthlyExpenses + monthlyBills) + previousSavings;
                } catch (SQLException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(DetailsPanel.this,
                                "Error refreshing data: " + e.getMessage(),
                                "Refresh Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                nameLabel.setText("Current User's Login: " + currentUser.getName());
                balanceLabel.setText(String.format("Current Balance: $%.2f", currentBalance));
                incomeLabel.setText(String.format("This Month's Income: $%.2f", monthlyIncome));
                expenseLabel.setText(String.format("This Month's Expenses: $%.2f (Regular: $%.2f + Bills: $%.2f)",
                        (monthlyExpenses + monthlyBills), monthlyExpenses, monthlyBills));
                savingsLabel.setText(String.format("Previous Month's Savings: $%.2f", previousSavings));

                DetailsPanel.this.monthlyExpenses = monthlyExpenses;
                DetailsPanel.this.monthlyBills = monthlyBills;

                if (currentUser.getMonthlyBudget() > 0) {
                    updateBudgetLabel(currentUser.getMonthlyBudget());
                }

                try {
                    updateUserBalance(currentUser.getId(), currentBalance);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private double calculateRegularExpenses(int userId, String month) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0) as total 
            FROM transactions 
            WHERE user_id = ? 
            AND type = 'Expense' 
            AND date LIKE ?
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, month + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble("total");
        }
    }

    private double calculatePaidBills(int userId, String month) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0) as total 
            FROM bills 
            WHERE user_id = ? 
            AND is_paid = 1 
            AND date LIKE ?
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, month + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble("total");
        }
    }

    //Calculates savings from the previous month by finding the difference between income and expenses.
    private double getPreviousMonthSavings(int userId) throws SQLException {
        String previousMonth = LocalDateTime.now()
                .minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM"));

        String sql = """
            SELECT COALESCE(
                (SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Income' AND date LIKE ?) -
                COALESCE((SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = 'Expense' AND date LIKE ?), 0),
                0
            ) as savings
        """;

        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, previousMonth + "%");
            pstmt.setInt(3, userId);
            pstmt.setString(4, previousMonth + "%");
            ResultSet rs = pstmt.executeQuery();
            return rs.getDouble("savings");
        }
    }

    private void updateUserBalance(int userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET current_balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
            mainFrame.showLoginPanel();
        }
    }

    private void handleReset() {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all data? This cannot be undone!",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                resetUserData(currentUser.getId());
                refreshData();
                JOptionPane.showMessageDialog(this,
                        "All data has been reset successfully.",
                        "Reset Complete",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error resetting data: " + e.getMessage(),
                        "Reset Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //Resets all user-related data including transactions, bills, and budget.
    //This operation cannot be undone.
    private void resetUserData(int userId) throws SQLException {
        // Delete all user-related data
        String[] tables = {"transactions", "bills", "budgets"};
        for (String table : tables) {
            String sql = "DELETE FROM " + table + " WHERE user_id = ?";
            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
        }

        // Reset user balance
        String sql = "UPDATE users SET current_balance = 0, monthly_budget = 0 WHERE id = ?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    //Handles the export of financial data to CSV format.
    //Allows user to choose save location and generates detailed transaction report.
    private void handleExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Financial Data");
        fileChooser.setSelectedFile(new File("financial_report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            exportToCSV(file);
        }
    }

    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write headers
            writer.println("Date,Type,Category,Amount,Description");

            // Export transactions
            String sql = """
                SELECT date, type, category, amount, description 
                FROM transactions 
                WHERE user_id = ? 
                ORDER BY date DESC
            """;

            try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, currentUser.getId());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    writer.printf("%s,%s,%s,%.2f,%s%n",
                            rs.getString("date"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getDouble("amount"),
                            rs.getString("description").replace(",", ";")  // Escape commas in description
                    );
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Data exported successfully to " + file.getName(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error exporting data: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}