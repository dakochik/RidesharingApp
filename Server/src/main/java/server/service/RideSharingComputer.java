package server.service;

import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class RideSharingComputer {
    private final IterNotifier notifier;
    public final List<Car> cars;

    public List<TripRequest> requests;

    public int confirmedReq = 0;

    public RideSharingComputer() {
        cars = new ArrayList<>();
        requests = new ArrayList<>();
        notifier = new IterNotifier() {
            @Override
            public void eventNotifier() {
            }

            @Override
            public void eventNotifier(double number) {
            }
        };
    }

    public RideSharingComputer(IterNotifier notifier) {
        cars = new ArrayList<>();
        requests = new ArrayList<>();
        this.notifier = notifier;
    }

    public void addTask(TripRequest request) {
        requests.add(request);
    }

    public void addAllTasks(Collection<TripRequest> requests) {
        this.requests.addAll(requests);
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public void addAllCars(Collection<Car> cars) {
        this.cars.addAll(cars);
    }

    public void clearRequests() {
        requests.clear();
    }

    public void clearTrips() {
        cars.clear();
    }

    /**
     * Запускает рекурсивный подбор машины для списка запросов.
     */
    public final void compute() {
        for (int i = 0; i < requests.size(); ++i) {
            ForkJoinPool pool = ForkJoinPool.commonPool();
            RideSharingComputerRecursiveTask task = new RideSharingComputerRecursiveTask(cars, requests.get(i));
            var res = pool.invoke(task);
            res.ifPresent(this::handleUpdating);
            notifier.eventNotifier();
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
