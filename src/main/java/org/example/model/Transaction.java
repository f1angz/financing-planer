package org.example.model;

import java.time.LocalDateTime;

public class Transaction {
    private String description;
    private double amount;
    private LocalDateTime date;
    private Category category;
    private TransactionType type;

    public Transaction(String description, double amount, LocalDateTime date, Category category, TransactionType type) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}


