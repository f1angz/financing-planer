package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Конфигурация базы данных SQLite
 */
public class DatabaseConfig {
    
    private static final Properties properties = new Properties();
    
    static {
        // Загружаем конфигурацию из файла (если есть)
        try (InputStream input = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            // Если файла нет, используем значения по умолчанию
        }
    }
    
    public static String getUrl() {
        return properties.getProperty("database.url", "jdbc:sqlite:finance_planner.db");
    }
    
    public static String getDriver() {
        return "org.sqlite.JDBC";
    }
}

