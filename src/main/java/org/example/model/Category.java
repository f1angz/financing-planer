package org.example.model;

public class Category {
    private Long id;
    private String name;
    private String color;
    private TransactionType type;
    private Long userId;

    public Category(String name, String color, TransactionType type) {
        this.name = name;
        this.color = color;
        this.type = type;
    }

    public Category(Long id, String name, String color, TransactionType type) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.type = type;
    }

    public Category(Long id, String name, String color, TransactionType type, Long userId) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.type = type;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}


