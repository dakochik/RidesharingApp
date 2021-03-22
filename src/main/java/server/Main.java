package server;

import server.model.Location;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.RideSharingComputer;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TripRequest tr1 = new TripRequest(new Location(55.690351, 37.860461)
                , new Location(55.680171, 37.850424), 10, 0.7);

        TripRequest tr2 = new TripRequest(new Location(55.686766, 37.85373)
                , new Location(55.678346, 37.855103), 15, 0.6);

        TripRequest tr3 = new TripRequest(new Location(55.685338, 37.857034)
                , new Location(55.681637, 37.852314), 15, 0.6);

        TripRequest tr4 = new TripRequest(new Location(55.681380, 37.861177)
                , new Location(55.691842, 37.853832), 10, 0.9); // 0.9 -> 0.8 => no trip

        TripRequest tr5 = new TripRequest(new Location(55.684995, 37.859206)
                , new Location(55.688651, 37.853079), 15, 0.9);

        RideSharingComputer comp = new RideSharingComputer();
        comp.addCar(new Car(3, tr1));
        comp.addCar(new Car(3, tr4));

        comp.addAllTask(new ArrayList<>(){{add(tr2);add(tr3);add(tr5);}});

        comp.compute();

        System.out.println(comp.cars.get(0).getUri());
        System.out.println(comp.cars.get(1).getUri());
    }
}
