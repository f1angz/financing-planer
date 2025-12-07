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

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private AuthService authService;
    private SessionManager sessionManager;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        sessionManager = SessionManager.getInstance();
        
        // Обработка Enter для входа
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        try {
            // Попытка авторизации
            User user = authService.login(username, password);
            
            // Сохраняем пользователя в сессии
            sessionManager.setCurrentUser(user);
            
            // Открываем главное окно
            openMainWindow();
            
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onRegister() {
        try {
            // Открываем окно регистрации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Scene scene = new Scene(loader.load(), 500, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Регистрация");
            
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть форму регистрации");
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
            showError("Не удалось открыть главное окно");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}

