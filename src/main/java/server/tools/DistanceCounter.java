package server.tools;

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
        if ((origin.location.latitude == destination.location.latitude)
                && (origin.location.longitude == destination.location.longitude)) {
            return 0;
        }
        else {
            double theta = origin.location.longitude - destination.location.longitude;
            double dist = Math.sin(Math.toRadians(origin.location.latitude))
                    * Math.sin(Math.toRadians(destination.location.latitude))
                    + Math.cos(Math.toRadians(origin.location.latitude))
                    * Math.cos(Math.toRadians(destination.location.latitude))
                    * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515 * 1.609344 * APPROXIMATION_ERROR_COEFFICIENT;

            return dist;
        }
    }
}
