package com.expensetracker.gui;

import com.expensetracker.Main;
import com.expensetracker.MainFrame;
import com.expensetracker.database.DatabaseManager;
import com.expensetracker.models.Expense;
import com.expensetracker.models.Transaction;
import com.expensetracker.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExpensePanel extends JPanel {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private DatabaseManager dbManager;
    private User currentUser;

    public ExpensePanel() {
        dbManager = Main.getDatabase();
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create table model with column names
        String[] columns = {"Date", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Double.class; // Amount column
                return String.class;
            }
        };

        // Create table
        expenseTable = new JTable(tableModel);
        expenseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expenseTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        expenseTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Date
        expenseTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Category
        expenseTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Amount
        expenseTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Description

        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Expense");
        deleteButton = new JButton("Delete Selected");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);

        // Add components to panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Setup button listeners
        setupListeners();
    }

    private void setupListeners() {
        addButton.addActionListener(e -> showAddExpenseDialog());
        deleteButton.addActionListener(e -> deleteSelectedExpense());
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadExpenseData();
    }

    private void loadExpenseData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // Load expense transactions from database
        List<Transaction> expenses = dbManager.getTransactions(currentUser.getId(), "Expense");
        for (Transaction expense : expenses) {
            tableModel.addRow(new Object[]{
                    expense.getDate(),
                    expense.getCategory(),
                    expense.getAmount(),
                    expense.getDescription()
            });
        }
    }

    private void showAddExpenseDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Expense", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create input fields
        JTextField amountField = new JTextField(10);
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "Food", "Transportation", "Housing", "Utilities",
                "Entertainment", "Healthcare", "Education", "Shopping", "Other"
        });
        JTextField descriptionField = new JTextField(20);

        // Add components to dialog
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        dialog.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        dialog.add(categoryBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        dialog.add(descriptionField, gbc);

        JButton submitButton = new JButton("Add");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = (String) categoryBox.getSelectedItem();
                String description = descriptionField.getText();
                String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Expense expense = new Expense(amount, date, category, description);

                // Save to database
                if (dbManager.addTransaction(currentUser.getId(), expense)) {
                    tableModel.addRow(new Object[]{date, category, amount, description});
                    dialog.dispose();

                    // Refresh the details panel if it's visible
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    mainFrame.refreshDetailsPanel();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to save expense entry.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter a valid amount.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow >= 0) {
            String date = (String) tableModel.getValueAt(selectedRow, 0);
            String category = (String) tableModel.getValueAt(selectedRow, 1);
            double amount = (double) tableModel.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this expense entry?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete from database
                if (dbManager.deleteTransaction(currentUser.getId(), "Expense", date, amount)) {
                    tableModel.removeRow(selectedRow);

                    // Refresh the details panel if it's visible
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    mainFrame.refreshDetailsPanel();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete expense entry.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an expense entry to delete.",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to refresh the panel's data
    public void refreshData() {
        if (currentUser != null) {
            loadExpenseData();
        }
    }
}