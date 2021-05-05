package server.tools;

import org.locationtech.jts.geom.Coordinate;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.RideSharingComputer;

import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

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
    public static AbstractMap.SimpleEntry<List<Car>, List<TripRequest>>
    getTripsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Coordinate loc, double r) {
        AbstractMap.SimpleEntry<List<Car>, List<TripRequest>> res
                = new AbstractMap.SimpleEntry<>(new ArrayList<>(), new ArrayList<>());

        for (int i = 0; i < comp.cars.size(); ++i) {
            var car = comp.cars.get(i);
            if (car.tree.currentRoot.arrivingTime.compareTo(t.plusMinutes(windS)) < 0
                    && car.tree.currentRoot.arrivingTime.compareTo(t.minusMinutes(windS)) > 0
                    && GeoTools.measureDistance(car.tree.currentRoot.location, loc) < r) {
                res.getKey().add(car);
            }
        }

        for (int i = 0; i < comp.requests.size(); ++i) {
            var req = comp.requests.get(i);
            if (req.dateOfRequest.compareTo(t.plusMinutes(windS)) < 0
                    && req.dateOfRequest.compareTo(t.minusMinutes(windS)) > 0
                    && GeoTools.measureDistance(req.origin, loc) < r) {
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
    public static List<Car>
    getCarsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Coordinate loc, double width, double height) {
        List<Car> res = new ArrayList<>();

        for (int i = 0; i < comp.cars.size(); ++i) {
            var car = comp.cars.get(i);
            if (car.tree.currentRoot.arrivingTime.compareTo(t.plusMinutes(windS)) < 0
                    && car.tree.currentRoot.arrivingTime.compareTo(t.minusMinutes(windS)) > 0
                    && car.tree.currentRoot.location.x >= loc.x
                    && car.tree.currentRoot.location.x <= loc.x + GeoTools.getLatitudeShift(width)
                    && car.tree.currentRoot.location.y >= loc.y
                    && car.tree.currentRoot.location.y <= loc.y + GeoTools.getLongitudeShift(height, loc.y)) {
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
    public static List<TripRequest>
    getRequestsByRegion(RideSharingComputer comp, LocalDateTime t, Long windS, Coordinate loc, double width, double height) {
        List<TripRequest> res = new ArrayList<>();

        for (int i = 0; i < comp.requests.size(); ++i) {
            var req = comp.requests.get(i);
            if (req.dateOfRequest.compareTo(t.plusMinutes(windS)) < 0
                    && req.dateOfRequest.compareTo(t.minusMinutes(windS)) > 0
                    && req.origin.x >= loc.x
                    && req.origin.x <= loc.x + GeoTools.getLatitudeShift(width)
                    && req.origin.y >= loc.y
                    && req.origin.y <= loc.y + GeoTools.getLongitudeShift(height, loc.y)) {
                res.add(req);
            }
        }

        return res;
    }
}
