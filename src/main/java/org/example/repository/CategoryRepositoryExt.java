package org.example.repository;

import org.example.model.Category;
import java.util.List;

/**
 * Расширенный интерфейс для работы с категориями по пользователям
 */
public interface CategoryRepositoryExt extends CategoryRepository {
    
    /**
     * Получить все категории пользователя
     */
    List<Category> findByUserId(Long userId);
}

