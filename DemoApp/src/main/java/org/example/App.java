package org.example;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import net.java.html.boot.fx.FXBrowsers;
import net.java.html.leaflet.*;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        final MapView map = new MapView();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(map);

        // a regular JavaFX ListView
        ListView<Address> listView = new ListView<>();
        listView.getItems().addAll(new Address("Toni", 48.1322840, 11.5361690),
                new Address("Jarda", 50.0284060, 14.4934400),
                new Address("JUG Münster", 51.94906770000001, 7.613701100000071));
        // we listen for the selected item and update the map accordingly
        // as a demo of how to interact between JavaFX and DukeScript
        listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Address>() {
            @Override
            public void changed(ObservableValue<? extends Address> ov, Address old_val, final Address new_val) {
                FXBrowsers.runInBrowser(map.getWebView(), new Runnable() {
                    @Override
                    public void run() {
                        LatLng pos = new LatLng(new_val.getLat(), new_val.getLng());
                        map.getMap().setView(pos, 20);
                        map.getMap().openPopup("Here is " + new_val, pos);
                    }
                });
            }
        });

        borderPane.setLeft(listView);
        Scene scene = new Scene(borderPane);

        stage.setTitle("JavaFX and DukeScript");
        stage.setScene(scene);
        stage.show();

//        testF();
//
//        scene = new Scene(loadFXML("primary"));
//        stage.setScene(scene);
//        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    public class MapView extends StackPane {

        private final WebView webView;
        private Map map;

        public MapView() {
            // we define a regular JavaFX WebView that DukeScript can use for rendering
            webView = new WebView();
            getChildren().add(webView);

            // FXBrowsers loads the associated page into the WebView and runs our
            // code.
            FXBrowsers.load(webView, MapView.class.getResource("/pages/index.html"), new Runnable() {

                @Override
                public void run() {
                    // Here we define that the map is rendered to a div with id="map"
                    // in our index.html.
                    // This can only be done after the page is loaded and the context is initialized.
                    map = new Map("map");

                    // from here we just use the Leaflet API to show some stuff on the map
                    map.setView(new LatLng(51.505, -0.09), 18);
                    map.addLayer(new TileLayer("http://{s}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png",
                            new TileLayerOptions()
                                    .setAttribution(
                                            "Map data &copy; <a href='http://www.thunderforest.com/opencyclemap/'>OpenCycleMap</a> contributors, "
                                                    + "<a href='http://creativecommons.org/licenses/by-sa/2.0/'>CC-BY-SA</a>, "
                                                    + "Imagery © <a href='http://www.thunderforest.com/'>Thunderforest</a>")
                                    .setMaxZoom(18)
                                    .setId("eppleton.ia9c2p12")
                    ));

                    // Мои попытки:
                    //map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/dakochik@285a4b42@768d66e72347d5c78a09b2f09b68b509:1617213417854/1/2/2.png?api_key=1dfba9e5fb93bade9610d6c49e070d65f5760ddb"));
                    //map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/312bbd8ee4045d8f01fc09920b752d5d:1617439548216/2/1/1.png?api_key=1dfba9e5fb93bade9610d6c49e070d65f5760ddb"));
                    //map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/named/tpl_287b8eec_3e65_4c76_b254_f42fe6dcb063?auth_token=1dfba9e5fb93bade9610d6c49e070d65f5760ddb"));
                    //map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/312bbd8ee4045d8f01fc09920b752d5d:1617439548216/:layer/1/1/1.mvt"));
//                    map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/312bbd8ee4045d8f01fc09920b752d5d:1617439548216/{z}/{x}/{y}.png",
//                            new TileLayerOptions().setMaxZoom(1)));
//                    map.addLayer(new TileLayer("https://dakochik.carto.com/api/v1/map/dakochik@285a4b42@768d66e72347d5c78a09b2f09b68b509:1617213417854/1/2/2.png?api_key=1dfba9e5fb93bade9610d6c49e070d65f5760ddb",
//                            new TileLayerOptions().setMaxZoom(2)));

                    // sample code showing how to use the Java API
                    map.addLayer(new Circle(new LatLng(51.508, -0.11), 500,
                            new PathOptions().setColor("red").setFillColor("#f03").setOpacity(0.5)
                    ).bindPopup("I am a Circle"));
                    map.addLayer(new Polygon(new LatLng[] {
                            new LatLng(51.509, -0.08),
                            new LatLng(51.503, -0.06),
                            new LatLng(51.51, -0.047)
                    }).bindPopup("I am a Polygon"));

                }
            });
        }

        public Map getMap() {
            return map;
        }

        public WebView getWebView() {
            return webView;
        }
    }

    private static class Address {

        private final String name;
        private final double lat;
        private final double lng;

        public Address(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        @Override
        public String toString() {
            return name;
        }



    }
}

