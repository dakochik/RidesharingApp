package server.model.tree;

import org.locationtech.jts.geom.Coordinate;
import server.model.users.TripRequest;
import server.tools.DistanceCounter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Tree {

    /**
     * Исходный корень
     */
    public Node originalRoot;

    /**
     * Текущий корень
     */
    public Node currentRoot;

    /**
     * Пустая ли это машина (актуально для такси)
     */
    public boolean isEmpty = true;

    public Tree getShallowCopy() {
        Tree newTree = new Tree();

        newTree.originalRoot = originalRoot.getShallowCopy();

        Node node = newTree.originalRoot;
        ;
        while (!node.equals(currentRoot)) {
            node = node.children.get(0);
        }

        newTree.currentRoot = node;

        return newTree;
    }

    /**
     * Обрабатывает новый запрос на поездку
     *
     * @param tripRequest запрос на поездку
     * @return возможна ли она в этом дереве
     */
    public boolean handleRequest(TripRequest tripRequest) {
        Node newOrigin = new Node(NodeType.ORIGIN, null, tripRequest.origin, tripRequest.dateOfRequest);
        Node newDestination = new Node(NodeType.DESTINATION, null, tripRequest.destination, null);
        newOrigin.timeLimit = tripRequest.maxWaitingMeasure;
        newOrigin.pairedLoc = tripRequest.destination;
        newDestination.pairedLoc = tripRequest.origin;
        newDestination.timeLimit = tripRequest.maxWaitingMeasure;
        newDestination.tripCoefficient = tripRequest.distanceCoefficient;
        newDestination.distanceFromOriginToNode = DistanceCounter.measureDistance(newOrigin, newDestination);
        newDestination.arrivingTime = newOrigin.arrivingTime.plusMinutes((long) (newDestination.distanceFromOriginToNode / TripRequest.MINUTES_TO_KM));

        boolean res;
        if (originalRoot == null) {
            initRoot(newOrigin, newDestination);
            res = true;
        } else {
            var oD = newOrigin.arrivingTime;
            var sth = oD.until(currentRoot.arrivingTime, ChronoUnit.MINUTES);
            newOrigin.timeLimit -= sth * TripRequest.MINUTES_TO_KM;
            newDestination.timeLimit = newOrigin.timeLimit;

            res = insertNode(currentRoot, new ArrayList<>() {{
                add(newOrigin);
                add(newDestination);
            }}, 0);
        }

        //updateSlackTime(currentRoot);
        //System.out.printf("%s %s\n", currentRoot.slackTime, res);
        updateSlackTimeFromRoot(currentRoot);
        return res && currentRoot.slackTime > 0;
    }

    /**
     * Инициализация корня дерева.
     *
     * @param origin      точка отправления водителя машины.
     * @param destination точка прибытия водителя машины.
     */
    private void initRoot(Node origin, Node destination) {
        isEmpty = false;
        originalRoot = origin;
        currentRoot = originalRoot;
        origin.children.add(destination);
        destination.parent = origin;
        destination.distanceFromRootToNode = DistanceCounter.measureDistance(origin, destination);
        destination.status = TripStatus.ACTIVE;
        origin.status = TripStatus.ACTIVE;
        updateSlackTime(destination);
        origin.slackTime = destination.slackTime;
    }

    /**
     * Вставка нового узла в дерево
     *
     * @param nodes новые узлы (начало и конец поездки)
     * @return возможно ли вставить узел в дерево
     */
    private static boolean insertNode(Node root, ArrayList<Node> nodes, double depth) {
        if (root.children.isEmpty()) {
            return false;
        }
        Node node = nodes.get(0);
        var dist = DistanceCounter.measureDistance(root, node);
        if (isFeasible(root, node, depth + dist)) {
            boolean firstFlag = false;

            Node newNode = new Node(node.type, new ArrayList<>(),
                    node.location, root, root.arrivingTime.plusMinutes((long) (root.distanceFromOriginToNode / TripRequest.MINUTES_TO_KM)));
            newNode.slackTime = node.slackTime;
            newNode.distanceFromRootToNode = root.distanceFromRootToNode + dist;
            newNode.distanceFromOriginToNode = node.distanceFromOriginToNode;
            newNode.pairedLoc = node.pairedLoc;
            newNode.tripCoefficient = node.tripCoefficient;
            newNode.timeLimit = node.timeLimit;

            for (Node child : root.children) {
                firstFlag |= copyNode(newNode, child,
                        dist +
                                DistanceCounter.measureDistance(newNode, child)
                                - DistanceCounter.measureDistance(root, child));
            }

            if (firstFlag && nodes.size() > 1) {
                updateSlackTime(newNode);
                firstFlag = insertNode(newNode, new ArrayList<>() {{
                    add(nodes.get(1));
                }}, depth + dist);
            }

            boolean secondFlag;
            ArrayList<Integer> positionsForDeletion = new ArrayList<>();
            for (int i = 0; i < root.children.size(); ++i) {
                secondFlag = insertNode(root.children.get(i), nodes, root.children.get(i).distanceFromRootToNode);
                if (!secondFlag) {
                    positionsForDeletion.add(i);
                }
            }

            if (firstFlag) {
                root.children.add(newNode);
                newNode.parent = root;
            }

            if (firstFlag || positionsForDeletion.size() != root.children.size()) {
                for (int i = positionsForDeletion.size() - 1; i >= 0; --i) {
                    root.children.remove((int) positionsForDeletion.get(i));
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Копируем поддерево в новый узел. При этом происходит проверка на то, возможно ли проехать по текущей ветви,
     * не выбиваясь при этом из графика.
     *
     * @param copyHere вершина, в котороую копируем
     * @param fromHere вершина, из которой копируем
     * @param distance разница расстояний
     * @return успешно ли прошло копирование
     */
    public static boolean copyNode(Node copyHere, Node fromHere, double distance) {
        double newShift = fromHere.slackTime - distance;
        if (newShift <= 0) {
            return false;
        }

        boolean flag = false;

        Node copy = new Node(fromHere.type, new ArrayList<>(),
                fromHere.location, fromHere.arrivingTime.plusMinutes((long) (distance / TripRequest.MINUTES_TO_KM)));
        copy.slackTime = distance;
        copy.status = fromHere.status;
        copy.distanceFromRootToNode = fromHere.distanceFromRootToNode + distance;
        copy.distanceFromOriginToNode = fromHere.distanceFromOriginToNode;
        copy.tripCoefficient = fromHere.tripCoefficient;
        copy.pairedLoc = fromHere.pairedLoc;
        ;
        copy.timeLimit = fromHere.timeLimit;
        copy.parent = copyHere;

        for (Node node : fromHere.children) {
            flag |= copyNode(copy, node, distance);
        }

        if (fromHere.children.isEmpty() || flag) {
            copyHere.children.add(copy);
            return true;
        }

        return false;
    }

    /**
     * Обновление запасного времени для текщуего корня
     *
     * @param node узел, начиная с которого нужно обновить запасное время
     */
    public static void updateSlackTimeFromRoot(Node node) {
        double max = 0;
        for (Node child : node.children) {
            updateSlackTime(child);
            max = max == 0 ? child.slackTime : Math.max(max, child.slackTime);
        }
        node.slackTime = max;
    }

    /**
     * Обновление запасного времени
     *
     * @param node узел, начиная с которого нужно обновить запасное время
     */
    public static void updateSlackTime(Node node) {
        double res;
        if (node.status == TripStatus.WAITING) {
            res = node.timeLimit - node.distanceFromRootToNode;
            res += node.type == NodeType.DESTINATION ? node.distanceFromOriginToNode * (1 + node.tripCoefficient) : 0;
        } else {
            res = (1 + node.tripCoefficient) * node.distanceFromOriginToNode -
                    node.distanceFromRootToNode;
        }

        double max = -1;
        for (Node child : node.children) {
            updateSlackTime(child);
            max = max == -1 ? child.slackTime : Math.max(max, child.slackTime);
        }
        if (max != -1) {
            node.slackTime = Math.min(max, res);
        } else {
            node.slackTime = res;
        }
    }

    /**
     * Возможно ли добавить вершину в текущее поддерево.
     *
     * @param root    корень поддерева
     * @param newNode новая вершина
     * @param dist    путь от корня всего дерева, до новой вершины
     * @return не рушит ли планы корня поддерева и его детей новая вершина
     */
    public static boolean isFeasible(Node root, Node newNode, double dist) {
        if (newNode.type == NodeType.ORIGIN && (dist < newNode.timeLimit)) {
            return true;
        }
        for (var child : root.children) {
            if (dist - root.distanceFromRootToNode + DistanceCounter.measureDistance(newNode, child)
                    - DistanceCounter.measureDistance(root, child) < child.slackTime) {
                return true;
            }

        }

        return false;
    }

    /**
     * Символьное представление дерева
     *
     * @return символьное представление
     */
    public String getStringRepresentation() {
        StringBuilder builder = new StringBuilder();

        builder.append("<Tree>\n");

        Node n = originalRoot;
        while (currentRoot.location != n.location) {
            builder.append(n.getSimpleStringRepresentation("\t"));
            n = n.children.get(0);
        }

        builder.append(currentRoot.getStringRepresentation("\t"));

        return builder.toString();
    }

    /**
     * Преобразование дерева решений в одно конкретное решение (в список точек прибытия и отправления).
     */
    public void convertIntoSolution() {
        Node currNode = currentRoot;

        while (!currNode.children.isEmpty()) {
            Node child = null;
            double max = -1;

            for (int i = 0; i < currNode.children.size(); ++i) {
                if (currNode.children.get(i).slackTime > max) {
                    child = currNode.children.get(i);
                    max = child.slackTime;
                }
            }

            if (child == null) {
                throw new IllegalStateException("There are no appropriate node");
            }

            currNode.children.clear();
            currNode.children.add(child);
            currNode = child;
        }
    }

    /**
     * Обновление дерева относительно временной точки (текущего времени). При этом необходимо сдвинуть текущий корень
     * до той точки, которую мы еще не успели проехать.
     *
     * @param time текущее время
     */
    public void updateCurrentRootByTime(LocalDateTime time) {
        Node node = currentRoot.children.isEmpty() ? currentRoot : currentRoot.children.get(0);

        ArrayList<Coordinate> locations = new ArrayList<>();

        while (node.arrivingTime.compareTo(time) < 0 && !node.children.isEmpty()) {
            if (node.type == NodeType.ORIGIN) {
                locations.add(node.pairedLoc);
                node.status = TripStatus.ACTIVE;
            } else {
                node.status = TripStatus.FINISHED;
                locations.add(node.location);
            }
            node = node.children.get(0);
        }
        if (node.type == NodeType.ORIGIN) {
            locations.add(node.pairedLoc);
        }

        double skippedKms = (currentRoot.arrivingTime.getDayOfMonth() - node.arrivingTime.getDayOfMonth()) * 24 * 60
                + (currentRoot.arrivingTime.getHour() - node.arrivingTime.getHour()) * 60
                + (currentRoot.arrivingTime.getMinute() - node.arrivingTime.getMinute()) * TripRequest.MINUTES_TO_KM;

        currentRoot = node;

        // Обновляем точки, до которых еще не доехали
        while (!node.children.isEmpty()) {
            if (locations.contains(node.location)) {
                node.status = TripStatus.ACTIVE;
                locations.remove(node.location);
            }
            node = node.children.get(0);
            node.timeLimit -= skippedKms;
        }
        if (locations.contains(node.location)) {
            node.status = TripStatus.ACTIVE;
            locations.remove(node.location);
        }

        // Обновляем точки отправления поездок, которые уже завершились
        node = currentRoot.parent;
        while (node != null && !locations.isEmpty()) {
            if (locations.contains(node.pairedLoc) && node.status != TripStatus.FINISHED) {
                node.status = TripStatus.FINISHED;
                locations.remove(node.pairedLoc);
            }
            node = node.parent;
        }
    }
}
