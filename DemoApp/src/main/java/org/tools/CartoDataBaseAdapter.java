package org.tools;

import com.roxstudio.utils.CUrl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.locationtech.jts.geom.Coordinate;
import server.TableHeaders;
import server.model.users.Car;
import server.model.users.TripRequest;
import server.service.RideSharingComputer;
import server.tools.GeoTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CartoDataBaseAdapter {
    private static CUrl.Resolver<Document> htmlResolver = new CUrl.Resolver<Document>() {
        @SuppressWarnings("unchecked")
        @Override
        public Document resolve(int httpCode, byte[] responseBody) throws Throwable {
            String html = new String(responseBody, "UTF-8");
            return Jsoup.parse(html);
        }
    };

    private enum RequestConstants {
        USERNAME("dakochik"),
        REQUEST_HEADER("Content-Type: application/json"),
        REQUESTS_TABLE_NAME("chicago_5000_filtered"),
        UNHANDLED_REQUESTS_TABLE_NAME("chicago_5000_requests"),
        TRIPS_TABLE_NAME("chicago_5000_cars"),
        API_KEY("1dfba9e5fb93bade9610d6c49e070d65f5760ddb"),
        ADDRESS(String.format("https://%s.carto.com/api/v2/sql?api_key=", USERNAME.val));


        private final String val;

        RequestConstants(String val) {
            this.val = val;
        }
    }

    public List<TripRequest> readRequests() throws IllegalAccessException {
        List<TripRequest> result = new ArrayList<>();

        String data = String.format("{\"q\":\"%s\"}", String.format("SELECT * FROM %s ",
                RequestConstants.REQUESTS_TABLE_NAME.val));
        CUrl curl = new CUrl(RequestConstants.ADDRESS.val);
        curl.data(data);
        curl.header(RequestConstants.REQUEST_HEADER.val);

        Document doc = Jsoup.parseBodyFragment(curl.exec(htmlResolver, null).outerHtml());
        System.out.printf("Request code: %s%n\n", curl.getHttpCode()); // Change to logging

        if (curl.getHttpCode() != 200) {
            throw new IllegalAccessException("Impossible to access remote data base\nRequest code: " + curl.getHttpCode());
        }

        JSONObject obj = new JSONObject(doc.body().text());
        JSONArray arr = (JSONArray) obj.get("rows");
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;

        for (var request : arr) {
            JSONObject requestObj = new JSONObject(request.toString());
            String reqId = requestObj.get(TableHeaders.TRIP_ID.val).toString();
            String time = requestObj.get(TableHeaders.TRIP_START_TIMESTAMP.val).toString();
            String originLat = requestObj.get(TableHeaders.PICKUP_CENTROID_LATITUDE.val).toString();
            String originLong = requestObj.get(TableHeaders.PICKUP_CENTROID_LONGITUDE.val).toString();
            String destLat = requestObj.get(TableHeaders.DROPOFF_CENTROID_LATITUDE.val).toString();
            String destLong = requestObj.get(TableHeaders.DROPOFF_CENTROID_LONGITUDE.val).toString();
            result.add(new TripRequest(new Coordinate(Double.parseDouble(originLat), Double.parseDouble(originLong)),
                    new Coordinate(Double.parseDouble(destLat), Double.parseDouble(destLong)),
                    TripRequest.DEFAULT_WAITING_TIME, TripRequest.DEFAULT_TRIP_COEFFICIENT,
                    LocalDateTime.parse(time, format), reqId));
        }

        return result;
    }

    public List<TripRequest> readNotHandledRequests() throws IllegalAccessException {
        List<TripRequest> result = new ArrayList<>();

        String data = String.format("{\"q\":\"%s\"}", String.format("SELECT * FROM %s WHERE (%s is null or %s = '')",
                RequestConstants.UNHANDLED_REQUESTS_TABLE_NAME.val, TableHeaders.TRIP_ID, TableHeaders.TRIP_ID));
        CUrl curl = new CUrl(RequestConstants.ADDRESS.val);
        curl.data(data);
        curl.header(RequestConstants.REQUEST_HEADER.val);

        Document doc = Jsoup.parseBodyFragment(curl.exec(htmlResolver, null).outerHtml());
        System.out.printf("Request code: %s%n\n", curl.getHttpCode()); // Change to logging

        if (curl.getHttpCode() != 200) {
            throw new IllegalAccessException("Impossible to access remote data base");
        }

        JSONObject obj = new JSONObject(doc.body().text());
        JSONArray arr = (JSONArray) obj.get("rows");
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;

        for (var request : arr) {
            JSONObject requestObj = new JSONObject(request.toString());
            String reqId = requestObj.get(TableHeaders.REQUEST_ID.val).toString();
            String time = requestObj.get(TableHeaders.TRIP_START_TIMESTAMP.val).toString();
            String originLat = requestObj.get(TableHeaders.PICKUP_CENTROID_LATITUDE.val).toString();
            String originLong = requestObj.get(TableHeaders.PICKUP_CENTROID_LONGITUDE.val).toString();
            String destLat = requestObj.get(TableHeaders.DROPOFF_CENTROID_LATITUDE.val).toString();
            String destLong = requestObj.get(TableHeaders.DROPOFF_CENTROID_LONGITUDE.val).toString();
            double waiting = Double.parseDouble(requestObj.get(TableHeaders.MAX_WAITING_TIME.val).toString());
            double coefficient = Double.parseDouble(requestObj.get(TableHeaders.MAX_DISTANCE_COEFFICIENT.val).toString());

            result.add(new TripRequest(new Coordinate(Double.parseDouble(originLat), Double.parseDouble(originLong)),
                    new Coordinate(Double.parseDouble(destLat), Double.parseDouble(destLong)),
                    waiting, coefficient,
                    LocalDateTime.parse(time, format), reqId));
        }

        return result;
    }

    public void updateRequestsByTripId(List<TripRequest> trips) throws IllegalAccessException {
        var validForUpdating = trips.stream()
                .filter(it -> it.carId.isPresent()).collect(Collectors.toList());

        StringBuilder dataFilterValues;
        StringBuilder dataRules;

        for (int i = 0; i < validForUpdating.size(); ++i) {
            dataRules = new StringBuilder();
            dataFilterValues = new StringBuilder();
            int counter = 0;

            dataRules.append("WHEN '").append(validForUpdating.get(i).tripId)
                    .append("' THEN '").append(validForUpdating.get(i).carId.get()).append("' ");
            dataFilterValues.append("'").append(validForUpdating.get(i).tripId).append("'");
            ++counter;
            ++i;

            while (counter < 500 && i < validForUpdating.size()) { // Creates 501 size buckets or less
                dataRules.append("WHEN '").append(validForUpdating.get(i).tripId)
                        .append("' THEN '").append(validForUpdating.get(i).carId.get()).append("' ");
                dataFilterValues.append(", '").append(validForUpdating.get(i).tripId).append("'");
                ++counter;
                ++i;
            }

            String data = String.format("{\"q\":\"%s\"}",
                    String.format("UPDATE %s SET %s = CASE %s %s ELSE %s END WHERE %s IN(%s)",
                            RequestConstants.UNHANDLED_REQUESTS_TABLE_NAME.val,
                            TableHeaders.TRIP_ID.val,
                            TableHeaders.REQUEST_ID.val,
                            dataRules.toString(),
                            TableHeaders.TRIP_ID.val,
                            TableHeaders.REQUEST_ID.val,
                            dataFilterValues.toString()));
            CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
            curl.data(data);
            curl.header(RequestConstants.REQUEST_HEADER.val);
            curl.exec(htmlResolver, null);

            System.out.printf("Update bucket up to %s finished with code: %s%n\n",
                    i, curl.getHttpCode());  // Change to logging

            if (curl.getHttpCode() != 200) {
                throw new IllegalAccessException("Impossible to access remote data base and update requests\nRequest code: " + curl.getHttpCode());
            }
        }
    }

    public void updateTripByTripId(List<Car> cars) throws IllegalAccessException {
        StringBuilder dataFilterValues;
        StringBuilder dataRules;

        for (int i = 0; i < cars.size(); ++i) {
            dataRules = new StringBuilder();
            dataFilterValues = new StringBuilder();
            int counter = 0;

            var geoRepresentation = GeoTools.getGeoStringRepresentation(GeoTools.getTripGraph(cars.get(i)));
            dataRules.append("WHEN '").append(cars.get(i).tripRequest.tripId)
                    .append("' THEN '").append(geoRepresentation).append("' ");
            dataFilterValues.append("'").append(cars.get(i).tripRequest.tripId).append("'");
            ++counter;
            ++i;

            while (counter < 500 && i < cars.size()) { // Creates 501 size buckets or less
                geoRepresentation = GeoTools.getGeoStringRepresentation(GeoTools.getTripGraph(cars.get(i)));
                dataRules.append("WHEN '").append(cars.get(i).tripRequest.tripId)
                        .append("' THEN '").append(geoRepresentation).append("' ");
                dataFilterValues.append(", '").append(cars.get(i).tripRequest.tripId).append("'");
                ++counter;
                ++i;
            }

            String data = String.format("{\"q\":\"%s\"}",
                    String.format("UPDATE %s SET %s = CASE %s %s ELSE %s END WHERE %s IN(%s)",
                            RequestConstants.TRIPS_TABLE_NAME.val,
                            TableHeaders.GEOM.val,
                            TableHeaders.TRIP_ID.val,
                            dataRules.toString(),
                            TableHeaders.GEOM.val,
                            TableHeaders.TRIP_ID.val,
                            dataFilterValues.toString()));
            CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
            curl.data(data);
            curl.header(RequestConstants.REQUEST_HEADER.val);
            curl.exec(htmlResolver, null);

            System.out.printf("Update bucket up to %s finished with code: %s%n\n",
                    i, curl.getHttpCode());  // Change to logging

            if (curl.getHttpCode() != 200) {
                throw new IllegalAccessException("Impossible to access remote data base and update trips\nRequest code: " + curl.getHttpCode());
            }
        }
    }

    public void clearRequestsTable() throws IllegalAccessException {
        String data = String.format("{\"q\":\"%s\"}", String.format("TRUNCATE TABLE %s ",
                RequestConstants.UNHANDLED_REQUESTS_TABLE_NAME.val));
        CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
        curl.data(data);
        curl.header(RequestConstants.REQUEST_HEADER.val);

        Document doc = Jsoup.parseBodyFragment(curl.exec(htmlResolver, null).outerHtml());
        System.out.printf("Request code: %s%n\n", curl.getHttpCode()); // Change to logging

        if (curl.getHttpCode() != 200) {
            throw new IllegalAccessException("Impossible to access remote data base and clear requests table\nRequest code: " + curl.getHttpCode());
        }
    }

    public void clearTripsTable() throws IllegalAccessException {
        String data = String.format("{\"q\":\"%s\"}", String.format("TRUNCATE TABLE %s ",
                RequestConstants.TRIPS_TABLE_NAME.val));
        CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
        curl.data(data);
        curl.header(RequestConstants.REQUEST_HEADER.val);

        Document doc = Jsoup.parseBodyFragment(curl.exec(htmlResolver, null).outerHtml());
        System.out.printf("Request code: %s%n\n", curl.getHttpCode()); // Change to logging

        if (curl.getHttpCode() != 200) {
            throw new IllegalAccessException("Impossible to access remote data base and clear trips table\nRequest code: " + curl.getHttpCode());
        }
    }

    public void pushRequests(List<TripRequest> trips) throws IllegalAccessException {
        StringBuilder dataRules;

        String columns = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                TableHeaders.GEOM.val, TableHeaders.REQUEST_ID.val, TableHeaders.TRIP_ID.val,
                TableHeaders.TRIP_START_TIMESTAMP.val, TableHeaders.PICKUP_CENTROID_LATITUDE.val,
                TableHeaders.PICKUP_CENTROID_LONGITUDE.val, TableHeaders.PICKUP_CENTROID_LOCATION.val,
                TableHeaders.DROPOFF_CENTROID_LATITUDE.val, TableHeaders.DROPOFF_CENTROID_LONGITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LOCATION.val, TableHeaders.MAX_WAITING_TIME.val,
                TableHeaders.MAX_DISTANCE_COEFFICIENT.val);

        for (int i = 0; i < trips.size(); ++i) {
            dataRules = new StringBuilder();
            int counter = 0;

            var trip = trips.get(i);
            var geoRepresentation = GeoTools.getGeoStringRepresentation(trip);

            dataRules.append("('").append(geoRepresentation).append("','").append(trip.tripId).append("','")
                    .append(trip.carId.orElse("")).append("','").append(trip.dateOfRequest).append("',")
                    .append(trip.origin.x).append(",").append(trip.origin.y).append(",'")
                    .append(String.format("POINT (%s %s)", trip.origin.y, trip.origin.x))
                    .append("',").append(trip.destination.x).append(",")
                    .append(trip.destination.y).append(",'")
                    .append(String.format("POINT (%s %s)", trip.destination.y, trip.destination.x)).append("',")
                    .append(trip.maxWaitingMeasure / TripRequest.MINUTES_TO_KM).append(",")
                    .append(trip.distanceCoefficient).append(")");
            ++counter;
            ++i;

            while (counter < 500 && i < trips.size()) { // Creates 501 size buckets or less
                trip = trips.get(i);
                geoRepresentation = GeoTools.getGeoStringRepresentation(trip);
                dataRules.append(",('").append(geoRepresentation).append("','").append(trip.tripId).append("','")
                        .append(trip.carId.orElse("")).append("','").append(trip.dateOfRequest).append("',")
                        .append(trip.origin.x).append(",").append(trip.origin.y).append(",'")
                        .append(String.format("POINT (%s %s)", trip.origin.y, trip.origin.x))
                        .append("',").append(trip.destination.x).append(",")
                        .append(trip.destination.y).append(",'")
                        .append(String.format("POINT (%s %s)", trip.destination.y, trip.destination.x)).append("',")
                        .append(trip.maxWaitingMeasure / TripRequest.MINUTES_TO_KM).append(",")
                        .append(trip.distanceCoefficient).append(")");
                ++counter;
                ++i;
            }

            String data = String.format("{\"q\":\"%s\"}",
                    String.format("INSERT INTO %s (%s) VALUES %s",
                            RequestConstants.UNHANDLED_REQUESTS_TABLE_NAME.val,
                            columns,
                            dataRules.toString()));
            CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
            curl.data(data);
            curl.header(RequestConstants.REQUEST_HEADER.val);

            curl.exec(htmlResolver, null);
            System.out.printf("Pushed bucket up to %s of requests finished with code: %s%n\n",
                    i, curl.getHttpCode());  // Change to logging

            if (curl.getHttpCode() != 200) {
                throw new IllegalAccessException("Impossible to access remote data base and push requests\nRequest code: " + curl.getHttpCode());
            }
        }
    }

    public void pushTrips(List<Car> cars) throws IllegalAccessException {
        StringBuilder dataRules;

        String columns = String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                TableHeaders.GEOM.val, TableHeaders.TRIP_ID.val, TableHeaders.TRIP_START_TIMESTAMP.val,
                TableHeaders.PICKUP_CENTROID_LATITUDE.val, TableHeaders.PICKUP_CENTROID_LONGITUDE.val,
                TableHeaders.PICKUP_CENTROID_LOCATION.val, TableHeaders.DROPOFF_CENTROID_LATITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LONGITUDE.val, TableHeaders.DROPOFF_CENTROID_LOCATION.val,
                TableHeaders.MAX_WAITING_TIME.val, TableHeaders.MAX_DISTANCE_COEFFICIENT.val);

        for (int i = 0; i < cars.size(); ++i) {
            dataRules = new StringBuilder();
            int counter = 0;

            var trip = cars.get(i);
            var geoRepresentation = GeoTools.getGeoStringRepresentation(GeoTools.getTripGraph(trip));
            dataRules.append("('").append(geoRepresentation).append("','").append(trip.tripRequest.tripId).append("','")
                    .append(trip.tripRequest.dateOfRequest).append("',").append(trip.tripRequest.origin.x).append(",")
                    .append(trip.tripRequest.origin.y).append(",'")
                    .append(String.format("POINT (%s %s)", trip.tripRequest.origin.y, trip.tripRequest.origin.x))
                    .append("',")
                    .append(trip.tripRequest.destination.x).append(",").append(trip.tripRequest.destination.y)
                    .append(",'")
                    .append(String.format("POINT (%s %s)", trip.tripRequest.destination.y, trip.tripRequest.destination.x))
                    .append("',")
                    .append(trip.tripRequest.maxWaitingMeasure / TripRequest.MINUTES_TO_KM).append(",")
                    .append(trip.tripRequest.distanceCoefficient).append(")");
            ++counter;
            ++i;

            while (counter < 500 && i < cars.size()) { // Creates 501 size buckets or less
                trip = cars.get(i);
                geoRepresentation = GeoTools.getGeoStringRepresentation(GeoTools.getTripGraph(trip));
                dataRules.append(",('").append(geoRepresentation).append("','").append(trip.tripRequest.tripId).append("','")
                        .append(trip.tripRequest.dateOfRequest).append("',").append(trip.tripRequest.origin.x).append(",")
                        .append(trip.tripRequest.origin.y).append(",'")
                        .append(String.format("POINT (%s %s)", trip.tripRequest.origin.y, trip.tripRequest.origin.x))
                        .append("',")
                        .append(trip.tripRequest.destination.x).append(",").append(trip.tripRequest.destination.y)
                        .append(",'")
                        .append(String.format("POINT (%s %s)", trip.tripRequest.destination.y, trip.tripRequest.destination.x))
                        .append("',")
                        .append(trip.tripRequest.maxWaitingMeasure / TripRequest.MINUTES_TO_KM).append(",")
                        .append(trip.tripRequest.distanceCoefficient).append(")");
                ++counter;
                ++i;
            }

            String data = String.format("{\"q\":\"%s\"}",
                    String.format("INSERT INTO %s (%s) VALUES %s",
                            RequestConstants.TRIPS_TABLE_NAME.val,
                            columns,
                            dataRules.toString()));
            CUrl curl = new CUrl(String.format("%s%s", RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
            curl.data(data);
            curl.header(RequestConstants.REQUEST_HEADER.val);
            curl.exec(htmlResolver, null);

            System.out.printf("Pushed bucket up to %s of cars finished with code: %s%n\n",
                    i, curl.getHttpCode());  // Change to logging

            if (curl.getHttpCode() != 200) {
                throw new IllegalAccessException("Impossible to access remote data base and push trips\nRequest code: " + curl.getHttpCode());
            }
        }
    }
}
