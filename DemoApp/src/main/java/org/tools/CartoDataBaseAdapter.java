package org.tools;

import com.roxstudio.utils.CUrl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.locationtech.jts.geom.Coordinate;
import server.TableHeaders;
import server.model.users.TripRequest;

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

    private enum RequestConstants{
        USERNAME("dakochik"),
        REQUEST_HEADER("Content-Type: application/json"),
        REQUESTS_TABLE_NAME("chicago_5000_requests"),
        API_KEY("1dfba9e5fb93bade9610d6c49e070d65f5760ddb"),
        ADDRESS(String.format("https://%s.carto.com/api/v2/sql",USERNAME.val));


        private final String val;
        RequestConstants(String val){
            this.val = val;
        }
    }

    public List<TripRequest> readRequests() throws ParseException {
        List<TripRequest> result = new ArrayList<>();

        String data = String.format("{\"q\":\"%s\"}", String.format("SELECT * FROM %s WHERE (%s is null or %s = '')",
                RequestConstants.REQUESTS_TABLE_NAME.val, TableHeaders.TRIP_ID, TableHeaders.TRIP_ID));
        CUrl curl = new CUrl(RequestConstants.ADDRESS.val);
        curl.data(data);
        curl.header(RequestConstants.REQUEST_HEADER.val);

        Document doc = Jsoup.parseBodyFragment(curl.exec(htmlResolver, null).outerHtml());
        System.out.printf("Request code: %s%n\n", curl.getHttpCode()); // Change to logging

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(doc.body().text());
        JSONArray arr = (JSONArray)obj.get("rows");
        DateTimeFormatter format = DateTimeFormatter.ISO_DATE_TIME;

        for(var request : arr){
            JSONObject requestObj = (JSONObject)parser.parse(request.toString());
            String reqId = requestObj.get(TableHeaders.REQUEST_ID.val).toString();
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

    public void updateRequestsByTripId(List<TripRequest> trips){
        var validForUpdating = trips.stream()
                .filter(it -> it.carId.isPresent()).collect(Collectors.toList());

        StringBuilder dataFilterValues = new StringBuilder();
        StringBuilder dataRules = new StringBuilder();

        for(int i =0; i < validForUpdating.size(); ++i){
            int counter = 0;

            dataRules.append("WHEN '").append(validForUpdating.get(i).tripId)
                    .append("' THEN '").append(validForUpdating.get(i).carId.get()).append("' ");
            dataFilterValues.append("'").append(validForUpdating.get(i).tripId).append("'");
            ++counter;
            ++i;

            while (counter < 100 && i < validForUpdating.size()){ // Creates 100 size buckets or less
                dataRules.append("WHEN '").append(validForUpdating.get(i).tripId)
                        .append("' THEN '").append(validForUpdating.get(i).carId.get()).append("' ");
                dataFilterValues.append(", '").append(validForUpdating.get(i).tripId).append("'");
                ++counter;
                ++i;
            }

            String data = String.format("{\"q\":\"%s\"}",
                    String.format("UPDATE %s SET %s = CASE %s %s ELSE %s END WHERE %s IN(%s)",
                            RequestConstants.REQUESTS_TABLE_NAME.val, TableHeaders.TRIP_ID.val,
                            TableHeaders.REQUEST_ID.val,
                            dataRules.toString(), TableHeaders.REQUEST_ID.val,
                            TableHeaders.REQUEST_ID.val, dataFilterValues.toString()));
            CUrl curl = new CUrl(String.format("%s%s",RequestConstants.ADDRESS.val, RequestConstants.API_KEY.val));
            curl.data(data);
            curl.header(RequestConstants.REQUEST_HEADER.val);

            System.out.println(curl.exec(htmlResolver, null).outerHtml());

            System.out.printf("Update bucket up to %s finished with code: %s%n\n",
                    i, curl.getHttpCode());  // Change to logging
        }
    }
}
