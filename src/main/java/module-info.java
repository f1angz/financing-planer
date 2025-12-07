module org.example.financeplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires com.sun.jna;
    requires com.sun.jna.platform;
    requires bcrypt;

    opens org.example to javafx.fxml;
    opens org.example.controller to javafx.fxml;
    opens org.example.model to javafx.base;
    
    exports org.example;
    exports org.example.controller;
    exports org.example.model;
    exports org.example.service;
    exports org.example.repository;
    exports org.example.config;
    exports org.example.util;
}


