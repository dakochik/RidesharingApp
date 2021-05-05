package server.tools;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.jts.io.WKBWriter;
import server.TableHeaders;
import server.model.users.Car;
import server.model.users.TripRequest;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CSVParser {
    /**
     * Вытаскивает необходимые данные из файла с сайта
     * https://data.cityofchicago.org/Transportation/Transportation-Network-Providers-Trips/m6dm-c72p/data
     *
     * @throws IOException если возникли проблемы с чтением/записью файлов
     */
    public static void fetchMainData(String inputPath, String outputPath) throws IOException {
        String fields[] = {TableHeaders.TRIP_ID.val, TableHeaders.TRIP_START_TIMESTAMP.val,
                TableHeaders.PICKUP_CENTROID_LATITUDE.val, TableHeaders.PICKUP_CENTROID_LONGITUDE.val,
                TableHeaders.PICKUP_CENTROID_LOCATION.val, TableHeaders.DROPOFF_CENTROID_LATITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LONGITUDE.val, TableHeaders.DROPOFF_CENTROID_LOCATION.val};
        int poss[] = new int[fields.length];
        int columnsSize;
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            var header = Arrays.stream(reader.readLine().replace("\"", "").split(",")).collect(Collectors.toList());
            columnsSize = header.size();
            poss[0] = 0;
            builder.append(fields[0]);
            for (int i = 1; i < fields.length; ++i) {
                poss[i] = header.indexOf(fields[i]);
                builder.append(",").append(fields[i]);
            }
            builder.append(",the_geom");

            boolean flag = true;
            StringBuilder newStr = new StringBuilder();
            var newLine = reader.readLine();
            var wkbWriter = new WKBWriter(2);
            while (newLine != null) {
                var arr = newLine.replace("\"", "").split(",");
                if (arr.length == columnsSize) {
                    newStr.append(arr[poss[0]]);
                    for (int i = 1; i < poss.length && flag; ++i) {
                        newStr.append(",").append(arr[poss[i]]);
                        if (arr[poss[i]].isEmpty()) {
                            flag = false;
                            break;
                        }
                    }


                    if (flag) {
                        Coordinate[] coordinate = new Coordinate[2];
                        coordinate[0] = new Coordinate(Double.parseDouble(arr[poss[3]]), Double.parseDouble(arr[poss[2]]));
                        coordinate[1] = new Coordinate(Double.parseDouble(arr[poss[6]]), Double.parseDouble(arr[poss[5]]));
                        CoordinateSequence sequence = new PackedCoordinateSequence.Double(coordinate, 2);
                        var line = new org.locationtech.jts.geom.LineString(sequence, new GeometryFactory());
                        newStr.append(",").append(WKBWriter.toHex(wkbWriter.write(line)));

                        builder.append("\n").append(newStr);
                        newStr.delete(0, newStr.length());
                    } else {
                        newStr.delete(0, newStr.length());
                        flag = true;
                    }
                }
                newLine = reader.readLine();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(builder.toString());
            writer.flush();
        }
    }

    public static void fetchMainDataAndSetExactDate(String inputPath, String outputPath) throws IOException {
        String fields[] = {TableHeaders.TRIP_ID.val, TableHeaders.TRIP_START_TIMESTAMP.val,
                TableHeaders.PICKUP_CENTROID_LATITUDE.val, TableHeaders.PICKUP_CENTROID_LONGITUDE.val,
                TableHeaders.PICKUP_CENTROID_LOCATION.val, TableHeaders.DROPOFF_CENTROID_LATITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LONGITUDE.val, TableHeaders.DROPOFF_CENTROID_LOCATION.val};
        int poss[] = new int[fields.length];
        int columnsSize;
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            var header = Arrays.stream(reader.readLine().replace("\"", "").split(",")).collect(Collectors.toList());
            columnsSize = header.size();
            poss[0] = 0;
            builder.append(fields[0]);
            for (int i = 1; i < fields.length; ++i) {
                poss[i] = header.indexOf(fields[i]);
                builder.append(",").append(fields[i]);
            }
            builder.append(",the_geom");

            boolean flag = true;
            StringBuilder newStr = new StringBuilder();
            var newLine = reader.readLine();
            var wkbWriter = new WKBWriter(2);
            while (newLine != null) {
                var arr = newLine.replace("\"", "").split(",");
                if (arr.length == columnsSize) {
                    newStr.append(arr[poss[0]]);
                    newStr.append(",").append("2021-01-31T22:45:00.000");
                    for (int i = 2; i < poss.length && flag; ++i) {
                        newStr.append(",").append(arr[poss[i]]);
                        if (arr[poss[i]].isEmpty()) {
                            flag = false;
                            break;
                        }
                    }


                    if (flag) {
                        Coordinate[] coordinate = new Coordinate[2];
                        coordinate[0] = new Coordinate(Double.parseDouble(arr[poss[3]]), Double.parseDouble(arr[poss[2]]));
                        coordinate[1] = new Coordinate(Double.parseDouble(arr[poss[6]]), Double.parseDouble(arr[poss[5]]));
                        CoordinateSequence sequence = new PackedCoordinateSequence.Double(coordinate, 2);
                        var line = new org.locationtech.jts.geom.LineString(sequence, new GeometryFactory());
                        newStr.append(",").append(WKBWriter.toHex(wkbWriter.write(line)));

                        builder.append("\n").append(newStr);
                        newStr.delete(0, newStr.length());
                    } else {
                        newStr.delete(0, newStr.length());
                        flag = true;
                    }
                }
                newLine = reader.readLine();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write(builder.toString());
            writer.flush();
        }
    }

    public static List<TripRequest> readFilteredData(String path) throws IOException{
        List<TripRequest> result = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))){
            reader.readLine();

            var newLine = reader.readLine();
            while (newLine !=null){
                var arr = newLine.split(",");
                result.add(new TripRequest(new Coordinate(Double.parseDouble(arr[2]), Double.parseDouble(arr[3])),
                        new Coordinate(Double.parseDouble(arr[5]), Double.parseDouble(arr[6])), TripRequest.DEFAULT_WAITING_TIME,
                        TripRequest.DEFAULT_TRIP_COEFFICIENT, LocalDateTime.parse(arr[1]), arr[0]));
                newLine = reader.readLine();
            }
        }catch (NumberFormatException| DateTimeParseException| IndexOutOfBoundsException e){
            throw new IOException("Incorrect input data:\n"+e.getMessage());
        }

        return result;
    }

    public static void carsWriter(List<Car> cars, String path) throws IOException {
        String fields[] = {TableHeaders.TRIP_ID.val, TableHeaders.GEOM.val, TableHeaders.TRIP_START_TIMESTAMP.val,
                TableHeaders.PICKUP_CENTROID_LATITUDE.val, TableHeaders.PICKUP_CENTROID_LONGITUDE.val,
                TableHeaders.PICKUP_CENTROID_LOCATION.val, TableHeaders.DROPOFF_CENTROID_LATITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LONGITUDE.val, TableHeaders.DROPOFF_CENTROID_LOCATION.val};
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            StringBuilder builder = new StringBuilder();

            builder.append(fields[0]);
            for(int i =1; i < fields.length; ++i){
                builder.append(",").append(fields[i]);
            }
            writer.write(builder.append("\n").toString());

            var wkbWriter = new WKBWriter(2);
            for (var car : cars) {
                builder.delete(0, builder.length());

                builder.append(car.tripRequest.tripId).append(",");

                ArrayList<Coordinate> lineOfCoordinates = new ArrayList<>(GeoTools.getTripGraph(car));

                int size = lineOfCoordinates.size();

                builder.append(GeoTools.getGeoStringRepresentation(lineOfCoordinates));

                builder.append(",").append(car.tree.originalRoot.arrivingTime);

                builder.append(",").append(String.format("%s,%s",
                        car.tree.originalRoot.location.x, car.tree.originalRoot.location.y));

                builder.append(",").append(String.format("POINT (%s %s)",
                        car.tree.originalRoot.location.x, car.tree.originalRoot.location.y));

                builder.append(",").append(String.format("%s,%s",
                        lineOfCoordinates.get(size-1).y, lineOfCoordinates.get(size-1).x));

                builder.append(",").append(String.format("POINT (%s %s)",
                        lineOfCoordinates.get(size-1).y, lineOfCoordinates.get(size-1).x));

                writer.append(builder.append("\n").toString());
            }
            writer.flush();
        }
    }

    public static void requestsWriter(List<TripRequest> requests, String path) throws IOException {
        String fields[] = {TableHeaders.REQUEST_ID.val,TableHeaders.TRIP_ID.val, TableHeaders.GEOM.val,
                TableHeaders.TRIP_START_TIMESTAMP.val, TableHeaders.PICKUP_CENTROID_LATITUDE.val,
                TableHeaders.PICKUP_CENTROID_LONGITUDE.val, TableHeaders.PICKUP_CENTROID_LOCATION.val,
                TableHeaders.DROPOFF_CENTROID_LATITUDE.val, TableHeaders.DROPOFF_CENTROID_LONGITUDE.val,
                TableHeaders.DROPOFF_CENTROID_LOCATION.val};
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            StringBuilder builder = new StringBuilder();

            builder.append(fields[0]);
            for(int i =1; i < fields.length; ++i){
                builder.append(",").append(fields[i]);
            }
            writer.write(builder.append("\n").toString());

            var wkbWriter = new WKBWriter(2);
            for (var request : requests) {
                builder.delete(0, builder.length());

                builder.append(request.tripId).append(",");
                builder.append(request.carId.orElse("")).append(",");

                ArrayList<Coordinate> lineOfCoordinates = new ArrayList<>();
                lineOfCoordinates.add(new Coordinate(request.origin.y, request.origin.x)); // CARTO format
                lineOfCoordinates.add(new Coordinate(request.destination.y, request.destination.x)); // CARTO format

                int size = lineOfCoordinates.size();

                builder.append(GeoTools.getGeoStringRepresentation(lineOfCoordinates));

                builder.append(",").append(request.dateOfRequest);

                builder.append(",").append(String.format("%s,%s",
                        request.origin.x, request.origin.y));

                builder.append(",").append(String.format("POINT (%s %s)",
                        request.origin.x, request.origin.y));

                builder.append(",").append(String.format("%s,%s",
                        request.destination.x, request.destination.y));

                builder.append(",").append(String.format("POINT (%s %s)",
                        request.destination.x, request.destination.y));

                writer.append(builder.append("\n").toString());
            }
            writer.flush();
        }
    }
}
