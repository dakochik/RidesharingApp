package server.model.users;

import server.model.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

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
     * Макимальный допустимый коэффициент увеличения продолжитнльности поездки
     */
    public double distanceCoefficient;

    /**
     * Дата создания запроса
     */
    public LocalDateTime dateOfRequest;

    /**
     * Конструктор запроса на поездку.
     * @param origin отправная точка
     * @param destination точка прибытия
     * @param waitingM максимальное время ожидания в минутах
     * @param tripM максимальный допустимый коэффициент того, на сколько может увеличиться поездка
     * @param date время создания запроса
     */
    public TripRequest(Location origin, Location destination, double waitingM, double tripM, LocalDateTime date){
        this.origin = origin;
        this.destination = destination;
        maxWaitingMeasure = waitingM * MINUTES_TO_KM;
        distanceCoefficient = tripM;
        dateOfRequest = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TripRequest that = (TripRequest) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }
}
