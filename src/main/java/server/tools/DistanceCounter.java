package server.tools;

import org.apache.lucene.util.SloppyMath;
import server.model.Location;
import server.model.tree.Node;

import static java.lang.Math.PI;

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
    public static double measureDistance(Location origin, Location destination){
        if ((origin.latitude == destination.latitude)
                && (origin.longitude == destination.longitude)) {
            return 0;
        }
        else {
            double theta = origin.longitude - destination.longitude;
            double dist = Math.sin(Math.toRadians(origin.latitude))
                    * Math.sin(Math.toRadians(destination.latitude))
                    + Math.cos(Math.toRadians(origin.latitude))
                    * Math.cos(Math.toRadians(destination.latitude))
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
