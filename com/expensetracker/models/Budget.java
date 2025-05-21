package com.expensetracker.models;

// This class demonstrates Encapsulation through private fields and public methods
public class Budget {
    private int id;
    private int userId;
    private String category;
    private double limit;
    private double spent;
    private String month; // Format: YYYY-MM

    public Budget(int userId, String category, double limit, String month) {
        this.userId = userId;
        this.category = category;
        this.limit = limit;
        this.spent = 0.0;
        this.month = month;
    }

    // Getters and Setters (Encapsulation)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }

    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    // Business logic methods
    public boolean isOverBudget() {
        return spent > limit;
    }

    public double getRemainingBudget() {
        return limit - spent;
    }

    public double getSpentPercentage() {
        return (spent / limit) * 100;
    }
}