package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.model.Category;
import org.example.service.DataService;

public class EditCategoryController {

    @FXML
    private TextField nameField;

    @FXML
    private ColorPicker colorPicker;

    private DataService dataService;
    private Stage dialogStage;
    private boolean saved = false;
    private Category category;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();
    }

    public void setCategory(Category category) {
        this.category = category;
        
        // Заполняем поля данными категории
        nameField.setText(category.getName());
        
        // Конвертируем цвет из HEX в Color
        try {
            Color color = Color.web(category.getColor());
            colorPicker.setValue(color);
        } catch (Exception e) {
            colorPicker.setValue(Color.GRAY);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void onSave() {
        // Валидация
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Ошибка валидации", "Пожалуйста, введите название категории.");
            return;
        }

        try {
            // Обновляем данные категории
            category.setName(nameField.getText().trim());
            
            // Конвертируем Color в HEX
            Color color = colorPicker.getValue();
            String hexColor = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));
            category.setColor(hexColor);

            dataService.updateCategory(category);
            saved = true;
            dialogStage.close();

        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить категорию: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

