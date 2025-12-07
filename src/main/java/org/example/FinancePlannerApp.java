package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.util.WindowsThemeUtil;

import java.io.IOException;

public class FinancePlannerApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FinancePlannerApp.class.getResource("/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        scene.setFill(Color.web("#0A1628")); // Цвет фона
        
        stage.setTitle("Планировщик финансов");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        
        // Применяем темную тему для title bar (только Windows)
        Platform.runLater(() -> WindowsThemeUtil.setDarkTitleBar(stage));
        
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


