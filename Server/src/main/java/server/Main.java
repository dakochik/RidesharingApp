package server;

import manager.csv_read_writer.LocalDataReadWriter;
import server.tools.CSVParser;
import server.model.users.Car;
import server.service.RideSharingComputer;

import java.io.IOException;

import static server.tools.CSVParser.fetchMainDataAndSetExactDate;

public class Main {
    public static void main(String[] args) throws IOException {
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

//        //Пример 2 с 5000 запрсами из г.Чикаго
//        RideSharingComputer comp = new RideSharingComputer();
//        try {
//            var res = LocalDataReadWriter.getDataSec(5000);
//            for(int i =0; i < 2*res.size()/3; ++i){
//                comp.addTask(res.get(i));
//            }
//            for(int i =2*res.size()/3 + 1; i < res.size(); ++i){
//                comp.addCar(new Car(3,res.get(i)));
//            }
//
//            System.out.println(comp.requests.size());
//            comp.compute();
//
//            System.out.println(comp.confirmedReq);
//
//            CSVParser.carsWriter(comp);
//            CSVParser.requestsWriter(comp);
//
//             //Вывод строкового представления деревьев решений
//            for (var a: comp.cars) {
//                System.out.println(a.tree.getStringRepresentation());
//            }
////
////            // Пример движения одной машины и проверка результата
////            comp.cars.get(comp.cars.size() - 4).updateLocation(comp.cars.get(comp.cars.size() - 4).tree.originalRoot.arrivingTime.plusMinutes(1));
////            System.out.println(comp.cars.get(comp.cars.size() - 4).tree.getStringRepresentation());
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

//        // Обоснование id =37
//        //-87.64707851,41.94257718],[-87.65641153,41.93623718],[-87.65177051,41.94269184
//
//        var l1 = new Location(-87.65302179,41.95815488);
//        var l2 = new Location(-87.64707851,41.94257718);
//        var l3 = new Location(-87.65641153,41.93623718);
//        var l4 = new Location(-87.65177051,41.94269184);
//        var l5 = new Location(-87.62614559,41.90278805);
//
//        double d1 = GeoTools.measureDistance(l1, l2) +
//                GeoTools.measureDistance(l2,l3) + GeoTools.measureDistance(l3, l4) +
//                GeoTools.measureDistance(l4, l5);
//
//        double d2 = GeoTools.measureDistance(l1, l4) +
//                GeoTools.measureDistance(l4,l2) + GeoTools.measureDistance(l2, l3) +
//                GeoTools.measureDistance(l3, l5);
//
//        double d3 = GeoTools.measureDistance(l1, l2) +
//                GeoTools.measureDistance(l2,l4) + GeoTools.measureDistance(l4, l3) +
//                GeoTools.measureDistance(l3, l5);
//
//        System.out.println(d1);
//        System.out.println(d2);
//        System.out.println(d3);

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
        //fetchMainDataAndSetExactDate();
    }
}
