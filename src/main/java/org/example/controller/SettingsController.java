package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.service.AuthService;
import org.example.service.SessionManager;

import java.io.IOException;

public class SettingsController {

    @FXML
    private Label usernameLabel;

    private AuthService authService;
    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        sessionManager = SessionManager.getInstance();
        
        // Загружаем имя пользователя
        loadUserInfo();
    }

    private void loadUserInfo() {
        Long userId = sessionManager.getCurrentUserId();
        if (userId != null) {
            // Получаем имя пользователя через репозиторий
            org.example.repository.UserRepository userRepository = new org.example.repository.impl.UserRepositoryImpl();
            userRepository.findById(userId).ifPresent(user -> {
                usernameLabel.setText(user.getUsername());
            });
        }
    }

    @FXML
    private void onLogout() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Выход из аккаунта");
        confirmDialog.setHeaderText("Вы действительно хотите выйти?");
        confirmDialog.setContentText("Все несохраненные данные будут потеряны.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Выходим из аккаунта
                authService.logout();
                
                // Переходим на экран входа
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Scene scene = new Scene(loader.load(), 1200, 700);
                    scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                    
                    Stage stage = (Stage) usernameLabel.getScene().getWindow();
                    stage.setScene(scene);
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Ошибка", "Не удалось перейти на экран входа: " + e.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

