package server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;
import server.Database.MongoDBConnector;
import server.Model.Routine;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class MongoDBConnectorTest {
    private static final String COLLECTION_NAME = "routines";
    private MongoDBConnector connector;

    @BeforeEach
    public void setUpDB() {
        connector = new MongoDBConnector();
    }

    @AfterEach
    public void closeDB() {
        connector.closeDatabase();
    }

    @Test
    public void testDatabaseConnection() {
        connector.connectDatabase();

        MongoClient client = connector.getClient();
        MongoDatabase db = connector.getDatabase();
        assertNotNull(client, "MongoClient should not be null after connection");
        assertNotNull(db, "Database should not be null after connection");
    }

}
