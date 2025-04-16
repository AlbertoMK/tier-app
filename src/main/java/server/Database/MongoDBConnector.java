package server.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.Model.*;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoDBConnector {

    private MongoDatabase database;
    private MongoClient client;
    private static final String ROUTINE_COLLECTION = "routines";

    public void connectDatabase() {
        LoggerService.log("Starting connection with mongodb database...");
        String uri = PropertiesLoader.getProperty("database.mongodb.url");
        try {
            client = MongoClients.create(uri);
            database = client.getDatabase(PropertiesLoader.getProperty("database.mongodb.name"));
            LoggerService.log("MongoDB connection successful");

        } catch (Exception e) {
            LoggerService.logerror("Error connecting to mongodb database");
        }
    }

    public void closeDatabase() {
        if (client != null) {
            client.close();
            LoggerService.log("MongoDB connection closed successfully");
        }
    }

    public void removeAllRoutines() {
        if (database != null) {
            MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
            collection.drop();
        } else {
            LoggerService.logerror("Error dropping all routines - Connection not established");
        }
    }

    public boolean addRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Optional<Document> optionalDoc = routineToDocument(routine);
        if (optionalDoc.isEmpty()) {
            return false;
        }

        try {
            collection.insertOne(optionalDoc.get());
            return true;
        } catch (Exception e) {
            LoggerService.logerror("Error inserting routine: " + e.getMessage());
            return false;
        }
    }



    private Optional<Document> routineToDocument(Routine routine) {
        try {
            Document document = new Document();
            document.append("name", routine.getRoutineName());
            document.append("_id", routine.getId());

            List<ExerciseSet> exerciseSets = routine.getExerciseSets();
            List<Document> exerciseSetsDocument = new ArrayList<>();

            if (exerciseSets != null && !exerciseSets.isEmpty()) {
                for (ExerciseSet exerciseSet : exerciseSets) {
                    Document exerciseSetDocument = new Document();
                    Exercise exercise = exerciseSet.getExercise();

                    if (exercise == null) continue;

                    exerciseSetDocument.append("exerciseName", exercise.getExerciseName());
                    exerciseSetDocument.append("setsType", exercise.getSetsType().name());

                    List<Document> setsDocument = new ArrayList<>();
                    for (Set set : exerciseSet.getSets()) {
                        Document setDocument = new Document();
                        setDocument.append("type", set.getSetType().name());
                        switch (exercise.getSetsType()) {
                            case TIME -> setDocument.append("duration", set.getDuration());
                            case REPETITIONS -> setDocument.append("reps", set.getReps());
                            case TIME_DISTANCE -> {
                                setDocument.append("duration", set.getDuration());
                                setDocument.append("distance", set.getDistance());
                            }
                            case WEIGHTED_REPETITIONS -> {
                                setDocument.append("weight", set.getWeight());
                                setDocument.append("reps", set.getReps());
                            }
                        }
                        setsDocument.add(setDocument);
                    }

                    exerciseSetDocument.append("sets", setsDocument);
                    exerciseSetsDocument.add(exerciseSetDocument);
                }
            }
            document.append("exerciseSets", exerciseSetsDocument);

            return Optional.of(document);
        } catch (Exception e) {
            LoggerService.logerror("Error transforming Routine to MongoDB Document");
            return Optional.empty();
        }
    }

    public Optional<Routine> findById(int id) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        try {
            Document document = collection.find(Filters.eq("_id", id)).first();
            if (document == null) {
                return Optional.empty();
            }
            Routine routine = documentToRoutine(document);
            return Optional.of(routine);

        } catch (Exception e) {
            LoggerService.logerror("Error retrieving routine with id " + id);
            return Optional.empty();
        }
    }

    private Routine documentToRoutine(Document document) {
        int id = document.getInteger("_id");
        String name = document.getString("name");
        List<Document> exerciseSetsDocument = (List<Document>) document.get("exerciseSets");
        List<ExerciseSet> exerciseSets = new ArrayList<>();

        if (!exerciseSetsDocument.isEmpty()) {
            for (Document exerciseSetDoc : exerciseSetsDocument) {
                String exerciseName = exerciseSetDoc.getString("exerciseName");
                String setsTypeStr = exerciseSetDoc.getString("setsType");
                Exercise.SetsType setsType = Exercise.SetsType.valueOf(setsTypeStr);

                Exercise exercise;
                if (setsType == Exercise.SetsType.TIME_DISTANCE) {
                    exercise = new CardioExercise();
                    exercise.setExerciseName(exerciseName);
                    exercise.setSetsType(setsType);
                } else {
                    exercise = new GymExercise(exerciseName, setsType, null, null, null, null, null, null);
                }

                List<Document> setsDocument = (List<Document>) exerciseSetDoc.get("sets");
                List<server.Model.Set> sets = new ArrayList<>();

                for (Document setDoc : setsDocument) {
                    Set.SetType type = Set.SetType.valueOf(setDoc.getString("type"));
                    server.Model.Set.SetBuilder set = Set.builder().setType(type);

                    switch (setsType) {
                        case TIME -> set.duration(setDoc.getInteger("duration"));
                        case REPETITIONS -> set.reps(setDoc.getInteger("reps"));
                        case TIME_DISTANCE -> {
                            set.duration(setDoc.getInteger("duration"));
                            set.distance(setDoc.getInteger("distance"));
                        }
                        case WEIGHTED_REPETITIONS -> {
                            set.weight(setDoc.getDouble("weight"));
                            set.reps(setDoc.getInteger("reps"));
                        }
                    }
                    sets.add(set.build());
                }

                ExerciseSet exerciseSet = new ExerciseSet(exercise, sets);
                exerciseSets.add(exerciseSet);
            }
        }


        return new Routine(id, name, exerciseSets);
    }

    public boolean updateRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Bson filter = Filters.eq("_id", routine.getId());
        Document document = routineToDocument(routine).get();
        if (document.isEmpty()) {
            return false;
        }
        collection.replaceOne(filter, document);
        return true;
    }

    public boolean deleteRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        boolean result;
        if (findById(routine.getId()).isPresent()) {
            collection.deleteOne(new Document("_id", routine.getId()));
            result = true;
        } else {
            LoggerService.logerror("Error deleting routine");
            result = false;
        }
        return result;
    }

    public List<Routine> findAllRoutines() {
        List<Routine> routines = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);

        for (Document document : collection.find()) {
            Routine routine = documentToRoutine(document);
            routines.add(routine);
        }
        return routines;
    }
}
