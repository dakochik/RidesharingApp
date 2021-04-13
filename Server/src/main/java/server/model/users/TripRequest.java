package server.model.users;

import org.locationtech.jts.geom.Coordinate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Описывает запрос на поездку
 */
public class TripRequest {
    /**
     * Время ожидания (в минутах) в случаях, когда входные данные не имеют его
     */
    public static final double DEFAULT_WAITING_TIME = 20;

    /**
     * Коэффициент максимального допустимого увеличения поездки (1 +  коеффициент) в случаях, когда входные данные не имеют его
     */
    public static final double DEFAULT_TRIP_COEFFICIENT = 0.8;

    /**
     * Преобразует заданное время в потенциальное проходимое расстояние с учетом средней скорости в 30км/ч
     */
    public static final double MINUTES_TO_KM = 0.5;

    /**
     * Точка отправления
     */
    public Coordinate origin;

    /**
     * Точка прибытия
     */
    public Coordinate destination;

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
     * ID поездки
     */
    public final String tripId;

    /**
     * ID машины, которая планирует подобрать клиента
     */
    public Optional<String> carId = Optional.empty();

    /**
     * Конструктор запроса на поездку.
     * @param origin отправная точка
     * @param destination точка прибытия
     * @param waitingM максимальное время ожидания в минутах
     * @param tripM максимальный допустимый коэффициент того, на сколько может увеличиться поездка
     * @param date время создания запроса
     * @param id ID поездки
     */
    public TripRequest(Coordinate origin, Coordinate destination, double waitingM, double tripM, LocalDateTime date, String id){
        this.origin = origin;
        this.destination = destination;
        maxWaitingMeasure = waitingM * MINUTES_TO_KM;
        distanceCoefficient = tripM;
        dateOfRequest = date;
        tripId = id;
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
