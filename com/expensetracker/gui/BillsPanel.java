package com.expensetracker.gui;

import com.expensetracker.Main;
import com.expensetracker.MainFrame;
import com.expensetracker.database.DatabaseManager;
import com.expensetracker.models.Bill;
import com.expensetracker.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillsPanel extends JPanel {
    private JTable billsTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton markPaidButton;
    private DatabaseManager dbManager;
    private User currentUser;

    public BillsPanel() {
        dbManager = Main.getDatabase();
        setLayout(new BorderLayout());
        initializeComponents();
    }

    private void initializeComponents() {
        // Create table model with column names
        String[] columns = {"Due Date", "Category", "Amount", "Description", "Status"};
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
        billsTable = new JTable(tableModel);
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        billsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Due Date
        billsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Category
        billsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Amount
        billsTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Description
        billsTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status

        // Create scroll pane for table
        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add Bill");
        deleteButton = new JButton("Delete Selected");
        markPaidButton = new JButton("Mark as Paid");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(markPaidButton);

        // Add components to panel
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Setup button listeners
        setupListeners();
    }

    private void setupListeners() {
        addButton.addActionListener(e -> showAddBillDialog());
        deleteButton.addActionListener(e -> deleteSelectedBill());
        markPaidButton.addActionListener(e -> markBillAsPaid());
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadBillsData();
    }

    private void loadBillsData() {
        // Clear existing data
        tableModel.setRowCount(0);

        // TODO: Implement loading bills from database
        // This will be implemented when we add the bills table to the database
        List<Bill> bills = dbManager.getBills(currentUser.getId());
        for (Bill bill : bills) {
            tableModel.addRow(new Object[]{
                    bill.getDueDate(),
                    bill.getCategory(),
                    bill.getAmount(),
                    bill.getDescription(),
                    bill.isPaid() ? "Paid" : "Unpaid"
            });
        }
    }

    private void showAddBillDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add Bill", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Create input fields
        JTextField amountField = new JTextField(10);
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{
                "Rent", "Utilities", "Phone", "Internet",
                "Insurance", "Credit Card", "Loan", "Other"
        });
        JTextField descriptionField = new JTextField(20);
        JTextField dueDateField = new JTextField(10);
        dueDateField.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

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

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        dialog.add(dueDateField, gbc);

        JButton submitButton = new JButton("Add");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        dialog.add(submitButton, gbc);

        submitButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String category = (String) categoryBox.getSelectedItem();
                String description = descriptionField.getText();
                String dueDate = dueDateField.getText();
                String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                Bill bill = new Bill(amount, currentDate, category, description, dueDate);

                // Save to database
                if (dbManager.addBill(currentUser.getId(), bill)) {
                    tableModel.addRow(new Object[]{dueDate, category, amount, description, "Unpaid"});
                    dialog.dispose();

                    // Refresh the details panel if it's visible
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    mainFrame.refreshDetailsPanel();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Failed to save bill entry.",
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

    private void deleteSelectedBill() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String dueDate = (String) tableModel.getValueAt(selectedRow, 0);
            String category = (String) tableModel.getValueAt(selectedRow, 1);
            double amount = (double) tableModel.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this bill?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Delete from database
                if (dbManager.deleteBill(currentUser.getId(), dueDate, amount)) {
                    tableModel.removeRow(selectedRow);

                    // Refresh the details panel if it's visible
                    MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                    mainFrame.refreshDetailsPanel();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete bill entry.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to delete.",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void markBillAsPaid() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String dueDate = (String) tableModel.getValueAt(selectedRow, 0);
            double amount = (double) tableModel.getValueAt(selectedRow, 2);

            // Update in database
            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (dbManager.updateBillStatus(currentUser.getId(), dueDate, amount, true, currentDate)) {
                tableModel.setValueAt("Paid", selectedRow, 4);

                // Refresh the details panel if it's visible
                MainFrame mainFrame = (MainFrame) SwingUtilities.getWindowAncestor(this);
                mainFrame.refreshDetailsPanel();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update bill status.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill to mark as paid.",
                    "Selection Required",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Method to refresh the panel's data
    public void refreshData() {
        if (currentUser != null) {
            loadBillsData();
        }
    }
}