package com.urjc.iagroup.bikesurbanfloats.history;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.urjc.iagroup.bikesurbanfloats.config.SimulationConfiguration;
import com.urjc.iagroup.bikesurbanfloats.entities.Entity;
import com.urjc.iagroup.bikesurbanfloats.events.Event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class History {

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    private static EntityCollection initialEntities = new EntityCollection();
    private static EntityCollection updatedEntities = new EntityCollection();

    private static TreeMap<Integer, List<EventEntry>> serializedEvents = new TreeMap<>();

    private static Path outputPath = Paths.get("history");

    public static void init(SimulationConfiguration simulationConfiguration) {
        /*initialEntities = new EntityCollection();
        updatedEntities = new EntityCollection();*/

        // TODO: save history output path in the indicated path in configuration file
    }

    public static void close() throws IOException {

        // TODO: maybe split entities.json into multiple files, e.g. entities/users.json

        Map<String, Collection<HistoricEntity>> entries = new HashMap<>();
        initialEntities.getEntityMaps().forEach((historicClass, entities) -> {
            String jsonIdentifier = getJsonIdentifier(historicClass);
            entries.put(jsonIdentifier, entities.values());
        });

        writeJson("entities.json", entries);
        writeTimeEntries();

        // TODO: copy configuration file
    }

    public static void registerEntity(Entity entity) {
        Class<? extends HistoricEntity> historicClass = getReferenceClass(entity.getClass());
        HistoricEntity historicEntity = instantiateHistoric(entity);
        initialEntities.addToMapFor(historicClass, historicEntity);
        updatedEntities.addToMapFor(historicClass, historicEntity);
    }

    public static void registerEvent(Event event) throws IOException {
        List<HistoricEntity> historicEntities = new ArrayList<>();

        for (Entity entity : event.getEntities()) {
            historicEntities.add(instantiateHistoric(entity));
        }

        Map<String, List<JsonObject>> changes = serializeChanges(historicEntities);

        if (!serializedEvents.containsKey(event.getInstant())) {
            if (serializedEvents.size() == 100) {
                writeTimeEntries();
                serializedEvents.clear();
            }

            serializedEvents.put(event.getInstant(), new ArrayList<>());
        }

        serializedEvents.get(event.getInstant()).add(new EventEntry(event.getClass().getSimpleName(), changes));

        for (HistoricEntity entity : historicEntities) {
            updatedEntities.addToMapFor(entity.getClass(), entity);
        }
    }

    private static void writeJson(String name, Object content) throws IOException {
        File json = outputPath.resolve(name).toFile();
        json.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(json)) {
            gson.toJson(content, writer);
        }
    }

    private static void writeTimeEntries() throws IOException {
        List<TimeEntry> timeEntries = new ArrayList<>();

        serializedEvents.forEach((time, eventEntries) -> {
            timeEntries.add(new TimeEntry(time, eventEntries));
        });

        String fileName = new StringBuilder()
                .append(serializedEvents.firstKey()).append("-")
                .append(serializedEvents.lastKey()).append("_")
                .append(serializedEvents.size()).append(".json").toString();

        writeJson(fileName, timeEntries);
    }

    private static Map<String, List<JsonObject>> serializeChanges(List<HistoricEntity> entities) {
        Map<String, List<JsonObject>> changes = new HashMap<>();

        for (HistoricEntity entity : entities) {
            Class<? extends HistoricEntity> historicClass = entity.getClass();
            HistoricEntity oldEntity = updatedEntities.getMapFor(historicClass).get(entity.getId());
            String jsonIdentifier = getJsonIdentifier(historicClass);
            JsonObject jsonEntity = new JsonObject();

            // TODO: maybe read private fields instead of methods for consistency with gson
            for (Field field : historicClass.getDeclaredFields()) {
                if (field.getName().equals("id")) continue;

                JsonElement property;

                field.setAccessible(true);

                try {
                    property = propertyChange(field.get(oldEntity), field.get(entity));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalStateException("Error reading field " + field + " from " + historicClass);
                }

                if (property != null) {
                    jsonEntity.add(field.getName(), property);
                }
            }

            if (!changes.containsKey(jsonIdentifier)) {
                changes.put(jsonIdentifier, new ArrayList<>());
            }

            if (!jsonEntity.entrySet().isEmpty()) {
                jsonEntity.add("id", new JsonPrimitive(entity.getId()));
                changes.get(jsonIdentifier).add(jsonEntity);
            }
        }

        return changes;
    }

    /**
     * It finds out, from an entity class, the corresponding history class.
     * @param entityClass It is the entity class whose corresponding history class musts be found out.
     * @return the corresponding history class to the entity class.
     */
    private static Class<? extends HistoricEntity> getReferenceClass(Class<? extends Entity> entityClass) {
        HistoryReference[] referenceClasses = entityClass.getAnnotationsByType(HistoryReference.class);

        if (referenceClasses.length == 0) {
            throw new IllegalStateException("No annotation @HistoryReference found for " + entityClass);
        }

        if (referenceClasses.length > 1) {
            throw new IllegalStateException("Found more than one @HistoryReference annotation in inheritance chain for " + entityClass);
        }

        return referenceClasses[0].value();
    }

    private static String getJsonIdentifier(Class<? extends HistoricEntity> historicClass) {
        JsonIdentifier[] jsonIdentifiers = historicClass.getAnnotationsByType(JsonIdentifier.class);

        if (jsonIdentifiers.length == 0) {
            throw new IllegalStateException("No annotation @JsonIdentifier found for " + historicClass);
        }

        if (jsonIdentifiers.length > 1) {
            throw new IllegalStateException("Found more than one @JsonIdentifier annotation in inheritance chain for " + historicClass);
        }

        return jsonIdentifiers[0].value();
    }

    /**
     * It creates, from a specific entity, the corresponding concrete history. 
     * @param entity It is the entity whose history must be created.
     * @return the concrete history corresponding to the entity.
     */
    @SuppressWarnings("unchecked")
    private static HistoricEntity instantiateHistoric(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        Class<? extends HistoricEntity> historicClass = getReferenceClass(entityClass);

        Class<? extends Entity> constructorParameter = entityClass;

        while (!constructorParameter.getSuperclass().equals(Object.class)) {
            constructorParameter = (Class<? extends Entity>) constructorParameter.getSuperclass();
        }

        try {
            Constructor<? extends HistoricEntity> historicConstructor = historicClass.getConstructor(constructorParameter);
            return historicConstructor.newInstance(entity);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new IllegalStateException("No matching constructor found for " + historicClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Error trying to instantiate " + historicClass);
        }
    }

    private static JsonObject propertyChange(Object oldProperty, Object newProperty) {
        if (Objects.equals(oldProperty, newProperty)) return null;

        JsonObject property = new JsonObject();
        property.add("old", gson.toJsonTree(oldProperty));
        property.add("new", gson.toJsonTree(newProperty));

        return property;
    }
    
    /**
     * This class is used to save the histories of all the entities of the system.
     * It provides methods to save a new entity history and to consult one.
     * @author IAgroup
     *
     */
    private static class EntityCollection {
       /**
        * It is a map whose key is the class of the historic entity and whose value is another map 
        * with the entity identifier as the key and the history of the entity as the value.
        */
        private Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> entityMaps;

        EntityCollection() {
            this.entityMaps = new HashMap<>();
        }

        Map<Class<? extends HistoricEntity>, Map<Integer, HistoricEntity>> getEntityMaps() {
            return entityMaps;
        }
        
        /**
         * 
         * @param entityClass It is the map key.
         * @return a map with all the instances of the given class type.
         */
        Map<Integer, HistoricEntity> getMapFor(Class<? extends HistoricEntity> entityClass) {
            return entityMaps.get(entityClass);
        }

        /**
         * It adds a new instance of entity historic to the map.
         * @param entityClass It is the key of the amp.
         * @param entity It is the instance to add to the map.
         */
        void addToMapFor(Class<? extends HistoricEntity> entityClass, HistoricEntity entity) {
            if (!entityMaps.containsKey(entityClass)) {
                entityMaps.put(entityClass, new HashMap<>());
            }
            entityMaps.get(entityClass).put(entity.getId(), entity);
        }
    }

    /**
     * This class represents an event and contains the changes which have occurred 
     * with respect to the previus event.
     * @author IAgroup
     */
    private static class EventEntry {

        /**
         * It is the name of the event.
         */
        @Expose
        private String name;
        
        /**
         * They are the differences between an event and the next one.
         */
        @Expose
        private Map<String, List<JsonObject>> changes;

        EventEntry(String name, Map<String, List<JsonObject>> changes) {
            this.name = name;
            this.changes = changes;
        }
    }
    
    /**
     * This class rpresents a time instant of the simulation and contains all the events 
     * which happen at this moment. 
     * @author IAgroup
     *
     */
    private static class TimeEntry {
    	  /**
       	* It is the moment when the events happen. 
    	   */
        @Expose
        private int time;
        
        /**
         * They are the vents which happen at the specific moment.
         */
        @Expose
        private List<EventEntry> events;

        TimeEntry(int time, List<EventEntry> events) {
            this.time = time;
            this.events = events;
        }
    }

}
