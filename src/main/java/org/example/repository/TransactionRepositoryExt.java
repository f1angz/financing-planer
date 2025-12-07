package org.example.repository;

import org.example.model.Transaction;
import java.util.List;

/**
 * Расширенный интерфейс для работы с транзакциями по пользователям
 */
public interface TransactionRepositoryExt extends TransactionRepository {
    
    /**
     * Получить все транзакции пользователя
     */
    List<Transaction> findByUserId(Long userId);
}

