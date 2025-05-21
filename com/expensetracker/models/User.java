package com.expensetracker.models;

// This class demonstrates Encapsulation through private fields and public methods
public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private double monthlyBudget;
    private double currentBalance;

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.currentBalance = 0.0;
        this.monthlyBudget = 0.0;
    }

    // Getters and Setters (Encapsulation)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }
}
