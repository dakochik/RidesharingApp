package org.app.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polyline;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.app.validator.DoubleLatLonValidator;
import org.app.validator.WaitCoefficientValidator;
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
            Platform.runLater(() -> prBar.setProgress(0));
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

    public Button btnStart;
    public Button btnPause;
    public Button btnUpdate;
    public Button btnCont;
    public Button btnAdd;
    public Button btnAddFew;
    public Button closeBtn;
    public Button curtailBtn;

    public TextArea loggerTA;

    public Ellipse sUC;
    public Ellipse middleC;
    public Ellipse rightBC;
    public Ellipse rightUC;
    public Ellipse leftBC;
    public Ellipse leftMC;
    public Ellipse leftUC;

    PathTransition trMC;
    PathTransition trSC;
    PathTransition trRBC;
    PathTransition trRUC;
    PathTransition trLBC;
    PathTransition trLMC;
    PathTransition trLUC;


    public JFXTextField orLatTF;
    public JFXTextField orLonTF;
    public JFXTextField idTF;
    public JFXTextField destLatTF;
    public JFXTextField destLonTF;
    public JFXTextField wTime;
    public JFXTextField tCoefficient;

    public JFXDatePicker dP;
    public JFXTimePicker tP;
    public JFXRadioButton rBtn;

    public PrimaryController() {
        comp = new RideSharingComputer(notifier);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTFListeners();
        adapter = new CartoDataBaseAdapter();

        initPath();

        trRBC.play();
        rightBC.setFill(Paint.valueOf("#FFE599"));

        btnPause.setDisable(true);

        btnCont.setDisable(true);
    }

    private void initPath(){
        Polyline mPl = new Polyline();
        mPl.getPoints().addAll(-60.0, 0.0, 60.0, 0.0, -60.0, 0.0);
        Polyline sPl = new Polyline();
        sPl.getPoints().addAll(-15.0, 0.0, 15.0, 0.0, -15.0, 0.0);
        Polyline lB = new Polyline();
        lB.getPoints().addAll(-20.0,20.0, 30.0, -30.0, -20.0, 20.0);
        Polyline lM = new Polyline();
        lM.getPoints().addAll(-15.0, 0.0, 10.0, 0.0, -15.0, 0.0);
        Polyline lU = new Polyline();
        lU.getPoints().addAll(-20.0, -20.0, 30.0, 30.0, -20.0, -20.0);
        Polyline rB = new Polyline();
        rB.getPoints().addAll(-15.0, -15.0, 15.0, 15.0, -15.0, -15.0);
        Polyline rU = new Polyline();
        rU.getPoints().addAll(-15.0, 15.0, 10.0, -15.0, -15.0, 15.0);

        trMC = new PathTransition();
        trMC.setNode(middleC);
        trMC.setDuration(Duration.seconds(0.5));
        trMC.setPath(mPl);
        trMC.setCycleCount(PathTransition.INDEFINITE);

        trSC = new PathTransition();
        trSC.setNode(sUC);
        trSC.setDuration(Duration.seconds(0.4));
        trSC.setPath(sPl);
        trSC.setCycleCount(PathTransition.INDEFINITE);

        trLBC = new PathTransition();
        trLBC.setNode(leftBC);
        trLBC.setDuration(Duration.seconds(0.6));
        trLBC.setPath(lB);
        trLBC.setCycleCount(PathTransition.INDEFINITE);

        trLMC = new PathTransition();
        trLMC.setNode(leftMC);
        trLMC.setDuration(Duration.seconds(0.4));
        trLMC.setPath(lM);
        trLMC.setCycleCount(PathTransition.INDEFINITE);

        trLUC = new PathTransition();
        trLUC.setNode(leftUC);
        trLUC.setDuration(Duration.seconds(0.6));
        trLUC.setPath(lU);
        trLUC.setCycleCount(PathTransition.INDEFINITE);

        trRBC = new PathTransition();
        trRBC.setNode(rightBC);
        trRBC.setDuration(Duration.seconds(0.5));
        trRBC.setPath(rB);
        trRBC.setCycleCount(PathTransition.INDEFINITE);

        trRUC = new PathTransition();
        trRUC.setNode(rightUC);
        trRUC.setDuration(Duration.seconds(0.5));
        trRUC.setPath(rU);
        trRUC.setCycleCount(PathTransition.INDEFINITE);
    }

    private void initTFListeners() {
        wTime.setText("20");
        tCoefficient.setText("1.8");
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("Введите значение");
        DoubleLatLonValidator lonValidator = new DoubleLatLonValidator(180);
        DoubleLatLonValidator latValidator = new DoubleLatLonValidator(90);
        WaitCoefficientValidator waitingTimeV = new WaitCoefficientValidator(0);
        WaitCoefficientValidator tripCoefficient = new WaitCoefficientValidator(1);

        orLonTF.getValidators().addAll(validator, lonValidator);
        orLatTF.getValidators().addAll(validator, latValidator);
        destLonTF.getValidators().addAll(validator, lonValidator);
        destLatTF.getValidators().addAll(validator, latValidator);
        dP.getValidators().add(validator);
        tP.getValidators().add(validator);
        wTime.getValidators().add(waitingTimeV);
        tCoefficient.getValidators().add(tripCoefficient);

        orLonTF.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                orLonTF.validate();
            }
        });
        orLatTF.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                orLatTF.validate();
            }
        });
        destLonTF.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                destLonTF.validate();
            }
        });
        destLatTF.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                destLatTF.validate();
            }
        });
        dP.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                dP.validate();
            }
        });
        tP.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tP.validate();
            }
        });
        wTime.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                wTime.validate();
            }
        });
        tCoefficient.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (!t1) {
                tCoefficient.validate();
            }
        });
    }

    public void btnStartPressed(ActionEvent event) {
        step = 1;
        if (timer != null) {
            timer.cancel();
        }
        btnCont.setDisable(true);
        btnPause.setDisable(false);
        Task<Void> taskF = clearTablesAndDownloadData();
        playMiddle();

        taskF.setOnSucceeded(it -> {
            Platform.runLater(this::pauseMiddle);
            prBar.setProgress(0);
            loggerTA.appendText("Timer is set\n");
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), 60000, 60000);
        });
        new Thread(taskF).start();
    }

    public void btnPausePressed(ActionEvent event) {
        loggerTA.appendText("\nComputing have been paused\n");
        prBar.setProgress(0);
        btnCont.setDisable(false);
        btnPause.setDisable(true);
        timer.cancel();
        timer.purge();
    }

    public void btnUpdatePressed(ActionEvent event) {
        prBar.setProgress(0);
        timer.cancel();
        timer.purge();

        Task<Void> updateAndPush = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                computeAndPush(true);
                return null;
            }
        };

        loggerTA.appendText("\nForce updating...\n");
        prBar.setProgress(0.2);

        updateAndPush.setOnSucceeded(it -> {
            btnCont.setDisable(true);
            btnPause.setDisable(false);
            prBar.setProgress(0);
            timer = new Timer();
            loggerTA.appendText("Timer is resumed\n");
            timer.schedule(new CustomTimerTask(), 60000, 60000);
        });

        new Thread(updateAndPush).start();
    }

    public void btnContinuePressed(ActionEvent event) {
        btnCont.setDisable(true);
        btnPause.setDisable(false);

        prBar.setProgress(0);
        timer = new Timer();
        loggerTA.appendText("Timer is resumed\n");
        timer.schedule(new CustomTimerTask(), 60000, 60000);
    }

    public void btnAddPressed(ActionEvent event) {
        var newRequest = verifyRequest();

        if (newRequest.isPresent()) {
            idTF.setText(newRequest.get().tripId);

            if(rBtn.isSelected()){
                List<Car> carL = new ArrayList<>() {{
                    add(new Car(3, newRequest.get()));
                }};
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        synchronized (comp) {
                            comp.addAllCars(carL);
                        }

                        Platform.runLater(() -> {
                            loggerTA.appendText("Pushing trip...\n");
                            playMiddle();
                            playTRT();
                        });

                        try {
                            adapter.pushTrips(carL);
                            Platform.runLater(() -> loggerTA.appendText("Trip was successfully added\n"));
                        } catch (IllegalAccessException e) {
                            Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to push new trip:\n" + e.getMessage() + "\n"));
                        }

                        Platform.runLater(() -> {
                            pauseMiddle();
                            stopTRT();
                        });

                        return null;
                    }
                };

                new Thread(task).start();
            }
            else {

                List<TripRequest> reqL = new ArrayList<>() {{
                    add(newRequest.get());
                }};

                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        synchronized (comp) {
                            comp.addAllTasks(reqL);
                        }

                        Platform.runLater(() -> {
                            loggerTA.appendText("Pushing request...\n");
                            playMiddle();
                            playTRT();
                        });

                        try {
                            adapter.pushRequests(reqL);
                            Platform.runLater(() -> loggerTA.appendText("Request was successfully added\n"));
                        } catch (IllegalAccessException e) {
                            Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to push new request:\n" + e.getMessage() + "\n"));
                        }

                        Platform.runLater(() -> {
                            pauseMiddle();
                            stopTRT();
                        });

                        return null;
                    }
                };

                new Thread(task).start();
            }
        }
    }

    public void btnAddFewPressed(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add data from file");
        File destination = fileChooser.showOpenDialog(btnAddFew.getScene().getWindow());

        if (destination == null) {
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
                    Platform.runLater(() -> {
                        loggerTA.appendText("Pushing new requests...\n");
                        playMiddle();
                        playTRT();
                    });
                    try {
                        adapter.pushRequests(reqL);
                        Platform.runLater(() -> loggerTA.appendText("Requests were successfully added\n"));
                    } catch (IllegalAccessException e) {
                        Platform.runLater(() ->
                                loggerTA.appendText("[ERROR] : Impossible to push new requests:\n" + e.getMessage() + "\n"));
                    }

                    Platform.runLater(() -> {
                        pauseMiddle();
                        stopTRT();
                    });

                    return null;
                }
            };

            new Thread(task).start();
        } catch (IOException e) {
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
                Platform.runLater(()->playTRT());
                synchronized (comp) {
                    comp.clearTrips();
                    comp.clearRequests();
                }

                Platform.runLater(() -> loggerTA.appendText("Clearing requests table...\n"));
                try {
                    adapter.clearRequestsTable();
                } catch (IllegalAccessException e) {
                    Platform.runLater(() ->
                            loggerTA.appendText("[ERROR] : Impossible to clear requests table:\n" + e.getMessage() + "\n"));
                    return null;
                }

                Platform.runLater(() -> {
                    notifier.eventNotifier(0.05);
                    loggerTA.appendText("Clearing trips table...\n");
                });

                try {
                    adapter.clearTripsTable();
                } catch (IllegalAccessException e) {
                    Platform.runLater(() ->
                            loggerTA.appendText("[ERROR] : Impossible to clear trips table:\n" + e.getMessage() + "\n"));
                    return null;
                }

                Platform.runLater(() -> {
                    stopTRT();
                    playMainT();
                    notifier.eventNotifier(0.05);
                    loggerTA.appendText("Downloading data...\n");
                });

                try {
                    distributeData(adapter.readRequests());
                } catch (IllegalAccessException e) {
                    Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to get data:\n" + e.getMessage() + "\n"));
                    return null;
                }

                Platform.runLater(() -> {
                    stopMainT();
                    notifier.eventNotifier(0.1);
                    loggerTA.appendText("We successfully got data\n");
                    pauseMiddle();
                });

                computeAndPush(false);
                return null;
            }
        };
    }

    private void getUpdates() {
        comp.clearRequests();

        Platform.runLater(() -> {
            loggerTA.appendText("Getting not handled requests...\n");
            playMiddle();
            playTRT();
        });

        try {
            synchronized (comp) {
                comp.addAllTasks(adapter.readNotHandledRequests());
            }
        } catch (IllegalAccessException e) {
            Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to get requests:\n" + e.getMessage() + "\n"));
            return;
        }finally {
            Platform.runLater(() -> {
                stopTRT();
                pauseMiddle();
            });
        }

        Platform.runLater(() -> {
            loggerTA.appendText("We successfully got requests\n");
            notifier.eventNotifier(0.2);
        });
    }

    private void computeAndPush(boolean update) {
        synchronized (comp) {
            Platform.runLater(() -> {
                loggerTA.appendText("Computing...\n");
                playALg();
            });
            comp.compute();
            Platform.runLater(() -> {
                loggerTA.appendText("Computing have been successfully finished\n");
                stopAlg();
                playMiddle();
                playTRT();
            });

            if (update) {
                Platform.runLater(() -> loggerTA.appendText("Updating requests...\n"));
                try {
                    adapter.updateRequestsByTripId(comp.requests);
                } catch (IllegalAccessException e) {
                    Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to update requests:\n" + e.getMessage() + "\n"));
                    return;
                }
                Platform.runLater(() -> {
                    notifier.eventNotifier(0.1);
                    loggerTA.appendText("Updating has been successfully finished\n");
                    loggerTA.appendText("Updating trips...\n");
                });
                try {
                    adapter.updateTripByTripId(comp.cars);
                } catch (IllegalAccessException e) {
                    Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to update trips:\n" + e.getMessage() + "\n"));
                    return;
                }
                Platform.runLater(() -> {
                    notifier.eventNotifier(0.1);
                    loggerTA.appendText("Updating has been successfully finished\n");
                    pauseMiddle();
                    stopTRT();
                });
            } else {
                Platform.runLater(() -> loggerTA.appendText("Uploading requests...\n"));
                try {
                    adapter.pushRequests(comp.requests);
                } catch (IllegalAccessException e) {
                    Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to upload requests:\n" + e.getMessage() + "\n"));
                    return;
                }
                Platform.runLater(() -> {
                    notifier.eventNotifier(0.1);
                    loggerTA.appendText("Uploading has been successfully finished\n");
                    loggerTA.appendText("Uploading trips...\n");
                });
                try {
                    adapter.pushTrips(comp.cars);
                } catch (IllegalAccessException e) {
                    Platform.runLater(() -> loggerTA.appendText("[ERROR] : Impossible to upload trips:\n" + e.getMessage() + "\n"));
                    return;
                }
                Platform.runLater(() -> {
                    notifier.eventNotifier(0.1);
                    loggerTA.appendText("Uploading has been successfully finished\n");
                    pauseMiddle();
                    stopTRT();
                });
            }
        }
    }

    private Optional<TripRequest> verifyRequest() {
        double orLat;
        double orLon;
        double desLat;
        double desLon;
        double waitingT;
        double tCoeff;

        try {
            orLat = Double.parseDouble(orLatTF.getText());
        } catch (NumberFormatException | NullPointerException e) {
            orLatTF.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Широта точки отправлени должна быть дробным числом");
            return Optional.empty();
        }

        try {
            orLon = Double.parseDouble(orLonTF.getText());
        } catch (NumberFormatException | NullPointerException e) {
            orLonTF.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Долгота точки отправлени должна быть дробным числом");
            return Optional.empty();
        }

        try {
            desLat = Double.parseDouble(destLatTF.getText());
        } catch (NumberFormatException | NullPointerException e) {
            destLatTF.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Широта точки прибытия должна быть дробным числом");
            return Optional.empty();
        }

        try {
            desLon = Double.parseDouble(destLonTF.getText());
        } catch (NumberFormatException | NullPointerException e) {
            destLonTF.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Долгота точки прибытия должна быть дробным числом");
            return Optional.empty();
        }

        if (dP.getValue() == null) {
            dP.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Необходимо ввести дату публикации запроса");
            return Optional.empty();
        }

        if (tP.getValue() == null) {
            tP.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Необходимо ввести время публикации запроса");
            return Optional.empty();
        }

        try {
            waitingT = Double.parseDouble(wTime.getText());
        } catch (NumberFormatException | NullPointerException e) {
            wTime.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Максимальное время ожидания должно быть дробным числом");
            return Optional.empty();
        }

        try {
            tCoeff = Double.parseDouble(tCoefficient.getText());
        } catch (NumberFormatException | NullPointerException e) {
            tCoefficient.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Максимальный коэффициент увеличиения поездки должен быть дробным числом");
            return Optional.empty();
        }

        if (waitingT < 0){
            wTime.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Максимальное время ожидания не должно быть отрицательным");
            return Optional.empty();
        }

        if (tCoeff < 1.0){
            wTime.validate();
            showAlert(Alert.AlertType.WARNING, "Некорректные входные данные", "Максимальный коэффициент увеличиения поездки не должен быть меньше 1");
            return Optional.empty();
        }

        LocalDateTime time = LocalDateTime.of(dP.getValue(), tP.getValue());
        TripRequest request = new TripRequest(new Coordinate(orLat, orLon),
                new Coordinate(desLat, desLon), waitingT, tCoeff-1,
                time, UUID.randomUUID().toString());
        return Optional.of(request);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void btnClosePressed(ActionEvent event) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        var stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    public void btnCurtailPressed(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setIconified(true);
    }

    private void playMiddle(){
        middleC.setFill(Paint.valueOf("#FFE599"));
        sUC.setFill(Paint.valueOf("#FFE599"));
        trMC.play();
        trSC.play();
    }

    private void pauseMiddle(){
        middleC.setFill(Paint.valueOf("#B5B0A1"));
        sUC.setFill(Paint.valueOf("#B5B0A1"));
        trMC.stop();
        trSC.stop();
    }

    private void playTRT(){
        leftBC.setFill(Paint.valueOf("#FFE599"));
        leftMC.setFill(Paint.valueOf("#FFE599"));
        trLBC.play();
        trLMC.play();
    }

    private void stopTRT(){
        leftBC.setFill(Paint.valueOf("#B5B0A1"));
        leftMC.setFill(Paint.valueOf("#B5B0A1"));
        trLBC.stop();
        trLMC.stop();
    }

    private void playMainT(){
        leftUC.setFill(Paint.valueOf("#FFE599"));
        trLUC.play();
    }

    private void stopMainT(){
        leftUC.setFill(Paint.valueOf("#B5B0A1"));
        trLUC.stop();
    }

    private void playALg(){
        rightUC.setFill(Paint.valueOf("#FFE599"));
        trRUC.play();
    }

    private void stopAlg(){
        rightUC.setFill(Paint.valueOf("#B5B0A1"));
        trRUC.stop();
    }
}
