package org.example.repository;

import org.example.model.Category;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с категориями в БД
 * Позволяет легко менять реализацию (SQLite -> PostgreSQL)
 */
public interface CategoryRepository {
    
    /**
     * Сохранить новую категорию
     */
    void save(Category category);
    
    /**
     * Обновить существующую категорию
     */
    void update(Category category);
    
    /**
     * Удалить категорию
     */
    void delete(Category category);
    
    /**
     * Получить категорию по ID
     */
    Optional<Category> findById(Long id);
    
    /**
     * Получить все категории
     */
    List<Category> findAll();
    
    /**
     * Удалить все категории
     */
    void deleteAll();
}


