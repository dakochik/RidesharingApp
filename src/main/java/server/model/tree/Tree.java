package server.model.tree;

import server.model.Location;
import server.model.users.TripRequest;
import server.tools.DistanceCounter;

import java.util.ArrayList;
import java.util.stream.Collectors;

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

    public Tree getShallowCopy(){
        Tree newTree = new Tree();

        newTree.originalRoot = originalRoot.getShallowCopy();

        Node node = newTree.originalRoot;;
        while(!node.equals(currentRoot)){
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
        Node newOrigin = new Node(NodeType.ORIGIN, null, tripRequest.origin);
        Node newDestination = new Node(NodeType.DESTINATION, null, tripRequest.destination);
        newOrigin.timeLimit = tripRequest.maxWaitingMeasure;
        newDestination.timeLimit = tripRequest.maxWaitingMeasure;
        newDestination.tripCoefficient = tripRequest.distanceCoefficient;
        newDestination.distanceFromOriginToNode = DistanceCounter.measureDistance(newOrigin, newDestination);
        newDestination.distanceFromOriginToNodeCurrent = newDestination.distanceFromOriginToNode;

        boolean res;
        if (originalRoot == null) {
            initRoot(newOrigin, newDestination);
            res = true;
        } else {
            res = insertNode(currentRoot, new ArrayList<>() {{
                add(newOrigin);
                add(newDestination);
            }}, 0);
        }

        updateSlackTime(currentRoot);
        return res;
    }

    private void initRoot(Node origin, Node destination) {
        isEmpty = false;
        originalRoot = origin;
        currentRoot = originalRoot;
        origin.children.add(destination);
        destination.parent = origin;
        destination.distanceFromRootToNode = DistanceCounter.measureDistance(origin, destination);
        destination.status = TripStatus.ACTIVE;
        updateSlackTime(destination);
        updateSlackTime(origin);
    }

    /**
     * Вставка нового узла в дерево
     *
     * @param nodes новые узлы (начало и конец поездки)
     * @return возможно ли вставить узел в дерево
     */
    private static boolean insertNode(Node root, ArrayList<Node> nodes, double depth) {
        if(root.children.isEmpty()){
            return false;
        }
        Node node = nodes.get(0);
        var dist = DistanceCounter.measureDistance(root, node);
        if (isFeasible(root, node, depth + dist)){
            boolean firstFlag = false;

            Node newNode = new Node(node.type, new ArrayList<>(), node.location, root);
            newNode.slackTime = node.slackTime;
            newNode.distanceFromRootToNode = root.distanceFromRootToNode + dist;
            newNode.distanceFromOriginToNode = node.distanceFromOriginToNode;
            newNode.distanceFromOriginToNodeCurrent = node.distanceFromOriginToNodeCurrent;
            newNode.tripCoefficient = node.tripCoefficient;
            newNode.timeLimit = node.timeLimit;

            for (Node child : root.children) {
                firstFlag |= copyNode(newNode, child,
                        dist +
                                DistanceCounter.measureDistance(newNode, child)
                                - DistanceCounter.measureDistance(root, child));
            }

            if ((firstFlag || root.children.isEmpty()) && nodes.size() > 1) {
                updateSlackTime(newNode);
                firstFlag = insertNode(newNode, new ArrayList<>() {{
                    add(nodes.get(1));
                }}, -nodes.get(1).distanceFromOriginToNode);
            }

            boolean secondFlag;
            ArrayList<Integer> positionsForDeletion = new ArrayList<>();
            for (int i = 0; i < root.children.size(); ++i) {
                if(nodes.size() == 1){
                    var dest = nodes.get(0);
                    var child = root.children.get(i);
                    dest.distanceFromOriginToNodeCurrent = DistanceCounter.measureDistance(root, child) +
                            DistanceCounter.measureDistance(child, dest);
                }
                secondFlag = insertNode(root.children.get(i), nodes, depth + dist);
                if (!secondFlag) {
                    positionsForDeletion.add(i);
                }
            }

            if (firstFlag) {
                root.children.add(newNode);
                newNode.parent = root;
                //updateSlackTime(newNode);
            }
//            else if (positionsForDeletion.size() == root.children.size()) {
//                secondFlag = false;
//            }

            if(firstFlag || positionsForDeletion.size() != root.children.size()){
                for (int i = positionsForDeletion.size() - 1; i >= 0; --i) {
                    root.children.remove((int)positionsForDeletion.get(i));
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Фактически мы проверяем, чтобы у добавляемых вершин оставался ненулевой запас лишнего времени.
     *
     * @param copyHere вершина, в котороую копируем
     * @param fromHere вершина, из которой копируем
     * @param distance разница расстояний
     * @return успешно ли прошло копирование
     */
    public static boolean copyNode(Node copyHere, Node fromHere, double distance) {
//        double newShift = fromHere.type == NodeType.ORIGIN ?
//                fromHere.timeLimit - distance:
//                fromHere.slackTime - distance;

        double newShift = fromHere.slackTime - distance;
        if (newShift <= 0) {
            return false;
        }

        boolean flag = false;

        Node copy = new Node(fromHere.type, new ArrayList<>(), fromHere.location);
        copy.slackTime = newShift;
        copy.status = fromHere.status;
        copy.distanceFromRootToNode = fromHere.distanceFromRootToNode + distance;
        copy.distanceFromOriginToNode = fromHere.distanceFromOriginToNode;
        if(copy.status == TripStatus.ACTIVE){
            //
            copy.distanceFromOriginToNodeCurrent = fromHere.distanceFromOriginToNodeCurrent + distance;
        }
        copy.tripCoefficient = fromHere.tripCoefficient;
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

    public static void updateSlackTime(Node node) {
        double res;
        if (node.status == TripStatus.WAITING) {
            res = node.timeLimit - node.distanceFromRootToNode;
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

    public static boolean isFeasible(Node root, Node newNode, double dist) {
        if(newNode.type == NodeType.ORIGIN && (dist < newNode.timeLimit)){
            return true;
        }
        for(var child : root.children){
            if(dist - root.distanceFromRootToNode + DistanceCounter.measureDistance(newNode, child)
                    - DistanceCounter.measureDistance(root, child) < child.slackTime){
                return true;
            }

        }

        return false;
    }

    public String getStringRepresentation() {
        StringBuilder builder = new StringBuilder();

        builder.append("<Tree>\n\t").append(currentRoot.getStringRepresentation("\t"));

        return builder.toString();
    }

    public void convertIntoSolution(){
        Node currNode = currentRoot;

        while (!currNode.children.isEmpty()){
            double max = -1;

            for(int i =0; i < currNode.children.size(); ++i){
                if(currNode.children.get(i).slackTime > max){
                    max = currNode.children.get(i).slackTime;
                }
            }

            final double bound = max;
            currNode.children = currNode.children.stream().filter(it -> it.slackTime >= bound).collect(Collectors.toCollection(ArrayList::new));

            currNode = currNode.children.get(0);
        }
    }

    public void updateCurrentRoot(Location location){
        Node node = currentRoot;

        while(!node.location.equals(location) && !node.children.isEmpty()){
            node = node.children.get(0);
        }

        currentRoot = node;
    }
}
