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
    
    public void updateTransaction(Transaction transaction) {
        transactionRepository.update(transaction);
        // Обновляем в списке
        int index = transactions.indexOf(transaction);
        if (index >= 0) {
            transactions.set(index, transaction);
        }
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
    
    public void updateCategory(Category category) {
        categoryRepository.update(category);
        // Обновляем в списке
        int index = categories.indexOf(category);
        if (index >= 0) {
            categories.set(index, category);
        }
        // Обновляем кэш
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
        Category salary = new Category("Заработанная плата", "#00FFA3", TransactionType.INCOME);
        salary.setUserId(userId);
        Category bonus = new Category("Премия", "#00D9FF", TransactionType.INCOME);
        bonus.setUserId(userId);
        Category investments = new Category("Инвестиции", "#ADFF00", TransactionType.INCOME);
        investments.setUserId(userId);
        Category gift = new Category("Подарок", "#FFD700", TransactionType.INCOME);
        gift.setUserId(userId);
        
        // Создаём категории расходов
        Category food = new Category("Продукты", "#FF4757", TransactionType.EXPENSE);
        food.setUserId(userId);
        Category digital = new Category("Цифровые товары", "#FFA502", TransactionType.EXPENSE);
        digital.setUserId(userId);
        Category sport = new Category("Спорт", "#1E90FF", TransactionType.EXPENSE);
        sport.setUserId(userId);
        Category utilities = new Category("Коммунальные услуги", "#48DBB4", TransactionType.EXPENSE);
        utilities.setUserId(userId);
        Category rent = new Category("Плата за квартиру", "#FF6348", TransactionType.EXPENSE);
        rent.setUserId(userId);
        Category credit = new Category("Кредит", "#9B59B6", TransactionType.EXPENSE);
        credit.setUserId(userId);
        Category taxes = new Category("Налоги", "#FF6B9D", TransactionType.EXPENSE);
        taxes.setUserId(userId);
        
        // Сохраняем в БД
        categoryRepository.save(salary);
        categoryRepository.save(bonus);
        categoryRepository.save(investments);
        categoryRepository.save(gift);
        categoryRepository.save(food);
        categoryRepository.save(digital);
        categoryRepository.save(sport);
        categoryRepository.save(utilities);
        categoryRepository.save(rent);
        categoryRepository.save(credit);
        categoryRepository.save(taxes);
        
        categories.addAll(salary, bonus, investments, gift, food, digital, sport, utilities, rent, credit, taxes);
        
        // Обновляем кэш
        categoryCache.put(salary.getId(), salary);
        categoryCache.put(bonus.getId(), bonus);
        categoryCache.put(investments.getId(), investments);
        categoryCache.put(gift.getId(), gift);
        categoryCache.put(food.getId(), food);
        categoryCache.put(digital.getId(), digital);
        categoryCache.put(sport.getId(), sport);
        categoryCache.put(utilities.getId(), utilities);
        categoryCache.put(rent.getId(), rent);
        categoryCache.put(credit.getId(), credit);
        categoryCache.put(taxes.getId(), taxes);
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


