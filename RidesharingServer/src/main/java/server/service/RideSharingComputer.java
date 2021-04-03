package server.service;

import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.Collection;

public class RideSharingComputer {
    public ArrayList<Car> cars;

    public ArrayList<TripRequest> requests;

    public int confirmedReq = 0;

    public RideSharingComputer() {
        cars = new ArrayList<>();
        requests = new ArrayList<>();
    }

    public void addTask(TripRequest request) {
        requests.add(request);
    }

    public void addAllTask(Collection<TripRequest> requests) {
        this.requests.addAll(requests);
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public void addAllCars(Collection<Car> cars) {
        this.cars.addAll(cars);
    }

    /**
     * Запускает рекурсивный подбор машины для списка запросов.
     */
    public final void compute() {
        for (int i = 0; i < requests.size(); ++i) {
            RideSharingComputerRecursiveTask task = new RideSharingComputerRecursiveTask(cars, requests.get(i));
            var res = task.compute();
            res.ifPresent(this::handleUpdating);
        }
    }

    /**
     * Обработчик подтверждения запроса.
     *
     * @param car машина, принявшая новый запрос на поездку.
     */
    public void handleUpdating(Car car) {
        if (car != null) {
            car.confirmRequest();
            ++confirmedReq;
        }
    }
}
