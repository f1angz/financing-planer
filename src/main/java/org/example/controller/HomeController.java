package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private TextField yearField;

    private DataService dataService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        dataService = DataService.getInstance();
        
        // Настройка диаграмм
        setupCharts();
        
        // Загрузка данных
        loadChartData();
        loadTransactions();
        
        // По умолчанию выбран год
        yearButton.setSelected(true);
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
        
        for (Transaction transaction : dataService.getTransactions()) {
            // Проверяем, что категория существует
            if (transaction.getCategory() == null) {
                continue;
            }
            
            if (transaction.getType() == TransactionType.INCOME) {
                String category = transaction.getCategory().getName();
                incomeByCategory.put(category, 
                    incomeByCategory.getOrDefault(category, 0.0) + transaction.getAmount());
            } else {
                String category = transaction.getCategory().getName();
                expenseByCategory.put(category, 
                    expenseByCategory.getOrDefault(category, 0.0) + Math.abs(transaction.getAmount()));
            }
        }
        
        // Заполнение диаграммы доходов
        ObservableList<PieChart.Data> incomeData = FXCollections.observableArrayList();
        incomeByCategory.forEach((category, amount) -> 
            incomeData.add(new PieChart.Data(category, amount)));
        incomeChart.setData(incomeData);
        
        // Заполнение диаграммы расходов
        ObservableList<PieChart.Data> expenseData = FXCollections.observableArrayList();
        expenseByCategory.forEach((category, amount) -> 
            expenseData.add(new PieChart.Data(category, amount)));
        expenseChart.setData(expenseData);
    }

    private void loadTransactions() {
        transactionsContainer.getChildren().clear();
        
        // Получаем последние 5 транзакций
        ObservableList<Transaction> allTransactions = dataService.getTransactions();
        int count = Math.min(5, allTransactions.size());
        
        for (int i = allTransactions.size() - 1; i >= allTransactions.size() - count && i >= 0; i--) {
            Transaction transaction = allTransactions.get(i);
            transactionsContainer.getChildren().add(createTransactionRow(transaction));
        }
    }

    private HBox createTransactionRow(Transaction transaction) {
        HBox row = new HBox(20);
        row.getStyleClass().add("transaction-row");
        
        Label descLabel = new Label(transaction.getDescription());
        descLabel.getStyleClass().add("transaction-desc");
        descLabel.setPrefWidth(300);
        
        Label dateLabel = new Label(transaction.getDate().format(dateFormatter));
        dateLabel.getStyleClass().add("transaction-date");
        dateLabel.setPrefWidth(150);
        
        Label amountLabel = new Label(String.format("%.2f руб.", Math.abs(transaction.getAmount())));
        amountLabel.getStyleClass().add("transaction-amount");
        if (transaction.getType() == TransactionType.INCOME) {
            amountLabel.getStyleClass().add("income");
        } else {
            amountLabel.getStyleClass().add("expense");
        }
        amountLabel.setPrefWidth(120);
        
        row.getChildren().addAll(descLabel, dateLabel, amountLabel);
        return row;
    }

    @FXML
    private void onPeriodChanged() {
        // Здесь можно добавить фильтрацию по периоду
        loadChartData();
        loadTransactions();
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


