package org.example.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.model.Category;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.repository.CategoryRepository;
import org.example.repository.TransactionRepository;
import org.example.repository.impl.CategoryRepositoryImpl;
import org.example.repository.impl.TransactionRepositoryImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Сервис для работы с данными приложения
 * Использует репозитории для работы с БД
 */
public class DataService {
    private static DataService instance;
    private ObservableList<Transaction> transactions;
    private ObservableList<Category> categories;
    
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    
    // Кэш категорий для быстрого доступа по ID
    private Map<Long, Category> categoryCache;

    private DataService() {
        transactionRepository = new TransactionRepositoryImpl();
        categoryRepository = new CategoryRepositoryImpl();
        categoryCache = new HashMap<>();
        
        transactions = FXCollections.observableArrayList();
        categories = FXCollections.observableArrayList();
        
        loadData();
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    /**
     * Загрузка данных из БД
     */
    private void loadData() {
        // Загружаем категории
        List<Category> loadedCategories = categoryRepository.findAll();
        categories.setAll(loadedCategories);
        
        // Обновляем кэш
        categoryCache.clear();
        for (Category category : loadedCategories) {
            categoryCache.put(category.getId(), category);
        }
        
        // Загружаем транзакции
        List<Transaction> loadedTransactions = transactionRepository.findAll();
        
        // Связываем транзакции с категориями
        for (Transaction transaction : loadedTransactions) {
            if (transaction.getCategoryId() != null) {
                Category category = categoryCache.get(transaction.getCategoryId());
                if (category != null) {
                    transaction.setCategory(category);
                }
            }
        }
        
        transactions.setAll(loadedTransactions);
    }

    public ObservableList<Transaction> getTransactions() {
        return transactions;
    }

    public ObservableList<Category> getCategories() {
        return categories;
    }

    public void addTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
        transactions.add(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
        transactions.remove(transaction);
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
        categories.add(category);
        categoryCache.put(category.getId(), category);
    }

    public void removeCategory(Category category) {
        categoryRepository.delete(category);
        categories.remove(category);
        categoryCache.remove(category.getId());
    }
    
    /**
     * Получить категорию по ID
     */
    public Category getCategoryById(Long id) {
        return categoryCache.get(id);
    }
    
    /**
     * Перезагрузить данные из БД
     */
    public void reload() {
        loadData();
    }
}


