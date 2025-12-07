package org.example.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.model.Category;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.repository.CategoryRepositoryExt;
import org.example.repository.TransactionRepositoryExt;
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
    
    private final TransactionRepositoryExt transactionRepository;
    private final CategoryRepositoryExt categoryRepository;
    private final SessionManager sessionManager;
    
    // Кэш категорий для быстрого доступа по ID
    private Map<Long, Category> categoryCache;

    private DataService() {
        transactionRepository = new TransactionRepositoryImpl();
        categoryRepository = new CategoryRepositoryImpl();
        sessionManager = SessionManager.getInstance();
        categoryCache = new HashMap<>();
        
        transactions = FXCollections.observableArrayList();
        categories = FXCollections.observableArrayList();
    }

    public static DataService getInstance() {
        if (instance == null) {
            instance = new DataService();
        }
        return instance;
    }

    /**
     * Загрузка данных из БД для текущего пользователя
     */
    public void loadData() {
        Long userId = sessionManager.getCurrentUserId();
        
        if (userId == null) {
            System.out.println("No user logged in");
            return;
        }
        
        // Загружаем категории пользователя
        List<Category> loadedCategories = categoryRepository.findByUserId(userId);
        categories.setAll(loadedCategories);
        
        // Обновляем кэш
        categoryCache.clear();
        for (Category category : loadedCategories) {
            categoryCache.put(category.getId(), category);
        }
        
        // Если категорий нет, создаём только категории по умолчанию (без транзакций)
        if (categories.isEmpty()) {
            initializeDefaultCategories();
            // После инициализации перезагружаем категории в кэш
            loadedCategories = categoryRepository.findByUserId(userId);
            categories.setAll(loadedCategories);
            categoryCache.clear();
            for (Category category : loadedCategories) {
                categoryCache.put(category.getId(), category);
            }
        }
        
        // Загружаем транзакции пользователя
        List<Transaction> loadedTransactions = transactionRepository.findByUserId(userId);
        
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
        // Устанавливаем userId
        transaction.setUserId(sessionManager.getCurrentUserId());
        transactionRepository.save(transaction);
        transactions.add(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
        transactions.remove(transaction);
    }

    public void addCategory(Category category) {
        // Устанавливаем userId
        category.setUserId(sessionManager.getCurrentUserId());
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
     * Инициализация категорий по умолчанию (без транзакций)
     */
    private void initializeDefaultCategories() {
        Long userId = sessionManager.getCurrentUserId();
        
        // Создаём категории доходов
        Category salary = new Category("Зарплатная плата", "#00FFA3", TransactionType.INCOME);
        salary.setUserId(userId);
        Category bonus = new Category("Премия", "#00D9FF", TransactionType.INCOME);
        bonus.setUserId(userId);
        Category scholarship = new Category("Стипендия", "#ADFF00", TransactionType.INCOME);
        scholarship.setUserId(userId);
        
        // Создаём категории расходов
        Category food = new Category("Продукты питания", "#00FFA3", TransactionType.EXPENSE);
        food.setUserId(userId);
        Category clothes = new Category("Одежда", "#00D9FF", TransactionType.EXPENSE);
        clothes.setUserId(userId);
        Category digital = new Category("Цифровые товары", "#FFEB3B", TransactionType.EXPENSE);
        digital.setUserId(userId);
        
        // Сохраняем в БД
        categoryRepository.save(salary);
        categoryRepository.save(bonus);
        categoryRepository.save(scholarship);
        categoryRepository.save(food);
        categoryRepository.save(clothes);
        categoryRepository.save(digital);
        
        categories.addAll(salary, bonus, scholarship, food, clothes, digital);
        
        // Обновляем кэш
        categoryCache.put(salary.getId(), salary);
        categoryCache.put(bonus.getId(), bonus);
        categoryCache.put(scholarship.getId(), scholarship);
        categoryCache.put(food.getId(), food);
        categoryCache.put(clothes.getId(), clothes);
        categoryCache.put(digital.getId(), digital);
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
    
    /**
     * Очистить данные при выходе
     */
    public void clear() {
        transactions.clear();
        categories.clear();
        categoryCache.clear();
    }
}


