package Database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Database.MongoDBConnector;
import server.Model.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class RoutinesDBTests {

    private static MongoDBConnector connector;

    @BeforeAll
    public static void setUp() {
        connector = new MongoDBConnector();
        connector.connectDatabase();
    }

    @AfterAll
    public static void tearDown() {
        Routine routine = new Routine();
        routine.setId(2);
        connector.deleteRoutine(routine);

        connector.closeDatabase();
    }

    @Test
    public void testConnectionEstablished() {
        assertNotNull(connector, "MongoDBConnector should not be null, if not the connection was unsuccessfully done");
    }

    @Test
    public void testAddNewRoutine() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Test routine");

        result = connector.addRoutine(routine);

        assertTrue(result);
    }

    @BeforeEach
    public void createRoutinesForFindUpdateDeleteTests() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(3);
        routine.setRoutineName("Upper");
        result = connector.addRoutine(routine);
    }

    @Test
    public void testFindRoutineById() {
        Optional<Routine> routineSearch;
        routineSearch = connector.findById(3);

        assertTrue(routineSearch.isPresent());
    }

    @Test
    public void testUpdateRoutine() {
        Routine routine = new Routine();
        routine.setId(3);
        routine.setRoutineName("Legs");

        connector.updateRoutine(routine);

        Optional<Routine> routineFound = connector.findById(3);
        assertTrue(routineFound.isPresent());
        assertEquals("Legs", routineFound.get().getRoutineName());
    }

    @Test
    public void testDeleteRoutine() {
        Routine routine = new Routine();
        routine.setId(3);
        boolean result;

        result = connector.deleteRoutine(routine);

        assertTrue(result);
    }

}
