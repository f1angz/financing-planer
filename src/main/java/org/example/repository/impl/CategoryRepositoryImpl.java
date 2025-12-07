package org.example.repository.impl;

import org.example.database.DatabaseManager;
import org.example.model.Category;
import org.example.model.TransactionType;
import org.example.repository.CategoryRepositoryExt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория категорий для работы с БД через JDBC
 * Работает с SQLite и PostgreSQL
 */
public class CategoryRepositoryImpl implements CategoryRepositoryExt {
    
    private final DatabaseManager databaseManager;
    
    public CategoryRepositoryImpl() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    @Override
    public void save(Category category) {
        String sql = "INSERT INTO categories (name, color, type, user_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getColor());
            pstmt.setString(3, category.getType().name());
            pstmt.setLong(4, category.getUserId());
            
            pstmt.executeUpdate();
            
            // Получаем последний вставленный ID (для SQLite)
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    category.setId(rs.getLong(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving category: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, color = ?, type = ?, user_id = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getColor());
            pstmt.setString(3, category.getType().name());
            pstmt.setLong(4, category.getUserId());
            pstmt.setLong(5, category.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void delete(Category category) {
        String sql = "DELETE FROM categories WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, category.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding category by id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY id";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all categories: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM categories";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            System.err.println("Error deleting all categories: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Category> findByUserId(Long userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? ORDER BY id";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                categories.add(mapResultSetToCategory(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding categories by user id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categories;
    }
    
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("color"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getLong("user_id")
        );
    }
}


