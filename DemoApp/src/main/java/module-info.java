module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.java.html.boot.fx;
    requires javafx.web;
    requires java.sql;
    requires RidesharingServer;
    requires java.curl;
    requires org.jsoup;
    requires org.json;
    requires org.locationtech.jts;
    requires com.jfoenix;

    opens org.app to javafx.fxml;
    exports org.app;
    exports org.app.controller;
    opens org.app.controller to javafx.fxml;
}