package Database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Database.MySqlConnector;
import server.Model.Exercise;
import server.Model.GymExercise;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExercisesDBTests {

    private static MySqlConnector connector;

    @BeforeAll
    public static void setup() throws SQLException {
        connector = new MySqlConnector();
        connector.connectDatabase();
    }

    @AfterAll
    public static void cleanup() {
        connector.closeDatabse();
    }

    @Test
    public void retrieveAllExercises() {
        Set<Exercise> exercises = connector.findAllExercises();
        assertEquals(2988, exercises.size());
    }

    @Test
    public void retrieveSpecificExercise() {
        Optional<Exercise> exercise = connector.findExerciseByName("Barbell Bench Press");
        assertTrue(exercise.isPresent());
        GymExercise ge = (GymExercise) exercise.get();
        assertEquals("NOVICE", ge.getDifficultyLevel().name());
        assertEquals("CHEST", ge.getMuscleGroup().name());
        assertEquals("PRONATED", ge.getGrip().name());
        assertEquals("DOUBLE_ARM", ge.getArm().name());
        assertEquals("UPPER_BODY", ge.getBodyRegion().name());
        assertEquals("BARBELL", ge.getEquipment().name());
        assertEquals("WEIGHTED_REPETITIONS", ge.getSetsType().name());
    }

    @Test
    public void retrieveUnexistentExercise() {
        Optional<Exercise> exercise = connector.findExerciseByName("Not found");
        assertTrue(exercise.isEmpty());
    }

    @Test
    public void retrieveExerciseWithFilter() {
        Map<String, String> filters = Map.of("grip", "Pronated", "body_region", "Upper Body", "single_double_arm", "Double Arm");
        Set<Exercise> set = connector.findExerciseWithFilters(filters);
        assertEquals(200, set.size());
        filters = Map.of("grip", "Pronated", "body_region", "Upper Body", "single_double_arm", "Double Arm", "difficulty", "Master");
        set = connector.findExerciseWithFilters(filters);
        assertEquals(8, set.size());
    }

    @Test
    public void emptySetWithFilters() {
        Map<String, String> filters = Map.of("grip", "Pronated", "body_region", "Upper Body", "single_double_arm", "Double Arm", "muscle_group", "Quadriceps");
        Set<Exercise> set = connector.findExerciseWithFilters(filters);
        assertTrue(set.isEmpty());
    }
}
