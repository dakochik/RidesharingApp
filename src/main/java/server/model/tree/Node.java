package server.model.tree;

import server.model.Location;
import server.model.users.TripRequest;
import server.tools.DistanceCounter;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Node {
    /**
     * Родительский узел
     */
    public Node parent;

    /**
     * Тип узла
     */
    public NodeType type;

    /**
     * Статус поездки, к которой принадлежит текущий узел
     */
    public TripStatus status;

    /**
     * Дочерние узлы
     */
    public ArrayList<Node> children;

    /**
     * Географическое местоположение текущего узла
     */
    public Location location;

    /**
     * Пара для текущей локации. (т. отправления + прибытия)
     */
    public Location pairedLoc;

    /**
     * Максимальное время ожидания,
     * представляемое в виде расстояния, которое можно преодолеть за это время.
     */
    public double timeLimit;

    /**
     * Максиамльное время в поездке,
     * представляемое в виде расстояния, которое можно преодолеть за это время.
     */
    public double tripCoefficient;

    /**
     * Время движения от точки отправления к текущему узлу, представляемое в виде расстояния, которое можно преодолеть за это время.
     */
    public double distanceFromOriginToNode;

    /**
     * Время движения от точки отправления к текущему узлу, представляемое в виде расстояния, которое можно преодолеть за это время.
     * Расстояние считается для маршрута по всем злам о тточки отправления, до точки прибытия.
     */
    public double distanceFromOriginToNodeCurrent;

    /**
     * Запас времени (т.е. лишнее время), представляемый в виде расстояния, которое можно преодолеть за это время
     */
    public double slackTime = 0;

    /**
     * Время движения от корня к текущему узлу, представляемое в виде расстояния, которое можно преодолеть за это время
     */
    public double distanceFromRootToNode;

    public Node(NodeType type, ArrayList<Node> children, Location location){
        this(type, children, location, null);
    }

    public Node(NodeType type, ArrayList<Node> children, Location location, Node parent){
        this.type =type;
        if(children != null) {
            this.children = children;
        }
        else{
            this.children = new ArrayList<>();
        }

        this.location = location;
        this.parent = parent;
        status = TripStatus.WAITING;
    }

    public String getStringRepresentation(String offset){
        StringBuilder builder = new StringBuilder();

        if(type == NodeType.ORIGIN){
            builder.append(offset).append("(O) - ");
        }
        else if(type == NodeType.DESTINATION){
            builder.append(offset).append("(D) - ");
        }
        else{
            builder.append(offset).append("(R) - ");
        }

        builder.append(location.latitude).append(" ").append(location.longitude);
        builder.append(" ; ").append(slackTime);

        for(var node : children){
            builder.append("\n" + offset).append(node.getStringRepresentation(offset + "\t"));
        }

        return  builder.toString();
    }

    public Node getShallowCopy(){
        Node node = new Node(type, new ArrayList<>(), location);
        node.slackTime = slackTime;
        node.distanceFromRootToNode = distanceFromRootToNode;
        node.tripCoefficient = tripCoefficient;
        node.status = status;
        node.distanceFromOriginToNode = distanceFromOriginToNode;
        node.distanceFromOriginToNodeCurrent = distanceFromOriginToNodeCurrent;
        node.timeLimit = timeLimit;

        for(var child : children){
            var newChild = child.getShallowCopy();
            newChild.parent = node;
            node.children.add(newChild);
        }

        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Double.compare(node.timeLimit, timeLimit) == 0 &&
                Double.compare(node.tripCoefficient, tripCoefficient) == 0 &&
                Double.compare(node.distanceFromOriginToNode, distanceFromOriginToNode) == 0 &&
                Objects.equals(location, node.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, timeLimit, tripCoefficient, distanceFromOriginToNode);
    }
}
