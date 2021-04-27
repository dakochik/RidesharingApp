package manager.csv_read_writer;

import org.locationtech.jts.geom.Coordinate;
import server.model.users.TripRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalDataReadWriter {
    //
    private static final String pathToFile ="./././././././././RideAustin_Weather.csv"; // Git не пропустил этот файл
    private static final String pathToFile2 ="./././././././././Chicago_5000.csv";
    private static final String pathToFile2Additional ="./././././././././Chicago_5000_Offset5000.csv";

    public static ArrayList<TripRequest> getData(int size) throws IOException {
        ArrayList<TripRequest> res = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(pathToFile))){
            int counter = size;
            reader.readLine();
            String string = reader.readLine();
            while(counter >= 0 && string!=null){
                String[] arr = string.split(",");

                res.add(new TripRequest(new Coordinate(Double.parseDouble(arr[2]), Double.parseDouble(arr[3])),
                        new Coordinate(Double.parseDouble(arr[14]), Double.parseDouble(arr[13])),
                        20, 0.8, LocalDateTime.now(), arr[0]));

                string = reader.readLine();

                --counter;
            }

            System.out.println(res.size());
        }

        return res;
    }

    public static Map<LocalDateTime, List<TripRequest>> getGroupedByTimeData(ArrayList<TripRequest> data) throws IOException {

        return data.stream().collect(Collectors.groupingBy(it -> it.dateOfRequest));
    }

    public static ArrayList<TripRequest> getDataSec(int size) throws IOException {
        ArrayList<TripRequest> res = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(pathToFile2))){
            int counter = size;
            reader.readLine();
            String string = reader.readLine();
            DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;
            while(counter >= 0 && string!=null){
                String[] arr = string.split(",");

                try {
                    if (!(arr[15].isEmpty() || arr[16].isEmpty() || arr[18].isEmpty() || arr[19].isEmpty())) {
                        res.add(new TripRequest(new Coordinate(Double.parseDouble(arr[15].replace("\"", "")),
                                Double.parseDouble(arr[16].replace("\"", ""))),
                                new Coordinate(Double.parseDouble(arr[18].replace("\"", "")),
                                        Double.parseDouble(arr[19].replace("\"", ""))),
                                TripRequest.DEFAULT_WAITING_TIME, TripRequest.DEFAULT_TRIP_COEFFICIENT,
                                LocalDateTime.parse(arr[1].replace("\"", "")
                                        .replace(".",":").substring(0, arr.length-2), format),
                                arr[0].replace("\"", "")));
                    }
                }
                catch (Exception e){

                }

                string = reader.readLine();

                --counter;
            }

            System.out.println(res.size());
        }

        return res.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }
}
