package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.service.DataService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HomeController {

    @FXML
    private PieChart incomeChart;

    @FXML
    private PieChart expenseChart;

    @FXML
    private VBox transactionsContainer;

    @FXML
    private ToggleGroup periodToggle;

    @FXML
    private ToggleButton yearButton;

    @FXML
    private ToggleButton monthButton;

    @FXML
    private ToggleButton dayButton;

    @FXML
    private Label periodLabel;
    
    @FXML
    private Button resetButton;
    
    @FXML
    private Label balanceLabel;

    private DataService dataService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    // Выбранный период
    private Integer selectedYear = null;
    private Integer selectedMonth = null;
    private Integer selectedDay = null;

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();
        
        // Настройка диаграмм
        setupCharts();
        
        // По умолчанию показываем данные за текущий год
        selectedYear = java.time.LocalDate.now().getYear();
        yearButton.setSelected(true);
        updatePeriodLabel();
        
        // Загрузка данных
        loadChartData();
        loadTransactions();
    }

    private void setupCharts() {
        incomeChart.setLegendSide(Side.RIGHT);
        incomeChart.setLabelsVisible(false);
        incomeChart.setStartAngle(90);
        
        expenseChart.setLegendSide(Side.RIGHT);
        expenseChart.setLabelsVisible(false);
        expenseChart.setStartAngle(90);
    }

    private void loadChartData() {
        Map<String, Double> incomeByCategory = new HashMap<>();
        Map<String, Double> expenseByCategory = new HashMap<>();
        Map<String, String> categoryColors = new HashMap<>();
        
        // Фильтруем транзакции по периоду
        ObservableList<Transaction> filteredTransactions = getFilteredTransactions();
        
        double totalIncome = 0.0;
        double totalExpense = 0.0;
        
        for (Transaction transaction : filteredTransactions) {
            // Подсчитываем общий баланс
            if (transaction.getType() == TransactionType.INCOME) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpense += Math.abs(transaction.getAmount());
            }
            
            // Проверяем, что категория существует
            if (transaction.getCategory() == null) {
                continue;
            }
            
            String categoryName = transaction.getCategory().getName();
            String categoryColor = transaction.getCategory().getColor();
            
            // Сохраняем цвет категории
            categoryColors.put(categoryName, categoryColor);
            
            if (transaction.getType() == TransactionType.INCOME) {
                incomeByCategory.put(categoryName, 
                    incomeByCategory.getOrDefault(categoryName, 0.0) + transaction.getAmount());
            } else {
                expenseByCategory.put(categoryName, 
                    expenseByCategory.getOrDefault(categoryName, 0.0) + Math.abs(transaction.getAmount()));
            }
        }
        
        // Обновляем баланс
        updateBalance(totalIncome, totalExpense);
        
        // Заполнение диаграммы доходов
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        incomeByCategory.forEach((category, amount) -> 
            incomeData.add(new PieChart.Data(category, amount)));
        incomeChart.setData(incomeData);
        
        // Применяем цвета категорий к секторам диаграммы доходов
        applyChartColors(incomeChart, categoryColors);
        
        // Заполнение диаграммы расходов
        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        expenseByCategory.forEach((category, amount) -> 
            expenseData.add(new PieChart.Data(category, amount)));
        expenseChart.setData(expenseData);
        
        // Применяем цвета категорий к секторам диаграммы расходов
        applyChartColors(expenseChart, categoryColors);
    }
    
    /**
     * Обновляет отображение баланса
     */
    private void updateBalance(double income, double expense) {
        double balance = income - expense;
        
        // Форматируем сумму
        String balanceText = String.format("%.2f руб.", Math.abs(balance));
        if (balance >= 0) {
            balanceText = "+" + balanceText;
        } else {
            balanceText = "-" + balanceText;
        }
        
        balanceLabel.setText(balanceText);
        
        // Устанавливаем цвет в зависимости от знака
        balanceLabel.getStyleClass().removeAll("balance-positive", "balance-negative");
        if (balance >= 0) {
            balanceLabel.getStyleClass().add("balance-positive");
        } else {
            balanceLabel.getStyleClass().add("balance-negative");
        }
    }
    
    /**
     * Применяет цвета категорий к секторам диаграммы и легенде
     */
    private void applyChartColors(PieChart chart, Map<String, String> categoryColors) {
        // Ждём когда диаграмма отрисуется
        chart.applyCss();
        chart.layout();
        
        for (PieChart.Data data : chart.getData()) {
            String categoryName = data.getName();
            String color = categoryColors.get(categoryName);
            
            if (color != null && data.getNode() != null) {
                // Применяем цвет к сектору диаграммы
                data.getNode().setStyle("-fx-pie-color: " + color + ";");
            }
        }
        
        // Применяем цвета к легенде
        javafx.application.Platform.runLater(() -> {
            applyLegendColors(chart, categoryColors);
        });
    }
    
    /**
     * Применяет цвета категорий к элементам легенды
     */
    private void applyLegendColors(PieChart chart, Map<String, String> categoryColors) {
        // Находим все узлы легенды
        for (javafx.scene.Node node : chart.lookupAll(".chart-legend-item")) {
            if (node instanceof javafx.scene.control.Label) {
                javafx.scene.control.Label label = (javafx.scene.control.Label) node;
                String categoryName = label.getText();
                String color = categoryColors.get(categoryName);
                
                if (color != null) {
                    // Находим символ легенды (цветной квадратик)
                    for (javafx.scene.Node child : label.getChildrenUnmodifiable()) {
                        if (child.getStyleClass().contains("chart-legend-item-symbol")) {
                            child.setStyle("-fx-background-color: " + color + ";");
                        }
                    }
                }
            }
        }
    }

    private void loadTransactions() {
        transactionsContainer.getChildren().clear();
        
        // Получаем отфильтрованные транзакции
        ObservableList<Transaction> filteredTransactions = getFilteredTransactions();
        
        // Получаем последние 5 транзакций
        int count = Math.min(5, filteredTransactions.size());
        
        for (int i = filteredTransactions.size() - 1; i >= filteredTransactions.size() - count && i >= 0; i--) {
            Transaction transaction = filteredTransactions.get(i);
            transactionsContainer.getChildren().add(createTransactionRow(transaction));
        }
    }
    
    /**
     * Фильтрует транзакции по выбранному периоду
     */
    private ObservableList<Transaction> getFilteredTransactions() {
        ObservableList<Transaction> allTransactions = dataService.getTransactions();
        ObservableList<Transaction> filtered = FXCollections.observableArrayList();
        
        // Если период не выбран, показываем данные за текущий год
        int filterYear = selectedYear != null ? selectedYear : java.time.LocalDate.now().getYear();
        Integer filterMonth = selectedMonth;
        Integer filterDay = selectedDay;
        
        for (Transaction transaction : allTransactions) {
            java.time.LocalDateTime transactionDate = transaction.getDate();
            
            // Фильтрация по году (обязательно)
            boolean matches = transactionDate.getYear() == filterYear;
            
            // Фильтрация по месяцу (если выбран)
            if (filterMonth != null) {
                matches = matches && (transactionDate.getMonthValue() == filterMonth);
            }
            
            // Фильтрация по дню (если выбран)
            if (filterDay != null) {
                matches = matches && (transactionDate.getDayOfMonth() == filterDay);
            }
            
            if (matches) {
                filtered.add(transaction);
            }
        }
        
        return filtered;
    }

    private HBox createTransactionRow(Transaction transaction) {
        HBox row = new HBox(20);
        row.getStyleClass().add("transaction-row");
        row.setAlignment(Pos.CENTER_LEFT);
        
        // Категория с цветным квадратиком
        HBox categoryBox = new HBox(10);
        categoryBox.setPrefWidth(200);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        
        // Цветной квадратик
        Region colorBox = new Region();
        colorBox.getStyleClass().add("category-indicator");
        colorBox.setPrefSize(20, 20);
        colorBox.setMinSize(20, 20);
        colorBox.setMaxSize(20, 20);
        
        String categoryName = "Без категории";
        String categoryColor = "#8B9FC5";
        
        if (transaction.getCategory() != null) {
            categoryName = transaction.getCategory().getName();
            categoryColor = transaction.getCategory().getColor();
        }
        
        colorBox.setStyle("-fx-background-color: " + categoryColor + "; -fx-background-radius: 4;");
        
        Label categoryLabel = new Label(categoryName);
        categoryLabel.getStyleClass().add("transaction-category");
        
        categoryBox.getChildren().addAll(colorBox, categoryLabel);
        
        // Дата
        Label dateLabel = new Label(transaction.getDate().format(dateFormatter));
        dateLabel.getStyleClass().add("transaction-date");
        dateLabel.setPrefWidth(150);
        dateLabel.setAlignment(Pos.CENTER_LEFT);
        
        // Сумма
        Label amountLabel = new Label(String.format("%.2f руб.", Math.abs(transaction.getAmount())));
        amountLabel.getStyleClass().add("transaction-amount");
        if (transaction.getType() == TransactionType.INCOME) {
            amountLabel.getStyleClass().add("income");
        } else {
            amountLabel.getStyleClass().add("expense");
        }
        amountLabel.setPrefWidth(130);
        amountLabel.setAlignment(Pos.CENTER_LEFT);
        
        // Описание
        Label descLabel = new Label(transaction.getDescription());
        descLabel.getStyleClass().add("transaction-desc");
        descLabel.setPrefWidth(300);
        descLabel.setAlignment(Pos.CENTER_LEFT);
        
        // Кнопки действий
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPrefWidth(150);
        
        Button editButton = new Button("✏");
        editButton.getStyleClass().add("action-button");
        editButton.getStyleClass().add("edit-action");
        editButton.setOnAction(e -> onEditTransaction(transaction));
        
        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("action-button");
        deleteButton.getStyleClass().add("delete-action");
        deleteButton.setOnAction(e -> onDeleteTransaction(transaction));
        
        actionsBox.getChildren().addAll(editButton, deleteButton);
        
        row.getChildren().addAll(categoryBox, dateLabel, amountLabel, descLabel, actionsBox);
        return row;
    }

    @FXML
    private void onYearClicked() {
        showYearPicker();
    }
    
    @FXML
    private void onMonthClicked() {
        showMonthPicker();
    }
    
    @FXML
    private void onDayClicked() {
        showDayPicker();
    }
    
    @FXML
    private void onResetPeriod() {
        // Сбрасываем на текущий год
        selectedYear = java.time.LocalDate.now().getYear();
        selectedMonth = null;
        selectedDay = null;
        
        // Выбираем кнопку "Год"
        yearButton.setSelected(true);
        
        updatePeriodLabel();
        loadChartData();
        loadTransactions();
    }
    
    /**
     * Диалог выбора года
     */
    private void showYearPicker() {
        java.util.List<Integer> years = new java.util.ArrayList<>();
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = currentYear - 10; i <= currentYear + 5; i++) {
            years.add(i);
        }
        
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(selectedYear != null ? selectedYear : currentYear, years);
        dialog.setTitle("Выбор года");
        dialog.setHeaderText("Выберите год");
        dialog.setContentText("Год:");
        
        dialog.showAndWait().ifPresent(year -> {
            selectedYear = year;
            selectedMonth = null;
            selectedDay = null;
            updatePeriodLabel();
            loadChartData();
            loadTransactions();
        });
    }
    
    /**
     * Диалог выбора месяца
     */
    private void showMonthPicker() {
        // Если год не выбран, выбираем текущий
        if (selectedYear == null) {
            selectedYear = java.time.LocalDate.now().getYear();
        }
        
        java.util.Map<String, Integer> months = new java.util.LinkedHashMap<>();
        months.put("Январь", 1);
        months.put("Февраль", 2);
        months.put("Март", 3);
        months.put("Апрель", 4);
        months.put("Май", 5);
        months.put("Июнь", 6);
        months.put("Июль", 7);
        months.put("Август", 8);
        months.put("Сентябрь", 9);
        months.put("Октябрь", 10);
        months.put("Ноябрь", 11);
        months.put("Декабрь", 12);
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            selectedMonth != null ? getMonthName(selectedMonth) : "Январь", 
            months.keySet()
        );
        dialog.setTitle("Выбор месяца");
        dialog.setHeaderText("Выберите месяц для " + selectedYear + " года");
        dialog.setContentText("Месяц:");
        
        dialog.showAndWait().ifPresent(monthName -> {
            selectedMonth = months.get(monthName);
            selectedDay = null;
            updatePeriodLabel();
            loadChartData();
            loadTransactions();
        });
    }
    
    /**
     * Диалог выбора дня
     */
    private void showDayPicker() {
        // Если год и месяц не выбраны, используем текущие
        if (selectedYear == null) {
            selectedYear = java.time.LocalDate.now().getYear();
        }
        if (selectedMonth == null) {
            selectedMonth = java.time.LocalDate.now().getMonthValue();
        }
        
        // Определяем количество дней в месяце
        int daysInMonth = java.time.YearMonth.of(selectedYear, selectedMonth).lengthOfMonth();
        
        java.util.List<Integer> days = new java.util.ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(
            selectedDay != null ? selectedDay : 1, 
            days
        );
        dialog.setTitle("Выбор дня");
        dialog.setHeaderText("Выберите день для " + getMonthName(selectedMonth) + " " + selectedYear);
        dialog.setContentText("День:");
        
        dialog.showAndWait().ifPresent(day -> {
            selectedDay = day;
            updatePeriodLabel();
            loadChartData();
            loadTransactions();
        });
    }
    
    /**
     * Обновляет метку с выбранным периодом
     */
    private void updatePeriodLabel() {
        StringBuilder label = new StringBuilder();
        
        if (selectedDay != null && selectedMonth != null && selectedYear != null) {
            label.append(String.format("%02d.%02d.%d", selectedDay, selectedMonth, selectedYear));
        } else if (selectedMonth != null && selectedYear != null) {
            label.append(getMonthName(selectedMonth)).append(" ").append(selectedYear);
        } else if (selectedYear != null) {
            label.append(selectedYear);
        }
        
        periodLabel.setText(label.toString());
        
        // Показываем кнопку "Сбросить" если:
        // - выбран год != текущий год ИЛИ
        // - выбран месяц ИЛИ день
        int currentYear = java.time.LocalDate.now().getYear();
        boolean yearNotCurrent = selectedYear != null && selectedYear != currentYear;
        boolean showResetButton = yearNotCurrent || selectedMonth != null || selectedDay != null;
        resetButton.setVisible(showResetButton);
        resetButton.setManaged(showResetButton);
    }
    
    /**
     * Возвращает название месяца по номеру
     */
    private String getMonthName(int month) {
        String[] months = {"", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", 
                          "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        return months[month];
    }

    /**
     * Удаление транзакции
     */
    private void onDeleteTransaction(Transaction transaction) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Подтверждение удаления");
        confirmDialog.setHeaderText("Удалить транзакцию?");
        confirmDialog.setContentText(String.format("Вы действительно хотите удалить транзакцию:\n%s - %.2f руб.",
                transaction.getDescription(), Math.abs(transaction.getAmount())));
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataService.removeTransaction(transaction);
                loadChartData();
                loadTransactions();
            }
        });
    }
    
    /**
     * Редактирование транзакции
     */
    private void onEditTransaction(Transaction transaction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_transaction_dialog.fxml"));
            VBox dialogContent = loader.load();

            EditTransactionController controller = loader.getController();
            controller.setTransaction(transaction); // Передаём транзакцию для редактирования
            
            // Создаём затемнённый фон на весь экран
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");
            overlay.setAlignment(Pos.CENTER);
            
            // Добавляем диалог в центр
            overlay.getChildren().add(dialogContent);
            
            // Создаём модальное окно без декораций
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(transactionsContainer.getScene().getWindow());
            dialogStage.setResizable(false);
            
            // Создаем сцену с прозрачным фоном
            Scene scene = new Scene(overlay);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            // Убираем стандартные декорации окна
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            // Устанавливаем размер ОКНА по размеру главного окна (для затемнения)
            Stage mainStage = (Stage) transactionsContainer.getScene().getWindow();
            dialogStage.setWidth(mainStage.getWidth());
            dialogStage.setHeight(mainStage.getHeight());
            dialogStage.setX(mainStage.getX());
            dialogStage.setY(mainStage.getY());
            
            dialogStage.showAndWait();

            if (controller.isSaved()) {
                loadChartData();
                loadTransactions();
            }

        } catch (Exception e) {
            System.err.println("Error loading edit transaction dialog: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть окно редактирования");
            alert.setContentText("Ошибка: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    @FXML
    private void onAddTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_transaction_dialog.fxml"));
            VBox dialogContent = loader.load();
            
            AddTransactionController controller = loader.getController();
            
            // Создаём затемнённый фон на весь экран
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.65);");
            overlay.setAlignment(Pos.CENTER);
            
            // Добавляем диалог в центр
            overlay.getChildren().add(dialogContent);
            
            // Создаём модальное окно без декораций
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(transactionsContainer.getScene().getWindow());
            dialogStage.setResizable(false);
            
            // Создаем сцену с прозрачным фоном
            Scene scene = new Scene(overlay);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            // Убираем стандартные декорации окна
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.setScene(scene);
            
            // Устанавливаем размер ОКНА по размеру главного окна (для затемнения)
            Stage mainStage = (Stage) transactionsContainer.getScene().getWindow();
            dialogStage.setWidth(mainStage.getWidth());
            dialogStage.setHeight(mainStage.getHeight());
            dialogStage.setX(mainStage.getX());
            dialogStage.setY(mainStage.getY());
            
            controller.setDialogStage(dialogStage);
            
            // Закрытие по клику на затемнённый фон (но не на сам диалог)
            overlay.setOnMouseClicked(e -> {
                // Проверяем, что клик был именно на overlay, а не на диалоге
                if (e.getTarget() == overlay) {
                    dialogStage.close();
                }
            });
            
            // Предотвращаем закрытие при клике на сам диалог
            dialogContent.setOnMouseClicked(e -> e.consume());
            
            // Показываем диалог и ждём закрытия
            dialogStage.showAndWait();
            
            // Если транзакция была сохранена, обновляем данные
            if (controller.isSaved()) {
                loadChartData();
                loadTransactions();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Не удалось открыть форму добавления транзакции: " + e.getMessage());
            alert.showAndWait();
        }
    }
}


