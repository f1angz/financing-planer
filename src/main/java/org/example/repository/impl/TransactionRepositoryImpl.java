package org.example.repository.impl;

import org.example.database.DatabaseManager;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.repository.TransactionRepositoryExt;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория транзакций для работы с БД через JDBC
 * Работает с SQLite и PostgreSQL
 */
public class TransactionRepositoryImpl implements TransactionRepositoryExt {
    
    private final DatabaseManager databaseManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public TransactionRepositoryImpl() {
        this.databaseManager = DatabaseManager.getInstance();
    }
    
    @Override
    public void save(Transaction transaction) {
        String sql = "INSERT INTO transactions (description, amount, date, category_id, type, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, transaction.getDescription());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDate().format(DATE_FORMATTER));
            
            if (transaction.getCategoryId() != null) {
                pstmt.setLong(4, transaction.getCategoryId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            pstmt.setString(5, transaction.getType().name());
            pstmt.setLong(6, transaction.getUserId());
            
            pstmt.executeUpdate();
            
            // Получаем сгенерированный ID
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                transaction.setId(rs.getLong(1));
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(Transaction transaction) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, date = ?, category_id = ?, type = ?, user_id = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transaction.getDescription());
            pstmt.setDouble(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDate().format(DATE_FORMATTER));
            
            if (transaction.getCategoryId() != null) {
                pstmt.setLong(4, transaction.getCategoryId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            
            pstmt.setString(5, transaction.getType().name());
            pstmt.setLong(6, transaction.getUserId());
            pstmt.setLong(7, transaction.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void delete(Transaction transaction) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, transaction.getId());
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToTransaction(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding transaction by id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Transaction> findAll() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM transactions";
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            System.err.println("Error deleting all transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public List<Transaction> findByUserId(Long userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error finding transactions by user id: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        String dateStr = rs.getString("date");
        LocalDateTime date = LocalDateTime.parse(dateStr, DATE_FORMATTER);
        
        Long categoryId = rs.getLong("category_id");
        if (rs.wasNull()) {
            categoryId = null;
        }
        
        return new Transaction(
            rs.getLong("id"),
            rs.getString("description"),
            rs.getDouble("amount"),
            date,
            categoryId,
            TransactionType.valueOf(rs.getString("type")),
            rs.getLong("user_id")
        );
    }
}

