package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class EditTransactionController {

    @FXML
    private ToggleGroup typeToggle;

    @FXML
    private ToggleButton incomeButton;

    @FXML
    private ToggleButton expenseButton;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField amountField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TextArea descriptionArea;

    private DataService dataService;
    private Stage dialogStage;
    private boolean saved = false;
    private Transaction transaction;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();

        // Слушатели для переключения типа транзакции
        typeToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == incomeButton) {
                filterCategories(TransactionType.INCOME);
            } else if (newToggle == expenseButton) {
                filterCategories(TransactionType.EXPENSE);
            }
        });

        // Валидация поля суммы
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([.,]\\d{0,2})?")) {
                amountField.setText(oldValue);
            }
        });
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        
        // Заполняем поля данными транзакции
        if (transaction.getType() == TransactionType.INCOME) {
            incomeButton.setSelected(true);
            filterCategories(TransactionType.INCOME);
        } else {
            expenseButton.setSelected(true);
            filterCategories(TransactionType.EXPENSE);
        }
        
        // Устанавливаем категорию
        if (transaction.getCategory() != null) {
            categoryComboBox.setValue(transaction.getCategory());
        }
        
        // Сумма (без знака)
        amountField.setText(String.format("%.2f", Math.abs(transaction.getAmount())));
        
        // Дата
        datePicker.setValue(transaction.getDate().toLocalDate());
        
        // Описание
        descriptionArea.setText(transaction.getDescription());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaved() {
        return saved;
    }

    private void filterCategories(TransactionType type) {
        ObservableList<Category> filteredCategories = FXCollections.observableArrayList();
        for (Category category : dataService.getCategories()) {
            if (category.getType() == type) {
                filteredCategories.add(category);
            }
        }
        categoryComboBox.setItems(filteredCategories);
        if (!filteredCategories.isEmpty() && categoryComboBox.getValue() == null) {
            categoryComboBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onSave() {
        // Валидация
        if (amountField.getText().isEmpty() || categoryComboBox.getSelectionModel().isEmpty() || datePicker.getValue() == null) {
            showAlert("Ошибка валидации", "Пожалуйста, заполните все обязательные поля (Сумма, Категория, Дата).");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText().replace(',', '.'));
            if (amount <= 0) {
                showAlert("Ошибка валидации", "Сумма должна быть положительным числом.");
                return;
            }

            TransactionType type = incomeButton.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
            if (type == TransactionType.EXPENSE) {
                amount = -Math.abs(amount);
            }

            Category selectedCategory = categoryComboBox.getSelectionModel().getSelectedItem();
            LocalDate selectedDate = datePicker.getValue();
            LocalDateTime transactionDateTime = selectedDate.atTime(transaction.getDate().toLocalTime()); // Сохраняем время

            // Обновляем данные транзакции
            transaction.setAmount(amount);
            transaction.setCategory(selectedCategory);
            transaction.setCategoryId(selectedCategory.getId());
            transaction.setDate(transactionDateTime);
            transaction.setDescription(descriptionArea.getText());
            transaction.setType(type);

            dataService.updateTransaction(transaction);
            saved = true;
            dialogStage.close();

        } catch (NumberFormatException e) {
            showAlert("Ошибка ввода", "Пожалуйста, введите корректную сумму.");
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

