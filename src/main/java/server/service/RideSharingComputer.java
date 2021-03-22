package server.service;

import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class RideSharingComputer {
    public ArrayList<Car> cars;

    public ArrayList<TripRequest> requests;

    public RideSharingComputer(){
        cars = new ArrayList<>();
        requests = new ArrayList<>();
    }

    public void addTask(TripRequest request){
        requests.add(request);
    }

    public void addAllTask(Collection<TripRequest> requests){
        this.requests.addAll(requests);
    }

    public void addCar(Car car){
        cars.add(car);
    }

    public void addAllCars(Collection<Car> cars){
        this.cars.addAll(cars);
    }

    public void compute(){
        for(int i =0; i < requests.size(); ++i){
            RideSharingComputerRecursiveTask task = new RideSharingComputerRecursiveTask(cars, requests.get(i));
            handleUpdating(task.compute());
        }
    }

    public void handleUpdating(Car car){
        car.confirmRequest();
    }
}
