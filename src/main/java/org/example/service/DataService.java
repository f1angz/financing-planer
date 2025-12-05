package org.example.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.model.Category;
import org.example.model.Transaction;
import org.example.model.TransactionType;

import java.time.LocalDateTime;

public class DataService {
    private static DataService instance;
    private ObservableList<Transaction> transactions;
    private ObservableList<Category> categories;

    private DataService() {
        transactions = FXCollections.observableArrayList();
        categories = FXCollections.observableArrayList();
        initializeDefaultData();
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    private void initializeDefaultData() {
        // Создаём категории доходов
        Category salary = new Category("Зарплатная плата", "#00FFA3", TransactionType.INCOME);
        Category bonus = new Category("Премия", "#00D9FF", TransactionType.INCOME);
        Category scholarship = new Category("Стипендия", "#ADFF00", TransactionType.INCOME);
        
        // Создаём категории расходов
        Category food = new Category("Продукты питания", "#00FFA3", TransactionType.EXPENSE);
        Category clothes = new Category("Одежда", "#00D9FF", TransactionType.EXPENSE);
        Category digital = new Category("Цифровые товары", "#FFEB3B", TransactionType.EXPENSE);

        categories.addAll(salary, bonus, scholarship, food, clothes, digital);

        // Создаём примеры транзакций
        transactions.add(new Transaction("Получена зарплата", 50000, LocalDateTime.now().minusDays(5), salary, TransactionType.INCOME));
        transactions.add(new Transaction("Премия за проект", 15000, LocalDateTime.now().minusDays(10), bonus, TransactionType.INCOME));
        transactions.add(new Transaction("Стипендия", 3000, LocalDateTime.now().minusDays(1), scholarship, TransactionType.INCOME));
        
        transactions.add(new Transaction("Покупка продуктов в супермаркете", -3500, LocalDateTime.now().minusDays(2), food, TransactionType.EXPENSE));
        transactions.add(new Transaction("Новая куртка", -8000, LocalDateTime.now().minusDays(4), clothes, TransactionType.EXPENSE));
        transactions.add(new Transaction("Подписка Netflix", -990, LocalDateTime.now().minusDays(7), digital, TransactionType.EXPENSE));
        transactions.add(new Transaction("Покупка в магазине", -2100, LocalDateTime.now().minusDays(3), food, TransactionType.EXPENSE));
    }

    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }
}


