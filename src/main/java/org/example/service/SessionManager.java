package org.example.service;

import org.example.model.User;

/**
 * Менеджер сессий для хранения текущего авторизованного пользователя
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Устанавливает текущего пользователя
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Получает текущего пользователя
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Проверяет, авторизован ли пользователь
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Выход из системы
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Получить ID текущего пользователя
     */
    public Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
}

