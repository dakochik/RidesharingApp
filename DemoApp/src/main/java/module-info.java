module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires leaflet4j;
    requires net.java.html.boot;
    requires net.java.html.boot.fx;
    requires javafx.web;

    opens org.example to javafx.fxml;
    exports org.example;
}