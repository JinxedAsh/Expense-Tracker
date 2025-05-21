package com.expensetracker.models;

// This class demonstrates:
// 1. Inheritance: Extends Transaction
// 2. Polymorphism: Overrides getType()
public class Expense extends Transaction {
    public Expense(double amount, String date, String category, String description) {
        super(amount, date, category, description);
    }

    // Polymorphism through method override
    @Override
    public String getType() {
        return "Expense";
    }
}