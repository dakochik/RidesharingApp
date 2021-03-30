package server.tools;

import server.model.Location;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.RideSharingComputer;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;

public class RequestsHandler {
    /**
     * Возвращает пару {список машин, список запросов на поездку} таких, что все элементы списокв находятся в одной
     * географической области в одном временном окне.
     *
     * @param comp  текущий планировщик поездок, содержащий информацию о машинах и запросах
     * @param t     временная точка, относительно которой создается временное окно
     * @param windS радиус временного окна в минутах
     * @param loc   географическая точка, центр рассмматриваемой локации
     * @param r     радиус географической локации
     * @return все машины и запросы, подходящие под описанные критерии
     */
    public static AbstractMap.SimpleEntry<ArrayList<Car>, ArrayList<TripRequest>>
    getTripsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Location loc, double r) {
        AbstractMap.SimpleEntry<ArrayList<Car>, ArrayList<TripRequest>> res
                = new AbstractMap.SimpleEntry<>(new ArrayList<>(), new ArrayList<>());

        for (int i = 0; i < comp.cars.size(); ++i) {
            var car = comp.cars.get(i);
            if (car.tree.currentRoot.arrivingTime.compareTo(t.plusMinutes(windS)) < 0
                    && car.tree.currentRoot.arrivingTime.compareTo(t.minusMinutes(windS)) > 0
                    && DistanceCounter.measureDistance(car.tree.currentRoot.location, loc) < r) {
                res.getKey().add(car);
            }
        }

        for (int i = 0; i < comp.requests.size(); ++i) {
            var req = comp.requests.get(i);
            if (req.dateOfRequest.compareTo(t.plusMinutes(windS)) < 0
                    && req.dateOfRequest.compareTo(t.minusMinutes(windS)) > 0
                    && DistanceCounter.measureDistance(req.origin, loc) < r) {
                res.getValue().add(req);
            }
        }

        return res;
    }

    /**
     * Возвращает список всех машин, которые находятся в определенной
     * географической области в определенном временном окне.
     *
     * @param comp   текущий планировщик поездок, содержащий информацию о машинах и запросах
     * @param t      временная точка, относительно которой создается временное окно
     * @param windS  радиус временного окна в минутах
     * @param loc    верхняя левая точка прямоугольника, являющегося географияеской областью
     * @param width  ширина прямоугольника в км
     * @param height высота прямоугольника в км
     * @return список всех подходящих машин
     */
    public static ArrayList<Car>
    getCarsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Location loc, double width, double height) {
        ArrayList<Car> res = new ArrayList<>();

        for (int i = 0; i < comp.cars.size(); ++i) {
            var car = comp.cars.get(i);
            if (car.tree.currentRoot.arrivingTime.compareTo(t.plusMinutes(windS)) < 0
                    && car.tree.currentRoot.arrivingTime.compareTo(t.minusMinutes(windS)) > 0
                    && car.tree.currentRoot.location.latitude >= loc.latitude
                    && car.tree.currentRoot.location.latitude <= loc.latitude + DistanceCounter.getLatitudeShift(width)
                    && car.tree.currentRoot.location.longitude >= loc.longitude
                    && car.tree.currentRoot.location.longitude <= loc.longitude + DistanceCounter.getLongitudeShift(height, loc.longitude)) {
                res.add(car);
            }
        }

        return res;
    }

    /**
     * Возвращает список всех запросов на поездку, которые находятся в определенной
     * географической области в определенном временном окне.
     *
     * @param comp   текущий планировщик поездок, содержащий информацию о машинах и запросах
     * @param t      временная точка, относительно которой создается временное окно
     * @param windS  радиус временного окна в минутах
     * @param loc    верхняя левая точка прямоугольника, являющегося географияеской областью
     * @param width  ширина прямоугольника в км
     * @param height высота прямоугольника в км
     * @return список всех подходящих запросов
     */
    public static ArrayList<TripRequest>
    getRequestsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Location loc, double width, double height) {
        ArrayList<TripRequest> res = new ArrayList<>();

        for (int i = 0; i < comp.requests.size(); ++i) {
            var req = comp.requests.get(i);
            if (req.dateOfRequest.compareTo(t.plusMinutes(windS)) < 0
                    && req.dateOfRequest.compareTo(t.minusMinutes(windS)) > 0
                    && req.origin.latitude >= loc.latitude
                    && req.origin.latitude <= loc.latitude + DistanceCounter.getLatitudeShift(width)
                    && req.origin.longitude >= loc.longitude
                    && req.origin.longitude <= loc.longitude + DistanceCounter.getLongitudeShift(height, loc.longitude)) {
                res.add(req);
            }
        }

        return res;
    }
}
