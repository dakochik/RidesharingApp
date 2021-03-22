package server.service;

import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

public class RideSharingComputerRecursiveTask extends RecursiveTask<Car> {

    private List<Car> cars;
    private TripRequest request;

    private static final int THRESHOLD = 20;

    public RideSharingComputerRecursiveTask(List<Car> cars, TripRequest request){
        this.cars = cars;
        this.request = request;
    }

    @Override
    protected Car compute() {
        if(cars.size() > THRESHOLD){
              var res = ForkJoinTask.invokeAll(createSubtasks())
                    .stream()
                    .map(ForkJoinTask::join).collect(toList());

            Car resCar = res.get(0);
            double max = res.get(0).updatedTree.currentRoot.slackTime;
              for(int i = 1; i < res.size(); ++i){
                  if(res.get(i).updatedTree.currentRoot.slackTime > max){
                      resCar.resetTree();
                      resCar = res.get(i);
                      max = res.get(i).updatedTree.currentRoot.slackTime;
                  }
              }

              return resCar;
        }
        else{
            return processing();
        }
    }

    private ArrayList<RideSharingComputerRecursiveTask> createSubtasks() {
        ArrayList<RideSharingComputerRecursiveTask> dividedTasks = new ArrayList<>();
        dividedTasks.add(new RideSharingComputerRecursiveTask(cars.subList(0, cars.size()/2), request));
        dividedTasks.add(new RideSharingComputerRecursiveTask(cars.subList(cars.size()/2, cars.size()), request));
        return dividedTasks;
    }

    private Car processing(){
        Car res = cars.get(0);
        double max = cars.get(0).tryAdd(request);
        for(int i =1; i < cars.size(); ++i){
            if(max < cars.get(i).tryAdd(request)){
                res.resetTree();
                max = cars.get(i).updatedTree.currentRoot.slackTime;
                res = cars.get(i);
            }
        }

        return res;
    }
}
