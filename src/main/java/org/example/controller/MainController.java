package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainContainer;

    @FXML
    private VBox sideMenu;

    @FXML
    private Button homeButton;

    @FXML
    private Button categoriesButton;

    @FXML
    private Button statisticsButton;

    @FXML
    private Button settingsButton;

    private Button currentActiveButton;

    @FXML
    public void initialize() {
        // По умолчанию показываем главную страницу
        loadHomeView();
        setActiveButton(homeButton);
    }

    @FXML
    private void loadHomeView() {
        loadView("/fxml/home.fxml");
        setActiveButton(homeButton);
    }

    @FXML
    private void loadCategoriesView() {
        loadView("/fxml/categories.fxml");
        setActiveButton(categoriesButton);
    }

    @FXML
    private void loadStatisticsView() {
        loadView("/fxml/statistics.fxml");
        setActiveButton(statisticsButton);
    }

    @FXML
    private void loadSettingsView() {
        loadView("/fxml/settings.fxml");
        setActiveButton(settingsButton);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainContainer.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        currentActiveButton = button;
    }
}


