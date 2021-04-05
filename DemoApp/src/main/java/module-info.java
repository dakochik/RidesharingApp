module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.java.html.boot.fx;
    requires javafx.web;
    requires java.sql;

    opens org.example to javafx.fxml;
    exports org.example;
}