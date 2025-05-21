package com.expensetracker.models;

// This class demonstrates:
// 1. Inheritance: Extends Transaction
// 2. Polymorphism: Overrides getType()
public class Bill extends Transaction {
    private String dueDate;
    private boolean isPaid;

    public Bill(double amount, String date, String category, String description, String dueDate) {
        super(amount, date, category, description);
        this.dueDate = dueDate;
        this.isPaid = false;
    }

    // Additional getters and setters (Encapsulation)
    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    // Polymorphism through method override
    @Override
    public String getType() {
        return "Bill";
    }
}
