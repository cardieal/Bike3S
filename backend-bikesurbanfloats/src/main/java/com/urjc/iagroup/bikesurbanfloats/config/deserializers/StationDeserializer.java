package com.urjc.iagroup.bikesurbanfloats.config.deserializers;

import com.google.gson.*;
import com.urjc.iagroup.bikesurbanfloats.entities.Bike;
import com.urjc.iagroup.bikesurbanfloats.entities.Station;
import com.urjc.iagroup.bikesurbanfloats.graphs.GeoPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to obtain the stations in the system's own format. 
 * @author IAgroup
 *
 */
public class StationDeserializer implements JsonDeserializer<List<Station>>  {

    private static final String JSON_ATTR_BIKES = "bikes";
    private static final String JSON_ATTR_CAPACITY = "capacity";
    private static final String JSON_ATTR_POSITION = "position";
    
    @Override
    public List<Station> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<Station> stations = new ArrayList<>();

        for (JsonElement element : json.getAsJsonArray()) {
            JsonObject station = element.getAsJsonObject();
            JsonElement jsonElementBikes = station.get(JSON_ATTR_BIKES);
            int capacity = station.get(JSON_ATTR_CAPACITY).getAsInt();
            List<Bike> bikes = new ArrayList<>();

            boolean isArray = jsonElementBikes.isJsonArray();
            JsonArray jsonArrayBikes = isArray ? jsonElementBikes.getAsJsonArray() : null;
            int n = isArray ? jsonArrayBikes.size() : jsonElementBikes.getAsInt();
            int naux = capacity - n;
            for (int i = 0; i < n; i++) {
                // TODO: check if the deserialization context actually calls the bike constructor
                Bike bike = isArray ? context.deserialize(jsonArrayBikes.get(i), Bike.class) : new Bike();
                bikes.add(bike);
            }
            for(int i = 0; i < naux; i++) {
                bikes.add(null);
            }

            JsonElement jsonElemGeoP = element.getAsJsonObject().get(JSON_ATTR_POSITION);
            GeoPoint position = context.deserialize(jsonElemGeoP, GeoPoint.class);
            stations.add(new Station(position, capacity, bikes));
        }
        
        return stations;
    }
    
}
