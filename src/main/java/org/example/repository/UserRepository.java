package org.example.repository;

import org.example.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с пользователями в БД
 */
public interface UserRepository {
    
    /**
     * Сохранить нового пользователя
     */
    void save(User user);
    
    /**
     * Получить пользователя по ID
     */
    Optional<User> findById(Long id);
    
    /**
     * Получить пользователя по имени
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Получить всех пользователей
     */
    List<User> findAll();
    
    /**
     * Проверить существование пользователя по имени
     */
    boolean existsByUsername(String username);
}

