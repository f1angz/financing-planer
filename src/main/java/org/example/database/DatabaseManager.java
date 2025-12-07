package org.example.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Менеджер для управления подключением к БД
 * Использует HikariCP connection pool для эффективной работы
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private HikariDataSource dataSource;
    
    private DatabaseManager() {
        initializeDataSource();
        initializeDatabase();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DatabaseConfig.getUrl());
        
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    /**
     * Инициализация схемы БД (создание таблиц)
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Проверяем, нужна ли миграция
            if (needsMigration(conn)) {
                System.out.println("Migrating database schema...");
                migrateDatabase(conn);
                System.out.println("Migration completed successfully");
                return;
            }
            
            // Создаём таблицу пользователей
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    email TEXT,
                    created_at TEXT NOT NULL
                )
            """;
            
            stmt.execute(createUsersTable);
            
            // Создаём таблицу категорий
            String createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            
            stmt.execute(createCategoriesTable);
            
            // Создаём таблицу транзакций
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    category_id INTEGER,
                    type TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            
            stmt.execute(createTransactionsTable);
            
            System.out.println("Database initialized successfully");
            
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Проверяет, нужна ли миграция БД
     */
    private boolean needsMigration(Connection conn) throws SQLException {
        // Проверяем существование старых таблиц без user_id
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='categories'")) {
            
            if (rs.next()) {
                // Таблица categories существует, проверяем наличие user_id
                try (ResultSet columns = stmt.executeQuery("PRAGMA table_info(categories)")) {
                    while (columns.next()) {
                        if ("user_id".equals(columns.getString("name"))) {
                            return false; // user_id уже есть, миграция не нужна
                        }
                    }
                    return true; // user_id нет, нужна миграция
                }
            }
        }
        return false; // Таблицы нет, миграция не нужна
    }
    
    /**
     * Мигрирует старую БД к новой схеме с user_id
     */
    private void migrateDatabase(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            
            // Создаём таблицу users если её нет
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    email TEXT,
                    created_at TEXT NOT NULL
                )
            """;
            stmt.execute(createUsersTable);
            
            // Создаём пользователя по умолчанию для старых данных
            String insertDefaultUser = """
                INSERT OR IGNORE INTO users (id, username, password_hash, email, created_at)
                VALUES (1, 'default_user', '$2a$12$dummy', NULL, datetime('now'))
            """;
            stmt.execute(insertDefaultUser);
            
            // Переименовываем старые таблицы
            stmt.execute("ALTER TABLE categories RENAME TO categories_old");
            stmt.execute("ALTER TABLE transactions RENAME TO transactions_old");
            
            // Создаём новые таблицы с user_id
            String createCategoriesTable = """
                CREATE TABLE categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    type TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createCategoriesTable);
            
            String createTransactionsTable = """
                CREATE TABLE transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    category_id INTEGER,
                    type TEXT NOT NULL,
                    user_id INTEGER NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createTransactionsTable);
            
            // Копируем данные из старых таблиц, добавляя user_id = 1
            stmt.execute("INSERT INTO categories (id, name, color, type, user_id) SELECT id, name, color, type, 1 FROM categories_old");
            stmt.execute("INSERT INTO transactions (id, description, amount, date, category_id, type, user_id) SELECT id, description, amount, date, category_id, type, 1 FROM transactions_old");
            
            // Удаляем старые таблицы
            stmt.execute("DROP TABLE categories_old");
            stmt.execute("DROP TABLE transactions_old");
        }
    }
    
    /**
     * Получить соединение с БД из пула
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Закрыть connection pool при завершении приложения
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

