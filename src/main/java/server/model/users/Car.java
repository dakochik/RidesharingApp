package server.model.users;

import server.model.Location;
import server.model.tree.Node;
import server.model.tree.Tree;

public class Car {
    public static final double IMPOSSIBLE_TO_HANDLE = -1;

    public Tree tree;

    public Tree updatedTree;

    public TripRequest tripRequest;

    public int currentCapacity;

    public Car(int capacity, TripRequest trip){
        tree = new Tree();
        tripRequest = trip;
        tree.handleRequest(trip);
        updatedTree = tree.getShallowCopy();
        currentCapacity = capacity;
    }

    public double tryAdd(TripRequest newTrip){
        boolean response = updatedTree.handleRequest(newTrip);

        if(!response){
            updatedTree = tree.getShallowCopy();
            return IMPOSSIBLE_TO_HANDLE;
        }
        else{
            return updatedTree.currentRoot.slackTime;
        }
    }

    public void confirmRequest(){
        updatedTree.convertIntoSolution();
        tree = updatedTree.getShallowCopy();
    }

    public void updateLocation(Location location){
        tree.updateCurrentRoot(location);
        updatedTree.updateCurrentRoot(location);
    }

    public void resetTree(){
        updatedTree = tree.getShallowCopy();
    }

    public String getUri(){
        StringBuilder builder = new StringBuilder();

        builder.append("https://www.google.ru/maps/dir/");

        Node node = tree.originalRoot;
        builder.append(String.format("%s,%s/",node.location.latitude, node.location.longitude));
        while (!node.children.isEmpty()){
            node = node.children.get(0);
            builder.append(String.format("%s,%s/",node.location.latitude, node.location.longitude));
        }

        return builder.toString();
    }
}
