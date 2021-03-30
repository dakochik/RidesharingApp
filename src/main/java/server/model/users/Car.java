package server.model.users;

import server.model.tree.Node;
import server.model.tree.Tree;

import java.time.LocalDateTime;

public class Car {
    public static final double IMPOSSIBLE_TO_HANDLE = -1;

    public Tree tree;

    public Tree updatedTree;

    public TripRequest tripRequest;

    public int currentCapacity;

    public Car(int capacity, TripRequest trip) {
        tree = new Tree();
        tripRequest = trip;
        tree.handleRequest(trip);
        updatedTree = tree.getShallowCopy();
        currentCapacity = capacity;
    }

    /**
     * Попытка обработать запрос.
     *
     * @param newTrip новый запрос на поездку, который необходимо добавить.
     * @return возвращает запасное время текущей поездки, если запрос возможно обработать, иначе возвращает -1.
     */
    public double tryAdd(TripRequest newTrip) {
        if (currentCapacity == 0) {
            return IMPOSSIBLE_TO_HANDLE;
        }

        boolean response = updatedTree.handleRequest(newTrip);

        if (!response) {
            updatedTree = tree.getShallowCopy();
            return IMPOSSIBLE_TO_HANDLE;
        } else {
            return updatedTree.currentRoot.slackTime;
        }
    }

    /**
     * Подтверждение запроса.
     */
    public void confirmRequest() {
        updatedTree.convertIntoSolution();
        tree = updatedTree.getShallowCopy();
        --currentCapacity;
    }

    /**
     * Обновляет информацию о текущем местоположении машины.
     *
     * @param time новая временная точка (текущее время).
     */
    public void updateLocation(LocalDateTime time) {
        tree.updateCurrentRootByTime(time);
        updatedTree.updateCurrentRootByTime(time);
    }

    /**
     * Отмена внесенных изменений
     */
    public void resetTree() {
        updatedTree = tree.getShallowCopy();
    }

    public String getUri() {
        StringBuilder builder = new StringBuilder();

        builder.append("https://www.google.ru/maps/dir/");

        Node node = tree.originalRoot;
        builder.append(String.format("%s,%s/", node.location.latitude, node.location.longitude));
        while (!node.children.isEmpty()) {
            node = node.children.get(0);
            builder.append(String.format("%s,%s/", node.location.latitude, node.location.longitude));
        }

        return builder.toString();
    }
}
