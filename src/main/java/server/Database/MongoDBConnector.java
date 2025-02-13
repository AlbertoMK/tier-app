package server.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.Model.Exercise;
import server.Model.ExerciseSet;
import server.Model.GymExercise;
import server.Model.Routine;
import server.Model.Set;
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
        Document document = routineToDocument(routine).get();
        if (document.isEmpty()) {
            return false;
        }
        collection.insertOne(document);
        return true;
    }

    private Optional<Document> routineToDocument(Routine routine) {
        try {
            Document document = new Document();
            document.append("name", routine.getRoutineName());
            document.append("_id", routine.getId());
            List<ExerciseSet> exerciseSets = routine.getExerciseSets();
            List<Document> exerciseSetsDocument = new ArrayList<>();
            exerciseSets.forEach(exerciseSet -> {
                Document exerciseSetDocument = new Document();
                exerciseSetDocument.append("exerciseName", exerciseSet.getExercise().getExerciseName());
                List<Document> setsDocument = new ArrayList<>();
                exerciseSet.getSets().forEach(set -> {
                    Document setDocument = new Document();
                    setDocument.append("type", set.getSetType().name());
                    switch (exerciseSet.getExercise().getSetsType()) {
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
                });
                exerciseSetDocument.append("sets", setsDocument);
                exerciseSetsDocument.add(exerciseSetDocument);
                document.append("exerciseSets", exerciseSetsDocument);
            });
            return Optional.of(document);
        } catch (Exception e) {
            LoggerService.logerror("Error transforming Routine to MongoDB Document");
            return Optional.empty();
        }
    }

    private Routine documentToRoutine(Document document) {
        int id = document.getInteger("_id");
        String name = document.getString("name");
        List<Document> exerciseSetsDocument = (List<Document>) document.get("exerciseSets");
        List<ExerciseSet> exerciseSets = new ArrayList<>();

        for (Document exerciseSetDoc : exerciseSetsDocument) {
            String exerciseName = exerciseSetDoc.getString("exerciseName");
            GymExercise exercise = new GymExercise(exerciseName, null,  null, null, null, null, null, null);
            List<Document> setsDocument = (List<Document>) exerciseSetDoc.get("sets");
            List<server.Model.Set> sets = new ArrayList<>();
            Exercise.SetsType setsType = exercise.getSetsType();
            for (Document setDoc : setsDocument) {
                Set.SetType type = Set.SetType.valueOf(setDoc.getString("type"));
                server.Model.Set.SetBuilder set = Set.builder()
                        .setType(type);

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
        return new Routine(id, name, exerciseSets);
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
            return null;
        }
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
