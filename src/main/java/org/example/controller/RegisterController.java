package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.service.SessionManager;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private AuthService authService;
    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        sessionManager = SessionManager.getInstance();
        
        // Обработка Enter для регистрации
        confirmPasswordField.setOnAction(e -> onRegister());
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Валидация
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Заполните все обязательные поля");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Пароли не совпадают");
            return;
        }

        try {
            // Регистрация пользователя
            User user = authService.register(username, password, email.isEmpty() ? null : email);
            
            // Автоматически входим
            sessionManager.setCurrentUser(user);
            
            // Открываем главное окно
            openMainWindow();
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Авторизация");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMainWindow() {
        try {
            // Загружаем данные пользователя
            org.example.service.DataService.getInstance().loadData();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Планировщик финансов");
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}

