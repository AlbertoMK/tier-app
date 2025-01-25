package server.Database;

import com.mongodb.client.*;
import org.bson.Document;
import server.Model.Routine;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

public class MongoDBConnector implements RoutineRepository{

    private MongoDatabase database;
    private static final String ROUTINE_COLLECTION = "routines";

    public void connectionMongoDB () {
        LoggerService.log("Starting connection with mongodb database...");
        String uri = PropertiesLoader.getProperty("database.mongodb.url");
        try (MongoClient mongoClient = MongoClients.create(uri)) {

            database = mongoClient.getDatabase(PropertiesLoader.getProperty("database.mongodb.name"));
            LoggerService.log("MongoDB connection successful");

        } catch (Exception e) {
            LoggerService.logerror("Error connecting to mongodb database");
        }
    }

    public void createRoutine(Routine routine) {

    }
}
