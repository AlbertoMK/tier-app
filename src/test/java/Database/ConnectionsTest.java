package Database;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import server.Database.MongoDBConnector;
import server.Database.MySqlConnector;
import server.Model.Routine;
import server.Model.User;

import java.sql.SQLException;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ConnectionsTest {

    @Test
    @Disabled("Use this test to try if you have connection with MySQL database via Docker")
    public void tryMySQLConnection() throws SQLException {
        MySqlConnector c = new MySqlConnector();
        c.connectDatabase();
        User userCreated = new User("Gonzalo", "Password", Calendar.getInstance());
        try {
            c.addUser(userCreated);
            User userRetrieved = c.findByUsername("Gonzalo").get();
            assertEquals(userCreated.getUsername(), userRetrieved.getUsername());
            assertEquals(userCreated.getPassword(), userRetrieved.getPassword());
        } catch(Exception ex){
            fail(ex.getMessage());
        }
    }

    @Test
    @Disabled("Use this test to try if you have connection with MongoDB database via Docker")
    public void tryMongoDBConnection() throws SQLException {
        MongoDBConnector c = new MongoDBConnector();
        c.connectDatabase();
        Routine routine = new Routine(4, "Routine name");
        c.createRoutine(routine);
        Routine retrievedRoutine = c.findById(4).get();
        assertEquals(routine.getId(), retrievedRoutine.getId());
        assertEquals(routine.getRoutineName(), retrievedRoutine.getRoutineName());
        assertEquals(routine.getExercises(), retrievedRoutine.getExercises());
    }
}
