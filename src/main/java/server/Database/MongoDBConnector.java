package server.Database;

import com.mongodb.client.*;
import org.bson.Document;
import server.Model.ExerciseSet;
import server.Model.Routine;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MongoDBConnector implements RoutineRepository{

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
        if(client != null) {
            client.close();
            LoggerService.log("MongoDB connection closed successfully");
        }
    }

    public boolean addRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Boolean result = false;
        try {
            // Allows to create a routine without exercises
            List<String> exercises = (routine.getExerciseSets() == null || routine.getExerciseSets().isEmpty()) ?
                    new ArrayList<>() :
                    routine.getExerciseSets().stream()
                            .map(exercise -> exercise.getExercise().getExerciseName())
                            .collect(Collectors.toList());
            Document newRoutine = new Document("_id", routine.getId())
                    .append("name", routine.getRoutineName())
                    .append("exercises", exercises);
            collection.insertOne(newRoutine);
            result = true;
        } catch (Exception e) {
            LoggerService.logerror("Error inserting new routine");
            result = false;
        }
        return result;
    }

    public Optional<Routine> findById(int id) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Document document = collection.find(new Document("_id", id)).first();
        if (document != null) {
            Routine routine = new Routine();
            routine.setId((int) document.get("_id"));
            routine.setRoutineName((String) document.get("name"));
            routine.setExerciseSets((List) document.get("exercises"));
            return Optional.of(routine);
        }
        return Optional.empty();
    }

    @Override
    public boolean updateRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        boolean result;
        if (findById(routine.getId()).isPresent()) {
            collection.updateOne(new Document("_id", routine.getId()), new Document("$set", new Document("name", routine.getRoutineName())));
            result = true;
        } else {
            LoggerService.logerror("Error updating routine");
            result = false;
        }
        return result;
    }

    @Override
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

    @Override
    public void findAllRoutines() {

    }

    @Override
    public List<Routine> findRoutinesByUser(User user) {
        return List.of();
    }

    @Override
    public Optional<Routine> findRoutinesWithFilters(Map<String, String> filter) {
        return Optional.empty();
    }

    @Override
    public boolean addExerciseToRoutine(Routine routine, ExerciseSet exercise) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        boolean result;
        if (findById(routine.getId()).isPresent()) {
            if (exercise.getExercise().getExerciseName() != null) {
                Document exerciseDocument = new Document("exercise", exercise.getExercise().getExerciseName())
                        .append("sets", exercise.getSets().stream().map(set -> {
                            return new Document("reps", set.getReps())
                                    .append("weight", set.getWeight())
                                    .append("duration", set.getDuration())
                                    .append("distance", set.getDistance())
                                    .append("setType", set.getSetType().toString());
                        }).collect(Collectors.toList()));
                collection.updateOne(new Document("_id", routine.getId()), new Document("$push", new Document("exercises", exerciseDocument)));
                result = true;
            } else {
                LoggerService.logerror("Error adding exercise to routine because exercise is null");
                result = false;
            }
        } else {
            LoggerService.logerror("Error adding exercise to routine because routine does not exists");
            result = false;
        }
        return result;
    }

    @Override
    public boolean removeExerciseFromRoutine(Routine routine, ExerciseSet exercise) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        boolean result;
        if (findById(routine.getId()).isPresent()) {
            if (routine.getExerciseSets().contains(exercise)) {
                Document exerciseDocument = new Document("exercise", exercise.getExercise().getExerciseName());
                collection.updateOne(new Document("_id", routine.getId()), new Document("$pull", new Document("exercises", exerciseDocument)));
                result = true;
            } else {
                LoggerService.logerror("Error removing exercise from routine because routine does not contain the exercise selected");
                result = false;
            }
        } else {
            LoggerService.logerror("Error removing exercise from routine because routine does not exists");
            result = false;
        }
        return result;
    }
}
