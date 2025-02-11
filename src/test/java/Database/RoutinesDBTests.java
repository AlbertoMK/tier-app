package Database;

import org.junit.jupiter.api.*;
import server.Database.MongoDBConnector;
import server.Model.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


public class RoutinesDBTests {

    private static MongoDBConnector connector;

    @BeforeEach
    public void setUp() {
        connector = new MongoDBConnector();
        connector.connectDatabase();
        connector.removeAllRoutines();

        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Upper Training");
        connector.addRoutine(routine);

        Routine routine1 = new Routine();
        routine1.setId(100);
        routine1.setRoutineName("Cardio training");
        connector.addRoutine(routine1);

        Routine routine2 = new Routine();
        routine2.setId(101);
        routine2.setRoutineName("Gym training");
        connector.addRoutine(routine2);

    }

    @AfterAll
    public static void tearDown() {
        connector.closeDatabase();
    }

    @Test
    public void testConnectionEstablished() {
        assertNotNull(connector, "MongoDBConnector should not be null, if not the connection was unsuccessfully done");
    }

    @Test
    public void testAddNewRoutineWithoutExercises() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Upper 1");

        result = connector.addRoutine(routine);

        assertTrue(result);
    }

    @Test
    public void testErrorAddingDuplicateRoutine() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(1);

        result = connector.addRoutine(routine);

        assertFalse(result);
    }

    @Test
    public void testAddNewRoutineWithExercises() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Upper 1");

        Exercise exercise = new CardioExercise();

        server.Model.Set set1 = server.Model.Set.builder()
                .reps(10)
                .distance(10)
                .build();
        server.Model.Set set2 = server.Model.Set.builder()
                .reps(10)
                .distance(10)
                .build();

        ExerciseSet exerciseSet = new ExerciseSet(exercise, List.of(set1, set2));

        routine.setExerciseSets(List.of(exerciseSet));

        result = connector.addRoutine(routine);

        assertTrue(result);
    }

    @Test
    public void testFindAllRoutines() {
        int numbAllRoutines = connector.findAllRoutines();
        assertEquals(3, numbAllRoutines);
    }

    @Test
    public void testFindRoutineById() {
        Optional<Routine> routineSearch;
        routineSearch = connector.findById(1);

        assertTrue(routineSearch.isPresent());
    }

    @Test
    public void testUpdateRoutine() {
        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Legs");

        connector.updateRoutine(routine);

        Optional<Routine> routineFound = connector.findById(1);
        assertTrue(routineFound.isPresent());
        assertEquals("Legs", routineFound.get().getRoutineName());
    }

    @Test
    public void testDeleteRoutine() {
        Routine routine = new Routine();
        routine.setId(1);
        boolean result;

        result = connector.deleteRoutine(routine);

        assertTrue(result);
    }

    @Test
    public void testDeleteRoutineNotFound() {
        Routine routine = new Routine();
        routine.setId(1000);
        boolean result;

        result = connector.deleteRoutine(routine);

        assertFalse(result);
    }

}
