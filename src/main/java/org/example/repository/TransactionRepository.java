package org.example.repository;

import org.example.model.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с транзакциями в БД
 * Позволяет легко менять реализацию (SQLite -> PostgreSQL)
 */
public interface TransactionRepository {
    
    /**
     * Сохранить новую транзакцию
     */
    void save(Transaction transaction);
    
    /**
     * Обновить существующую транзакцию
     */
    void update(Transaction transaction);
    
    /**
     * Удалить транзакцию
     */
    void delete(Transaction transaction);
    
    /**
     * Получить транзакцию по ID
     */
    Optional<Transaction> findById(Long id);
    
    /**
     * Получить все транзакции
     */
    List<Transaction> findAll();
    
    /**
     * Удалить все транзакции
     */
    void deleteAll();
}


