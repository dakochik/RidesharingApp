package server;

import manager.csv_read_writer.LocalDataReadWriter;
import server.model.Location;
import server.model.tree.Node;
import server.model.tree.NodeType;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.RideSharingComputer;
import server.tools.DistanceCounter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ////Простой пример 1
//        TripRequest tr1 = new TripRequest(new Location(55.690351, 37.860461)
//                , new Location(55.680171, 37.850424), 10, 0.7, LocalDate.now());
//
//        TripRequest tr2 = new TripRequest(new Location(55.686766, 37.85373)
//                , new Location(55.678346, 37.855103), 15, 0.8, LocalDate.now());
//
//        TripRequest tr3 = new TripRequest(new Location(55.685338, 37.857034)
//                , new Location(55.681637, 37.852314), 15, 0.8, LocalDate.now());
//
//        TripRequest tr4 = new TripRequest(new Location(55.681380, 37.861177)
//                , new Location(55.691842, 37.853832), 10, 0.8, LocalDate.now()); // 0.9 -> 0.8 => no trip
//
//        TripRequest tr5 = new TripRequest(new Location(55.684995, 37.859206)
//                , new Location(55.688651, 37.853079), 15, 0.9, LocalDate.now());
//
//        RideSharingComputer comp = new RideSharingComputer();
//        comp.addCar(new Car(3, tr1));
//        comp.addCar(new Car(3, tr4));
//
//        comp.addAllTask(new ArrayList<>(){{add(tr2);add(tr3);add(tr5);}});
//
//        comp.compute();
//
//        System.out.println(comp.cars.get(0).tree.getStringRepresentation());
//        System.out.println(comp.cars.get(0).getUri());
//        System.out.println(comp.cars.get(1).tree.getStringRepresentation());
//        System.out.println(comp.cars.get(1).getUri());

        //Пример 2 с 5000 запрсами из г.Чикаго
        RideSharingComputer comp = new RideSharingComputer();
        try {
            var res = LocalDataReadWriter.getDataSec(5000);
            for(int i =0; i < res.size()/3; ++i){
                comp.addCar(new Car(3,res.get(i)));
            }
            for(int i =res.size()/3 + 1; i < res.size(); ++i){
                comp.addTask(res.get(i));
            }

            System.out.println(comp.requests.size());
            comp.compute();

            System.out.println(comp.confirmedReq);

            // Вывод строкового представления деревьев решений
            for (var a: comp.cars) {
                System.out.println(a.tree.getStringRepresentation());
            }

            // Пример движения одной машины и проверка результата
            comp.cars.get(comp.cars.size() - 4).updateLocation(comp.cars.get(comp.cars.size() - 4).tree.originalRoot.arrivingTime.plusMinutes(1));
            System.out.println(comp.cars.get(comp.cars.size() - 4).tree.getStringRepresentation());
        }
        catch (Exception e){
            e.printStackTrace();
        }

        ////Пример группировки по времени запроса
//        try {
//            var res = LocalDataReadWriter.getGroupedByTimeData(LocalDataReadWriter.getDataSec(5000));
//            for (var a : res.keySet()) {
//                System.out.printf("%s - %s\n", a, res.get(a).size());
//            }
//        }
//        catch (IOException e){
//
//        }
    }
}
