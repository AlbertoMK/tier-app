package Database;

import org.junit.jupiter.api.*;
import server.Database.MongoDBConnector;
import server.Model.*;
import server.Utils.LoggerService;

import java.util.ArrayList;
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
    public void testErrorAddingNewRoutineWithoutExercises() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Upper 1");

        result = connector.addRoutine(routine);

        assertFalse(result);
    }

    @Test
    public void testErrorAddingDuplicateRoutineWithoutExercises() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Cardio training");
        Exercise exercise = new CardioExercise();
        server.Model.Set set1 = server.Model.Set.builder()
                .distance(100)
                .build();
        server.Model.Set set2 = server.Model.Set.builder()
                .distance(100)
                .build();
        ExerciseSet exerciseSet = new ExerciseSet(exercise, List.of(set1, set2));
        routine.setExerciseSets(List.of(exerciseSet));
        connector.addRoutine(routine);

        Routine routineDuplicated = new Routine();
        routine.setId(1);
        routine.setRoutineName("Cardio training");
        Exercise exerciseDuplicated = new CardioExercise();
        server.Model.Set set1Dup = server.Model.Set.builder()
                .distance(100)
                .build();
        server.Model.Set set2Dup = server.Model.Set.builder()
                .distance(100)
                .build();
        exerciseSet = new ExerciseSet(exerciseDuplicated, List.of(set1Dup, set2Dup));
        routineDuplicated.setExerciseSets(List.of(exerciseSet));
        connector.addRoutine(routine);

        result = connector.addRoutine(routineDuplicated);

        assertFalse(result);
    }

    @Test
    public void testAddNewRoutineWithExercises() {
        boolean result;
        Routine sprints = new Routine();
        sprints.setId(2);
        sprints.setRoutineName("Sprints");

        Exercise exercise = new CardioExercise();

        server.Model.Set set1 = server.Model.Set.builder()
                .distance(100)
                .build();
        server.Model.Set set2 = server.Model.Set.builder()
                .distance(100)
                .build();

        ExerciseSet exerciseSet = new ExerciseSet(exercise, List.of(set1, set2));

        sprints.setExerciseSets(List.of(exerciseSet));

        result = connector.addRoutine(sprints);

        assertTrue(result);
    }

    @Test
    public void testFindAllRoutines() {
        Routine routine1 = new Routine();
        routine1.setId(100);
        routine1.setRoutineName("Cardio training");
        connector.addRoutine(routine1);

        Routine routine2 = new Routine();
        routine2.setId(101);
        routine2.setRoutineName("Gym training");
        connector.addRoutine(routine2);


        List<Routine> routines = new ArrayList<>();
        routines.add(routine1);
        routines.add(routine2);

        List<Routine> allRoutines = connector.findAllRoutines();
        assertFalse(allRoutines.stream().filter(routine -> routine.getId()==routines.get(0).getId()).toList().isEmpty());
        assertFalse(allRoutines.stream().filter(routine -> routine.getId()==routines.get(1).getId()).toList().isEmpty());
        Routine routineFound0 = allRoutines.stream().filter(routine -> routine.getId()==routines.get(0).getId()).toList().get(0);
        Routine routineFound1 = allRoutines.stream().filter(routine -> routine.getId()==routines.get(1).getId()).toList().get(0);
        assertEquals(routine1.getRoutineName(), routineFound0.getRoutineName());
        assertEquals(routine2.getRoutineName(), routineFound1.getRoutineName());
        // Falta verificar todos los ejercicios de cada rutina
    }

    @Test
    public void testFindRoutineById() { // Done
        Routine routine1 = new Routine();
        routine1.setId(1);
        routine1.setRoutineName("Cardio training");
        connector.addRoutine(routine1);

        Optional<Routine> routineSearch;
        routineSearch = connector.findById(1);

        assertTrue(routineSearch.isPresent());
    }

    @Test
    public void testUpdateRoutine() { // Done
        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Upper");

        connector.addRoutine(routine);

        routine.setRoutineName("Legs");

        connector.updateRoutine(routine);

        Optional<Routine> routineFound = connector.findById(1);
        assertTrue(routineFound.isPresent());
        assertEquals("Legs", routineFound.get().getRoutineName());
    }

    @Test
    public void testDeleteRoutine() { // Done
        Routine routine = new Routine();
        routine.setId(1);
        boolean result;

        connector.addRoutine(routine);

        result = connector.deleteRoutine(routine);

        assertTrue(result);
        assertFalse(connector.findById(1).isPresent());
    }

    @Test
    public void testDeleteRoutineNotFound() { // Done
        Routine routine = new Routine();
        routine.setId(1000);
        boolean result;

        result = connector.deleteRoutine(routine);

        assertFalse(result);
    }

}
