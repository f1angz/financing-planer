package org.example.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.repository.impl.UserRepositoryImpl;

import java.util.Optional;

/**
 * Сервис для аутентификации и регистрации пользователей
 */
public class AuthService {
    
    private static AuthService instance;
    private final UserRepository userRepository;
    private final BCrypt.Hasher hasher;
    private final BCrypt.Verifyer verifyer;
    
    private AuthService() {
        this.userRepository = new UserRepositoryImpl();
        this.hasher = BCrypt.withDefaults();
        this.verifyer = BCrypt.verifyer();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    /**
     * Регистрация нового пользователя
     */
    public User register(String username, String password, String email) throws Exception {
        // Проверяем, не занято ли имя
        if (userRepository.existsByUsername(username)) {
            throw new Exception("Пользователь с таким именем уже существует");
        }
        
        // Валидация
        if (username.length() < 3) {
            throw new Exception("Имя пользователя должно быть не менее 3 символов");
        }
        
        if (password.length() < 6) {
            throw new Exception("Пароль должен быть не менее 6 символов");
        }
        
        // Хешируем пароль
        String passwordHash = hashPassword(password);
        
        // Создаём пользователя
        User user = new User(username, passwordHash, email);
        userRepository.save(user);
        
        return user;
    }
    
    /**
     * Авторизация пользователя
     */
    public User login(String username, String password) throws Exception {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new Exception("Неверное имя пользователя или пароль");
        }
        
        User user = userOpt.get();
        
        // Проверяем пароль
        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new Exception("Неверное имя пользователя или пароль");
        }
        
        return user;
    }
    
    /**
     * Хеширование пароля с использованием BCrypt
     */
    private String hashPassword(String password) {
        return hasher.hashToString(12, password.toCharArray());
    }
    
    /**
     * Проверка пароля
     */
    private boolean verifyPassword(String password, String hash) {
        BCrypt.Result result = verifyer.verify(password.toCharArray(), hash);
        return result.verified;
    }
    
    /**
     * Выход из системы
     */
    public void logout() {
        SessionManager sessionManager = SessionManager.getInstance();
        DataService dataService = DataService.getInstance();
        
        // Очищаем сессию
        sessionManager.logout();
        
        // Очищаем данные
        dataService.clear();
    }
}

