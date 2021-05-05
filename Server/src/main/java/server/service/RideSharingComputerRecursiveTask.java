package server.service;

import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static java.util.stream.Collectors.toList;

public class RideSharingComputerRecursiveTask extends RecursiveTask<Optional<Car>> {

    private final List<Car> cars;
    private final TripRequest request;

    private static final int THRESHOLD = 20;

    public RideSharingComputerRecursiveTask(List<Car> cars, TripRequest request) {
        this.cars = cars;
        this.request = request;
    }

    /**
     * Реализует рекурсивное решение задачи. Есди в списке машин меньше чем 20 элементов, вызывает метод processing,
     * иначе - разбивает текущую задачу на две рекурсивные подзадачи.
     *
     * @return возвращает объект типа Optional с наиболее подходящей машиной, если такая есть.
     */
    @Override
    protected Optional<Car> compute() {
        if (cars.size() > THRESHOLD) {
            var res = ForkJoinTask.invokeAll(createSubtasks())
                    .stream()
                    .map(ForkJoinTask::join).collect(toList());

            Car resCar = null;
            double max = Car.IMPOSSIBLE_TO_HANDLE;
            for (int i = 0; i < res.size(); ++i) {
                if (res.get(i).isPresent() && res.get(i).get().updatedTree.currentRoot.slackTime > max) {
                    if (resCar != null) {
                        resCar.resetTree();
                    }
                    resCar = res.get(i).get();
                    max = res.get(i).get().updatedTree.currentRoot.slackTime;
                }
            }

            return max != Car.IMPOSSIBLE_TO_HANDLE ? Optional.of(resCar) : Optional.empty();
        } else {
            return processing();
        }
    }

    /**
     * Разбивает задачу подбора машины на две поздзадчи.
     *
     * @return список новых подзадач.
     */
    private List<RideSharingComputerRecursiveTask> createSubtasks() {
        List<RideSharingComputerRecursiveTask> dividedTasks = new ArrayList<>();
        dividedTasks.add(new RideSharingComputerRecursiveTask(cars.subList(0, cars.size() / 2), request));
        dividedTasks.add(new RideSharingComputerRecursiveTask(cars.subList(cars.size() / 2, cars.size()), request));
        return dividedTasks;
    }

    /**
     * Последовательно проходит все элементы коллекции и вычиялет наиболее подходящую машину для текущего запроса.
     *
     * @return возвращает объект типа Optional с наиболее подходящей машиной, если такая есть.
     */
    private Optional<Car> processing() {
        Car res = cars.get(0);
        double max = cars.get(0).tryAdd(request);
        for (int i = 1; i < cars.size(); ++i) {
            if (max < cars.get(i).tryAdd(request)) {
                res.resetTree();
                max = cars.get(i).updatedTree.currentRoot.slackTime;
                res = cars.get(i);
            }
        }

        return max != Car.IMPOSSIBLE_TO_HANDLE ? Optional.of(res) : Optional.empty();
    }
}
