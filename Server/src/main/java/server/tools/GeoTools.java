package server.tools;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.io.WKBWriter;
import server.model.tree.Node;
import server.model.users.Car;
import server.model.users.TripRequest;

import java.util.ArrayList;
import java.util.List;

public class GeoTools {
    /**
     * Поправка на незнание реального маршрута передвижения.
     */
    public static final double APPROXIMATION_ERROR_COEFFICIENT = 1.5;

    /**
     * Измеряет географическое расстояние между двумя узлами дерева с поправкой на незнание реального маршрута.
     *
     * Пока что отсутсвует привязка к дорогаям.
     * @param origin Точка отправления
     * @param destination Точка прибытия
     * @return расстояние в км
     */
    public static double measureDistance(Node origin, Node destination){
        return measureDistance(origin.location, destination.location);
    }

    /**
     * Измеряет географическое расстояние между двумя географическими точками с поправкой на незнание реального маршрута.
     *
     * Пока что отсутсвует привязка к дорогаям.
     * @param origin Точка отправления
     * @param destination Точка прибытия
     * @return расстояние в км
     */
    public static double measureDistance(Coordinate origin, Coordinate destination){
        if ((origin.x == destination.x)
                && (origin.y == destination.y)) {
            return 0;
        }
        else {
            double theta = origin.y - destination.y;
            double dist = Math.sin(Math.toRadians(origin.x))
                    * Math.sin(Math.toRadians(destination.x))
                    + Math.cos(Math.toRadians(origin.x))
                    * Math.cos(Math.toRadians(destination.x))
                    * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 111.18957696 * APPROXIMATION_ERROR_COEFFICIENT; // 111.18957696 - длина дуги 1 градуса мередиана

            return dist;
        }
    }

    /**
     * Сдвиг по широте
     * @param width длинна сдвига в км
     * @return
     */
    public static double getLatitudeShift(double width){
        return width / 111.111;
    }

    /**
     * Сдвиг по долготе
     * @param length длинна сдвига в км
     * @param lat долгота точки
     * @return
     */
        public static double getLongitudeShift(double length, double lat){
        return (length/111.111)/Math.cos(lat);
    }

    /**
     * Возвращает маршрут машины (определенной поездки)
     * @param car машина
     * @return маршрут
     */
    public static List<Coordinate> getTripGraph(Car car){
        List<Coordinate> result = new ArrayList<>();

        var node = car.tree.originalRoot;
        result.add(new Coordinate(node.location.y, node.location.x)); // CARTO geom format
        while (!node.children.isEmpty()) {
            node = node.children.get(0);
            result.add(new Coordinate(node.location.y, node.location.x)); // CARTO geom format
        }

        return result;
    }

    /**
     * Возвращает строковое представление линии координат в формате WGS 84 переведенном в бинарный ("well-known binary")
     * @param line линия координат
     * @return строковое представление
     */
    public static String getGeoStringRepresentation(List<Coordinate> line){
        var wkbWriter = new WKBWriter(2, true);

        Coordinate[] coordinates = new Coordinate[line.size()];
        for(int i =0; i < coordinates.length; ++i){
            coordinates[i]= new Coordinate(line.get(i).x, line.get(i).y);
        }

        CoordinateSequence sequence =
                new PackedCoordinateSequence.Double(coordinates, 2);
        var ResultLine = new org.locationtech.jts.geom.LineString(sequence, new GeometryFactory(new PrecisionModel(),4326));

        return WKBWriter.toHex(wkbWriter.write(ResultLine));
    }

    /**
     * Возвращает строковое представление пути запроса в формате WGS 84 переведенном в бинарный ("well-known binary")
     * @param request запрос
     * @return строковое представление
     */
    public static String getGeoStringRepresentation(TripRequest request){
        ArrayList<Coordinate> line = new ArrayList<>();
        line.add(request.origin);
        line.add(request.destination);

        var wkbWriter = new WKBWriter(2, true);

        Coordinate[] coordinates = new Coordinate[line.size()];
        for(int i =0; i < coordinates.length; ++i){
            coordinates[i]= new Coordinate(line.get(i).y, line.get(i).x);
        }

        CoordinateSequence sequence =
                new PackedCoordinateSequence.Double(coordinates, 2);
        var ResultLine = new org.locationtech.jts.geom.LineString(sequence, new GeometryFactory(new PrecisionModel(),4326));

        return WKBWriter.toHex(wkbWriter.write(ResultLine));
    }
}
