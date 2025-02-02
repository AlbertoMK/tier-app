package server.Database;

import com.mongodb.client.*;
import org.bson.Document;
import server.Model.Exercise;
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

    public MongoDatabase database; //Poner en privado cuando se pruebe
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



 // Estos dos métodos los voy a quitar en cuanto meta un test más
    public MongoClient getClient() {
        return client;
    }
    public MongoDatabase getDatabase() {
        return database;
    }
 // --------------------------------------------------------------




    public void addRoutine(Routine routine) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Document newRoutine = new Document("_id", routine.getId())
                  .append("name", routine.getRoutineName())
                  .append("exercises", routine.getExerciseSets().stream().map(exercise -> exercise.getExercise().getExerciseName()).collect(Collectors.toList()));
        collection.insertOne(newRoutine);
    }

    public Optional<Routine> findById(int id) {
        MongoCollection<Document> collection = database.getCollection(ROUTINE_COLLECTION);
        Document document = collection.find(new Document("_id", id)).first();
        Routine routine = new Routine();
        routine.setId((int)document.get("_id"));
        routine.setRoutineName((String) document.get("name"));
        routine.setExerciseSets((List)document.get("exercises"));
        return Optional.of(routine);
    }

    @Override
    public void updateRoutine(Routine routine) {

    }

    @Override
    public void deleteRoutine(Routine routine) {

    }

    @Override
    public void findAllRoutines() {

    }

    @Override
    public List<Routine> findRoutinesByUser(User user) {
        return List.of();
    }

    @Override
    public Optional<Routine> findByFilters(Map<String, String> filter) {
        return Optional.empty();
    }

    @Override
    public void addExerciseToRoutine(Routine routine, Exercise exercise) {

    }

    @Override
    public void removeExerciseFromRoutine(Routine routine, Exercise exercise) {

    }

    @Override
    public void duplicatePersonalizedRoutineToAssignToOtherUser(Routine routine, User otherUser) {

    }

    @Override
    public int countRoutinesByUser(User user) {
        return 0;
    }

    @Override
    public List<Routine> getPopularRoutines() {
        return List.of();
    }
}
