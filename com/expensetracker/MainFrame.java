package com.expensetracker;

import com.expensetracker.gui.*;
import com.expensetracker.models.User;

import javax.swing.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JTabbedPane tabbedPane;
    private LoginPanel loginPanel;
    private IncomePanel incomePanel;
    private ExpensePanel expensePanel;
    private BillsPanel billsPanel;
    private DetailsPanel detailsPanel;
    private AboutPanel aboutPanel;

    public MainFrame() {
        setTitle("Personal Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initializeComponents();
        showLoginPanel();
    }

    private void initializeComponents() {
        loginPanel = new LoginPanel(this);
        tabbedPane = new JTabbedPane();

        // Initialize all panels but only add them after successful login
        incomePanel = new IncomePanel();
        expensePanel = new ExpensePanel();
        billsPanel = new BillsPanel();
        detailsPanel = new DetailsPanel();
        aboutPanel = new AboutPanel();
    }

    public void showLoginPanel() {
        getContentPane().removeAll();
        getContentPane().add(loginPanel);
        revalidate();
        repaint();
    }

    public void showMainApplication(User user) {
        this.currentUser = user;
        getContentPane().removeAll();

        // Set user for all panels that need it
        incomePanel.setUser(user);
        expensePanel.setUser(user);
        billsPanel.setUser(user);
        detailsPanel.setUser(user);

        tabbedPane.removeAll();
        tabbedPane.addTab("Income", incomePanel);
        tabbedPane.addTab("Expenses", expensePanel);
        tabbedPane.addTab("Bills", billsPanel);
        tabbedPane.addTab("Details", detailsPanel);
        tabbedPane.addTab("About", aboutPanel);

        getContentPane().add(tabbedPane);
        revalidate();
        repaint();
    }

    public void refreshDetailsPanel() {
        if (detailsPanel != null) {
            detailsPanel.refreshData();
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}