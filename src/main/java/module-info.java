module org.example.financeplanner {
    requires javafx.controls;
    requires javafx.fxml;
    
    opens org.example to javafx.fxml;
    opens org.example.controller to javafx.fxml;
    opens org.example.model to javafx.base;
    
    exports org.example;
    exports org.example.controller;
    exports org.example.model;
}


