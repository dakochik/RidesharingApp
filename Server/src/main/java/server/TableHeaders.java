package server;

public enum TableHeaders {
    REQUEST_ID("request_id"),
    TRIP_ID("trip_id"),
    GEOM("the_geom"),
    TRIP_START_TIMESTAMP("trip_start_timestamp"),
    PICKUP_CENTROID_LATITUDE("pickup_centroid_latitude"),
    PICKUP_CENTROID_LONGITUDE("pickup_centroid_longitude"),
    PICKUP_CENTROID_LOCATION("pickup_centroid_location"),
    DROPOFF_CENTROID_LATITUDE("dropoff_centroid_latitude"),
    DROPOFF_CENTROID_LONGITUDE("dropoff_centroid_longitude"),
    DROPOFF_CENTROID_LOCATION("dropoff_centroid_location");


    public final String val;
    TableHeaders(String val){
        this.val = val;
    }
}
