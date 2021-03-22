package server.model.users;

import server.model.Location;
import server.tools.DistanceCounter;

/**
 * Описывает запрос на поездку
 */
public class TripRequest {
    /**
     * Преобразует заданное время в потенциальное проходимое расстояние с учетом средней скорости в 30км/ч
     */
    public static final double MINUTES_TO_KM = 0.5;

    /**
     * Точка отправления
     */
    public Location origin;

    /**
     * Точка прибытия
     */
    public Location destination;

    /**
     * Расстояние в километрах, которое можно преодолеть за максимально допустимое время ожидания
     *
     */
    public double maxWaitingMeasure;

    /**
     * Расстояние в километрах, которое можно преодолеть за максимально допустимое время в поездке
     */
    public double distanceCoefficient;

    /**
     * Конструктор запроса на поездку.
     * @param origin отправная точка
     * @param destination точка прибытия
     * @param waitingM максимальное время ожидания в минутах
     * @param tripM максимальный допустимый коэффициент того, на сколько может увеличиться поездка
     */
    public TripRequest(Location origin, Location destination, double waitingM, double tripM){
        this.origin = origin;
        this.destination = destination;
        maxWaitingMeasure = waitingM * MINUTES_TO_KM;
        distanceCoefficient = tripM;
    }
}
