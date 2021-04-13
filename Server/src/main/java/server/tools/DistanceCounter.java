package server.tools;

import org.locationtech.jts.geom.Coordinate;
import server.model.tree.Node;

public class DistanceCounter {
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
            dist = dist * 60 * 1.1515 * 1.609344 * APPROXIMATION_ERROR_COEFFICIENT;

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
}
