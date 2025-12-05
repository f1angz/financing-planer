package org.example.model;

import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private String description;
    private double amount;
    private LocalDateTime date;
    private Category category;
    private Long categoryId;  // Для связи с БД
    private TransactionType type;

    public Transaction(String description, double amount, LocalDateTime date, Category category, TransactionType type) {
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.categoryId = category != null ? category.getId() : null;
        this.type = type;
    }

    public Transaction(Long id, String description, double amount, LocalDateTime date, Long categoryId, TransactionType type) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        this.categoryId = category != null ? category.getId() : null;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}


