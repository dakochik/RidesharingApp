module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.java.html.boot.fx;
    requires javafx.web;
    requires java.sql;
    requires RidesharingServer;
    requires java.curl;
    requires org.jsoup;
    requires json.simple;
    requires org.locationtech.jts;

    opens org.example to javafx.fxml;
    exports org.example;
}