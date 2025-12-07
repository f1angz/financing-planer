package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.service.SessionManager;
import org.example.util.WindowsThemeUtil;

import java.io.IOException;

public class FinancePlannerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SessionManager sessionManager = SessionManager.getInstance();
        
        // Проверяем, авторизован ли пользователь
        String fxmlPath;
        int width, height;
        
        if (sessionManager.isLoggedIn()) {
            // Если пользователь уже авторизован, открываем главное окно
            fxmlPath = "/fxml/main.fxml";
            width = 1200;
            height = 700;
        } else {
            // Иначе показываем экран логина
            fxmlPath = "/fxml/login.fxml";
            width = 500;
            height = 600;
        }
        
        FXMLLoader fxmlLoader = new FXMLLoader(FinancePlannerApp.class.getResource(fxmlPath));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        scene.setFill(Color.web("#0A1628")); // Цвет фона
        
        stage.setTitle(sessionManager.isLoggedIn() ? "Планировщик финансов" : "Авторизация");
        stage.setScene(scene);
        
        if (sessionManager.isLoggedIn()) {
            stage.setMinWidth(1000);
            stage.setMinHeight(600);
        } else {
            stage.setResizable(false);
        }
        
        // Применяем темную тему для title bar (только Windows)
        Platform.runLater(() -> WindowsThemeUtil.setDarkTitleBar(stage));
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


