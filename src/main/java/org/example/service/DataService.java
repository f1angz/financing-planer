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
        
        // Если категорий нет, создаём примеры
        if (categories.isEmpty()) {
            initializeDefaultData();
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
     * Инициализация БД примерами данных (только при первом запуске)
     */
    private void initializeDefaultData() {
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

        // Создаём примеры транзакций
        Transaction t1 = new Transaction("Получена зарплата", 50000, LocalDateTime.now().minusDays(5), salary, TransactionType.INCOME);
        t1.setUserId(userId);
        Transaction t2 = new Transaction("Премия за проект", 15000, LocalDateTime.now().minusDays(10), bonus, TransactionType.INCOME);
        t2.setUserId(userId);
        Transaction t3 = new Transaction("Стипендия", 3000, LocalDateTime.now().minusDays(1), scholarship, TransactionType.INCOME);
        t3.setUserId(userId);
        Transaction t4 = new Transaction("Покупка продуктов в супермаркете", -3500, LocalDateTime.now().minusDays(2), food, TransactionType.EXPENSE);
        t4.setUserId(userId);
        Transaction t5 = new Transaction("Новая куртка", -8000, LocalDateTime.now().minusDays(4), clothes, TransactionType.EXPENSE);
        t5.setUserId(userId);
        Transaction t6 = new Transaction("Подписка Netflix", -990, LocalDateTime.now().minusDays(7), digital, TransactionType.EXPENSE);
        t6.setUserId(userId);
        Transaction t7 = new Transaction("Покупка в магазине", -2100, LocalDateTime.now().minusDays(3), food, TransactionType.EXPENSE);
        t7.setUserId(userId);
        
        transactionRepository.save(t1);
        transactionRepository.save(t2);
        transactionRepository.save(t3);
        transactionRepository.save(t4);
        transactionRepository.save(t5);
        transactionRepository.save(t6);
        transactionRepository.save(t7);
        
        transactions.addAll(t1, t2, t3, t4, t5, t6, t7);
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


