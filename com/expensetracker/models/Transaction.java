package com.expensetracker.models;

// This class demonstrates multiple OOP principles:
// 1. Encapsulation: Through private fields with getters/setters
// 2. Abstraction: As an abstract class
public abstract class Transaction {
    private int id;
    private double amount;
    private String date;
    private String category;
    private String description;

    public Transaction(double amount, String date, String category, String description) {
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    // Getters and Setters (Encapsulation)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Abstract method demonstrating abstraction
    public abstract String getType();
}