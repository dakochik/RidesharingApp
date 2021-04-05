package org.example;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PrimaryController implements Initializable {

    public WebView webView;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        WebEngine engine = webView.getEngine();
        URL url = this.getClass().getResource("/pages/index.html");
        engine.load(url.toString());
        //engine.load("https://leafletjs.com/examples/quick-start/");
        //engine.load("https://www.openstreetmap.org/#map=10/41.9453/-87.4781");
        //engine.load("https://leafletjs.com/examples.html");
    }

}
