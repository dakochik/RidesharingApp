package org.app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.locationtech.jts.geom.Coordinate;
import org.tools.CartoDataBaseAdapter;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.IterNotifier;
import server.service.RideSharingComputer;
import server.tools.CSVParser;

public class PrimaryController implements Initializable {
    private int step = 1;
    private final RideSharingComputer comp;
    private CartoDataBaseAdapter adapter;
    private Timer timer;

    class CustomTimerTask extends TimerTask {
        @Override
        public void run() {
            loggerTA.appendText(String.format("\n > > > > > > > > > > > > Step #%s < < < < < < < < < < < < \n", step++));
            prBar.setProgress(0);
            getUpdates();
            computeAndPush(true);
        }
    }

    private final IterNotifier notifier = new IterNotifier() {
        @Override
        public void eventNotifier() {
            prBar.setProgress(prBar.getProgress() + 0.6 / comp.requests.size());
        }

        @Override
        public void eventNotifier(double number) {
            prBar.setProgress(prBar.getProgress() + number);
        }
    };

    public AnchorPane paneLikeBane;

    public ProgressBar prBar;

    public Label lblStart;
    public Label lblPause;
    public Label lblUpdate;
    public Label lblCont;

    public Button btnStart;
    public Button btnPause;
    public Button btnUpdate;
    public Button btnCont;
    public Button btnAdd;
    public Button btnAddFew;
    public Button closeBtn;
    public Button curtailBtn;

    public TextArea loggerTA;

    public JFXTextField orLatTF;
    public JFXTextField orLonTF;
    public JFXTextField idTF;
    public JFXTextField destLatTF;
    public JFXTextField destLonTF;
    public JFXTextField hoursTF;
    public JFXTextField minutesTF;

    public DatePicker dP;

    public PrimaryController() {
        comp = new RideSharingComputer(notifier);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTFListeners();
        adapter = new CartoDataBaseAdapter();

        btnPause.setDisable(true);

        btnCont.setDisable(true);
    }

    private void initTFListeners() {
        orLonTF.textProperty().addListener(((observable, oldValue, newValue) -> orLonTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        orLatTF.textProperty().addListener(((observable, oldValue, newValue) -> orLatTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        destLonTF.textProperty().addListener(((observable, oldValue, newValue) -> destLonTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        destLatTF.textProperty().addListener(((observable, oldValue, newValue) -> destLatTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        dP.valueProperty().addListener(((observable, oldValue, newValue) -> dP
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        hoursTF.textProperty().addListener(((observable, oldValue, newValue) -> hoursTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
        minutesTF.textProperty().addListener(((observable, oldValue, newValue) -> minutesTF
                .setStyle("-fx-text-box-border: ladder(-fx-background, black 10%, derive(-fx-background,-15%) 30%);")));
    }

    public void btnStartPressed(ActionEvent event) {
        step = 1;
        if(timer!=null) {
            timer.cancel();
        }
        btnCont.setDisable(true);
        btnPause.setDisable(false);
        Task<Void> taskF = clearTablesAndDownloadData();

        taskF.setOnSucceeded(it -> {
            loggerTA.appendText("Timer is set\n");
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), 60000, 60000);
        });
        new Thread(taskF).start();
    }

    public void btnPausePressed(ActionEvent event) {
        btnCont.setDisable(false);
        btnPause.setDisable(true);
        timer.cancel();
        timer.purge();
    }

    public void btnUpdatePressed(ActionEvent event) {
        timer.cancel();
        timer.purge();

        Task<Void> updateAndPush = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                loggerTA.appendText("\nForce updating...\n");
                prBar.setProgress(0.2);
                computeAndPush(true);
                return null;
            }
        };

        updateAndPush.setOnSucceeded(it -> {
            btnCont.setDisable(true);
            btnPause.setDisable(false);
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), 60000, 60000);
        });

        new Thread(updateAndPush).start();
    }

    public void btnContinuePressed(ActionEvent event) {
        btnCont.setDisable(true);
        btnPause.setDisable(false);

        timer = new Timer();
        timer.schedule(new CustomTimerTask(), 60000, 60000);
    }

    public void btnAddPressed(ActionEvent event) {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var newRequest = verifyRequest();
                if (newRequest.isPresent()) {
                    idTF.setText(newRequest.get().tripId);
                    List<TripRequest> reqL = new ArrayList<>() {{
                        add(newRequest.get());
                    }};
                    synchronized (comp) {
                        comp.addAllTasks(reqL);
                    }
                    adapter.pushRequests(reqL);
                }
                return null;
            }
        };

        new Thread(task).start();
    }

    public void btnAddFewPressed(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add data from file");
        File destination = fileChooser.showOpenDialog(btnAddFew.getScene().getWindow());

        if(destination == null){
            return;
        }

        try {
            var newRequests = CSVParser.readFilteredData(destination.getPath());

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    List<TripRequest> reqL = new ArrayList<>() {{
                        addAll(newRequests);
                    }};

                    synchronized (comp) {
                        comp.addAllTasks(reqL);
                    }

                    adapter.pushRequests(reqL);
                    return null;
                }
            };

            new Thread(task).start();
        }catch (IOException e){
            showAlert(Alert.AlertType.WARNING, "Incorrect input data",
                    String.format("File %s contains incorrect input data", destination.getPath()));
        }
    }

    private void distributeData(List<TripRequest> requests) {
        synchronized (comp) {
            for (int i = 0; i < 2 * requests.size() / 3; ++i) {
                comp.addTask(requests.get(i));
            }

            for (int i = 2 * requests.size() / 3 + 1; i < requests.size(); ++i) {
                comp.addCar(new Car(3, requests.get(i)));
            }
        }
    }

    private Task<Void> clearTablesAndDownloadData() {
        return new Task<>() {
            @Override
            protected Void call() {
                synchronized (comp) {
                    comp.clearTrips();
                    comp.clearRequests();
                }

                loggerTA.appendText("Clearing requests table...\n");
                try {
                    adapter.clearRequestsTable();
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to clear requests table:\n" + e.getMessage() + "\n");
                    return null;
                }
                notifier.eventNotifier(0.05);

                loggerTA.appendText("Clearing trips table...\n");
                try {
                    adapter.clearTripsTable();
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to clear trips table:\n" + e.getMessage() + "\n");
                    return null;
                }
                notifier.eventNotifier(0.05);

                loggerTA.appendText("Downloading data...\n");
                try {
                    distributeData(adapter.readRequests());
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to get data:\n" + e.getMessage() + "\n");
                    return null;
                }

                notifier.eventNotifier(0.1);
                loggerTA.appendText("We successfully got data\n");

                computeAndPush(false);
                return null;
            }
        };
    }

    private void getUpdates() {
        comp.clearRequests();

        loggerTA.appendText("Getting not handled requests...\n");
        try {
            synchronized (comp) {
                comp.addAllTasks(adapter.readNotHandledRequests());
            }
        } catch (IllegalAccessException e) {
            loggerTA.appendText("[ERROR] : Impossible to get requests:\n" + e.getMessage() + "\n");
            return;
        }
        loggerTA.appendText("We successfully got requests\n");
        notifier.eventNotifier(0.2);
    }

    private void computeAndPush(boolean update) {
        synchronized (comp) {
            loggerTA.appendText("Computing...\n");
            comp.compute();
            loggerTA.appendText("Computing has been successfully finished\n");

            if (update) {
                loggerTA.appendText("Updating requests...\n");
                try {
                    adapter.updateRequestsByTripId(comp.requests);
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to update requests:\n" + e.getMessage() + "\n");
                    return;
                }
                notifier.eventNotifier(0.1);
                loggerTA.appendText("Updating has been successfully finished\n");

                loggerTA.appendText("Updating trips...\n");
                try {
                    adapter.updateTripByTripId(comp.cars);
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to update trips:\n" + e.getMessage() + "\n");
                    return;
                }
                notifier.eventNotifier(0.1);
                loggerTA.appendText("Updating has been successfully finished\n");
            } else {
                loggerTA.appendText("Uploading requests...\n");
                try {
                    adapter.pushRequests(comp.requests);
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to upload requests:\n" + e.getMessage() + "\n");
                    return;
                }
                notifier.eventNotifier(0.1);
                loggerTA.appendText("Uploading has been successfully finished\n");

                loggerTA.appendText("Uploading trips...\n");
                try {
                    adapter.pushTrips(comp.cars);
                } catch (IllegalAccessException e) {
                    loggerTA.appendText("[ERROR] : Impossible to upload trips:\n" + e.getMessage() + "\n");
                    return;
                }
                notifier.eventNotifier(0.1);
                loggerTA.appendText("Uploading has been successfully finished\n");
            }
        }
    }

    private Optional<TripRequest> verifyRequest() {
        double orLat;
        double orLon;
        double desLat;
        double desLon;
        int hours;
        int minutes;

        try {
            orLat = Double.parseDouble(orLatTF.getText());
        } catch (NumberFormatException e) {
            orLatTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Широта точки отправлени должна быть дробным числомю");
            return Optional.empty();
        }

        try {
            orLon = Double.parseDouble(orLonTF.getText());
        } catch (NumberFormatException e) {
            orLonTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Долгота точки отправлени должна быть дробным числомю");
            return Optional.empty();
        }

        try {
            desLat = Double.parseDouble(destLatTF.getText());
        } catch (NumberFormatException e) {
            destLatTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Широта точки прибытия должна быть дробным числомю");
            return Optional.empty();
        }

        try {
            desLon = Double.parseDouble(destLonTF.getText());
        } catch (NumberFormatException e) {
            destLonTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Долгота точки прибытия должна быть дробным числомю");
            return Optional.empty();
        }

        try {
            hours = Integer.parseInt(hoursTF.getText());
            if (hours < 0 || hours > 23) {
                hoursTF.setStyle("-fx-text-box-border: red ;");
                showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Значение часа отправления должно лежать на отрезке [0;23]");
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            hoursTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Значение часа отправления должно быть целым числом");
            return Optional.empty();
        }

        try {
            minutes = Integer.parseInt(minutesTF.getText());
            if (minutes < 0 || minutes > 59) {
                minutesTF.setStyle("-fx-text-box-border: red ;");
                showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Значение минут отправления должно лежать на отрезке [0;59]");
                return Optional.empty();
            }
        } catch (NumberFormatException e) {
            minutesTF.setStyle("-fx-text-box-border: red ;");
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Значение минут отправления должно быть целым числом");
            return Optional.empty();
        }

        LocalDateTime time = LocalDateTime.of(dP.getValue(), LocalTime.of(hours, minutes));
        TripRequest request = new TripRequest(new Coordinate(orLat, orLon),
                new Coordinate(desLat, desLon), TripRequest.DEFAULT_WAITING_TIME, TripRequest.DEFAULT_TRIP_COEFFICIENT,
                time, UUID.randomUUID().toString());
        return Optional.of(request);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void btnClosePressed(ActionEvent event){
        var stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void btnCurtailPressed(ActionEvent event){
        Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }
}
