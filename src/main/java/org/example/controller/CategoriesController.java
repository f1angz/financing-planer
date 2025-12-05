package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.model.Category;
import org.example.model.TransactionType;
import org.example.service.DataService;

public class CategoriesController {

    @FXML
    private VBox incomeCategoriesContainer;

    @FXML
    private VBox expenseCategoriesContainer;

    @FXML
    private TextField categoryNameField;

    @FXML
    private ColorPicker categoryColorPicker;

    @FXML
    private ComboBox<String> categoryTypeCombo;

    private DataService dataService;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();
        
        // Настройка комбобокса
        categoryTypeCombo.getItems().addAll("Доход", "Расход");
        categoryTypeCombo.setValue("Доход");
        
        // Загрузка категорий
        loadCategories();
    }

    private void loadCategories() {
        incomeCategoriesContainer.getChildren().clear();
        expenseCategoriesContainer.getChildren().clear();
        
        for (Category category : dataService.getCategories()) {
            HBox categoryRow = createCategoryRow(category);
            if (category.getType() == TransactionType.INCOME) {
                incomeCategoriesContainer.getChildren().add(categoryRow);
            } else {
                expenseCategoriesContainer.getChildren().add(categoryRow);
            }
        }
    }

    private HBox createCategoryRow(Category category) {
        HBox row = new HBox(15);
        row.getStyleClass().add("category-row");
        
        Label colorBox = new Label();
        colorBox.getStyleClass().add("category-color-box");
        colorBox.setStyle("-fx-background-color: " + category.getColor() + ";");
        colorBox.setPrefSize(30, 30);
        
        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("category-name");
        nameLabel.setPrefWidth(200);
        
        Button deleteButton = new Button("Удалить");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> {
            dataService.removeCategory(category);
            loadCategories();
        });
        
        row.getChildren().addAll(colorBox, nameLabel, deleteButton);
        return row;
    }

    @FXML
    private void onAddCategory() {
        String name = categoryNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите название категории");
            return;
        }
        
        String color = "#" + categoryColorPicker.getValue().toString().substring(2, 8).toUpperCase();
        TransactionType type = categoryTypeCombo.getValue().equals("Доход") 
            ? TransactionType.INCOME : TransactionType.EXPENSE;
        
        Category newCategory = new Category(name, color, type);
        dataService.addCategory(newCategory);
        
        categoryNameField.clear();
        loadCategories();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


