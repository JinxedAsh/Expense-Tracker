package com.expensetracker.models;

// This class demonstrates:
// 1. Inheritance: Extends Transaction
// 2. Polymorphism: Overrides getType()
public class Income extends Transaction {
    public Income(double amount, String date, String category, String description) {
        super(amount, date, category, description);
    }

    // Polymorphism through method override
    @Override
    public String getType() {
        return "Income";
    }
}