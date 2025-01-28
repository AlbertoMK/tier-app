package server.Database;

import com.mongodb.client.*;
import org.bson.Document;
import server.Model.Routine;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.util.List;
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
}
