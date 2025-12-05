package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Category;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.service.DataService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddTransactionController {

    @FXML
    private ToggleGroup typeToggle;

    @FXML
    private ToggleButton incomeButton;

    @FXML
    private ToggleButton expenseButton;

    @FXML
    private ComboBox<Category> categoryCombo;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea descriptionArea;

    private DataService dataService;
    private Stage dialogStage;
    private boolean saved = false;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();
        
        // По умолчанию выбран расход
        expenseButton.setSelected(true);
        
        // Устанавливаем текущую дату
        datePicker.setValue(LocalDate.now());
        
        // Загружаем категории
        loadCategories();
        
        // Обновляем категории при изменении типа
        typeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadCategories();
            }
        });
        
        // Форматирование суммы (только цифры и точка)
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                amountField.setText(oldVal);
            }
        });
    }

    private void loadCategories() {
        TransactionType selectedType = getSelectedType();
        categoryCombo.getItems().clear();
        
        for (Category category : dataService.getCategories()) {
            if (category.getType() == selectedType) {
                categoryCombo.getItems().add(category);
            }
        }
        
        // Устанавливаем отображение имени категории
        categoryCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Выбираем первую категорию по умолчанию
        if (!categoryCombo.getItems().isEmpty()) {
            categoryCombo.getSelectionModel().select(0);
        }
    }

    private TransactionType getSelectedType() {
        return incomeButton.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    @FXML
    private void onSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Получаем данные
            String description = descriptionArea.getText().trim();
            if (description.isEmpty()) {
                description = "Транзакция";
            }
            
            double amount = Double.parseDouble(amountField.getText());
            TransactionType type = getSelectedType();
            
            // Для расходов делаем сумму отрицательной
            if (type == TransactionType.EXPENSE && amount > 0) {
                amount = -amount;
            }
            
            Category category = categoryCombo.getValue();
            LocalDate date = datePicker.getValue();
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.now());
            
            // Создаём транзакцию
            Transaction transaction = new Transaction(description, amount, dateTime, category, type);
            
            // Сохраняем
            dataService.addTransaction(transaction);
            
            saved = true;
            dialogStage.close();
            
        } catch (Exception e) {
            showError("Ошибка", "Не удалось сохранить транзакцию: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (categoryCombo.getValue() == null) {
            errors.append("• Выберите категорию\n");
        }
        
        if (amountField.getText().trim().isEmpty()) {
            errors.append("• Введите сумму\n");
        } else {
            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    errors.append("• Сумма должна быть больше 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Неверный формат суммы\n");
            }
        }
        
        if (datePicker.getValue() == null) {
            errors.append("• Выберите дату\n");
        }
        
        if (errors.length() > 0) {
            showError("Ошибка валидации", errors.toString());
            return false;
        }
        
        return true;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }
}

