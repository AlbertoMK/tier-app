package Database;

import org.junit.jupiter.api.*;
import server.Database.MongoDBConnector;
import server.Model.*;
import server.Utils.LoggerService;

import javax.swing.text.html.Option;
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
        //connector.removeAllRoutines();
        connector.closeDatabase();
    }

    @Test
    public void testConnectionEstablished() {
        assertNotNull(connector, "MongoDBConnector should not be null, if not the connection was unsuccessfully done");
    }

    @Test
    public void testAddingNewRoutineWithoutExercises() {
        boolean result;
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Upper 1");

        result = connector.addRoutine(routine);

        assertTrue(result);
    }

    @Test
    public void testErrorAddingDuplicateRoutineWithoutExercises() {
        boolean result;

        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Cardio training");
        connector.addRoutine(routine);

        Routine routineDuplicated = new Routine();
        routineDuplicated.setId(1);
        routineDuplicated.setRoutineName("Cardio training");

        result = connector.addRoutine(routineDuplicated);

        assertFalse(result);
    }

    @Test
    public void testAddRoutineWithNullFields() {
        Routine routine = new Routine();
        routine.setId(1234);

        boolean result = connector.addRoutine(routine);

        assertTrue(result, "Routine can be added without name");
        Optional<Routine> retrieved = connector.findById(1234);
        assertTrue(retrieved.isPresent());
    }


    @Test
    public void testAddNewRoutineWithExercises() {
        Routine routine = new Routine();
        routine.setId(2);
        routine.setRoutineName("Mixed Routine");

        CardioExercise cardio = new CardioExercise();
        cardio.setExerciseName("100m Sprint");
        cardio.setSetsType(Exercise.SetsType.TIME_DISTANCE);

        Set cardioSet1 = Set.builder()
                .setType(Set.SetType.NORMAL)
                .duration(15)
                .distance(100)
                .build();

        Set cardioSet2 = Set.builder()
                .setType(Set.SetType.NORMAL)
                .duration(14)
                .distance(100)
                .build();

        ExerciseSet cardioSet = new ExerciseSet(cardio, List.of(cardioSet1, cardioSet2));

        GymExercise gym = new GymExercise(
                "Bench Press",
                Exercise.SetsType.WEIGHTED_REPETITIONS,
                GymExercise.DifficultyLevel.INTERMEDIATE,
                GymExercise.MuscleGroup.CHEST,
                GymExercise.Equipment.BARBELL,
                GymExercise.SingleArm.DOUBLE_ARM,
                GymExercise.Grip.SUPINATED,
                GymExercise.BodyRegion.UPPER_BODY
        );

        Set gymSet1 = Set.builder()
                .setType(Set.SetType.NORMAL)
                .reps(10)
                .weight(60.0)
                .build();

        Set gymSet2 = Set.builder()
                .setType(Set.SetType.NORMAL)
                .reps(8)
                .weight(70.0)
                .build();

        ExerciseSet gymSet = new ExerciseSet(gym, List.of(gymSet1, gymSet2));

        routine.setExerciseSets(List.of(cardioSet, gymSet));

        boolean result = connector.addRoutine(routine);
        assertTrue(result, "Routine should have been added to the database");

        Optional<Routine> retrieved = connector.findById(2);
        assertTrue(retrieved.isPresent(), "Routine should be found");

        Routine found = retrieved.get();
        assertEquals("Mixed Routine", found.getRoutineName());

        List<ExerciseSet> sets = found.getExerciseSets();
        assertEquals(2, sets.size(), "Routine should have 2 exercise sets");



        ExerciseSet retrievedCardioSet = sets.stream()
                .filter(es -> es.getExercise().getExerciseName().equals("100m Sprint"))
                .findFirst().orElse(null);
        assertNotNull(retrievedCardioSet);
        assertEquals(2, retrievedCardioSet.getSets().size());
        assertEquals(Set.SetType.NORMAL, retrievedCardioSet.getSets().get(0).getSetType());



        ExerciseSet retrievedGymSet = sets.stream()
                .filter(es -> es.getExercise().getExerciseName().equals("Bench Press"))
                .findFirst().orElse(null);
        assertNotNull(retrievedGymSet);
        assertEquals(2, retrievedGymSet.getSets().size());
        assertEquals(Set.SetType.NORMAL, retrievedGymSet.getSets().get(0).getSetType());
        assertEquals(70.0, retrievedGymSet.getSets().get(1).getWeight());
    }


    @Test
    public void testFindAllRoutines() {
        Routine routine0 = new Routine();
        routine0.setId(0);
        routine0.setRoutineName("Cardio training");

        Routine routine1 = new Routine();
        routine1.setId(1);
        routine1.setRoutineName("Gym training");

        List<Routine> routines = new ArrayList<>();
        routines.add(routine0);
        routines.add(routine1);

        Exercise benchPress = new GymExercise(
                "Bench Press",
                Exercise.SetsType.WEIGHTED_REPETITIONS,
                GymExercise.DifficultyLevel.INTERMEDIATE,
                GymExercise.MuscleGroup.CHEST,
                GymExercise.Equipment.BARBELL,
                GymExercise.SingleArm.DOUBLE_ARM,
                GymExercise.Grip.SUPINATED,
                GymExercise.BodyRegion.UPPER_BODY
        );
        Set bpSet1 = Set.builder()
                .reps(8)
                .weight(60)
                .setType(Set.SetType.NORMAL)
                .build();
        Set bpSet2 = Set.builder()
                .reps(8)
                .weight(60)
                .setType(Set.SetType.NORMAL)
                .build();

        ExerciseSet benchPressEx = new ExerciseSet(benchPress, List.of(bpSet1, bpSet2));

        Exercise lateralRaises = new GymExercise(
                "Lateral Raises",
                Exercise.SetsType.WEIGHTED_REPETITIONS,
                GymExercise.DifficultyLevel.INTERMEDIATE,
                GymExercise.MuscleGroup.SHOULDERS,
                GymExercise.Equipment.DUMBBELL,
                GymExercise.SingleArm.DOUBLE_ARM,
                GymExercise.Grip.FALSE_GRIP,
                GymExercise.BodyRegion.UPPER_BODY
        );

        Set lrSet1 = Set.builder()
                .reps(12)
                .weight(10)
                .setType(Set.SetType.DROPSET)
                .build();
        Set lrSet2 = Set.builder()
                .reps(12)
                .weight(10)
                .setType(Set.SetType.DROPSET)
                .build();

        ExerciseSet lateralRaisesEx = new ExerciseSet(lateralRaises, List.of(lrSet1, lrSet2));

        List<ExerciseSet> exercisesListR1 = new ArrayList<>();
        exercisesListR1.add(benchPressEx);
        exercisesListR1.add(lateralRaisesEx);

        routine0.setExerciseSets(exercisesListR1);

        connector.addRoutine(routine0);
        connector.addRoutine(routine1);

        List<Routine> allRoutines = connector.findAllRoutines();
        assertFalse(allRoutines.stream().filter(routine -> routine.getId()==routines.get(0).getId()).toList().isEmpty());
        assertFalse(allRoutines.stream().filter(routine -> routine.getId()==routines.get(1).getId()).toList().isEmpty());
        Routine routineFound0 = allRoutines.stream().filter(routine -> routine.getId()==routines.get(0).getId()).toList().get(0);
        Routine routineFound1 = allRoutines.stream().filter(routine -> routine.getId()==routines.get(1).getId()).toList().get(0);
        assertEquals(routine0.getRoutineName(), routineFound0.getRoutineName());
        assertEquals(routine1.getRoutineName(), routineFound1.getRoutineName());

        List<ExerciseSet> foundExerciseListR1 = routineFound0.getExerciseSets();
        assertEquals(2, foundExerciseListR1.size());

        ExerciseSet foundBenchPressEx = foundExerciseListR1.get(0);
        assertEquals("Bench Press", foundBenchPressEx.getExercise().getExerciseName());
        assertEquals(2, foundBenchPressEx.getSets().size());

        Set foundBpSet1 = foundBenchPressEx.getSets().get(0);
        Set foundBpSet2 = foundBenchPressEx.getSets().get(1);

        assertEquals(8, foundBpSet1.getReps());
        assertEquals(60, foundBpSet1.getWeight());
        assertEquals(Set.SetType.NORMAL, foundBpSet1.getSetType());

        assertEquals(8, foundBpSet2.getReps());
        assertEquals(60, foundBpSet2.getWeight());
        assertEquals(Set.SetType.NORMAL, foundBpSet2.getSetType());

        ExerciseSet foundLateralRaisesEx = foundExerciseListR1.get(1);
        assertEquals("Lateral Raises", foundLateralRaisesEx.getExercise().getExerciseName());
        assertEquals(2, foundLateralRaisesEx.getSets().size());

        Set foundLrSet1 = foundLateralRaisesEx.getSets().get(0);
        Set foundLrSet2 = foundLateralRaisesEx.getSets().get(1);

        assertEquals(12, foundLrSet1.getReps());
        assertEquals(10, foundLrSet1.getWeight());
        assertEquals(Set.SetType.DROPSET, foundLrSet1.getSetType());

        assertEquals(12, foundLrSet2.getReps());
        assertEquals(10, foundLrSet2.getWeight());
        assertEquals(Set.SetType.DROPSET, foundLrSet2.getSetType());

        assertTrue(routineFound1.getExerciseSets() == null || routineFound1.getExerciseSets().isEmpty());
    }

    @Test
    public void testFindRoutineById() {
        Routine routine1 = new Routine();
        routine1.setId(1);
        routine1.setRoutineName("Cardio training");
        connector.addRoutine(routine1);

        Optional<Routine> routineSearch;
        routineSearch = connector.findById(1);
        assertTrue(routineSearch.isPresent());

        Routine routine2 = new Routine();
        routine2.setId(2025);
        routine2.setRoutineName("Cardio training");
        connector.addRoutine(routine2);

        Optional<Routine> routineSearch2 = connector.findById(2025);
        assertTrue(routineSearch2.isPresent());
    }

    @Test
    public void testUpdateRoutine() {
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
    public void testUpdateRoutineExercises() {
        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Upper");

        connector.addRoutine(routine);

        Optional<Routine> routineFound = connector.findById(1);
        assertTrue(routineFound.isPresent(), "Routine should exists before update");
        assertTrue(routineFound.get().getExerciseSets().isEmpty(), "Routine should not have exercises right now");

        Exercise benchPress = new GymExercise(
                "Bench Press",
                Exercise.SetsType.WEIGHTED_REPETITIONS,
                GymExercise.DifficultyLevel.INTERMEDIATE,
                GymExercise.MuscleGroup.CHEST,
                GymExercise.Equipment.BARBELL,
                GymExercise.SingleArm.DOUBLE_ARM,
                GymExercise.Grip.SUPINATED,
                GymExercise.BodyRegion.UPPER_BODY
        );
        Set bpSet1 = Set.builder()
                .reps(8)
                .weight(60)
                .setType(Set.SetType.NORMAL)
                .build();
        Set bpSet2 = Set.builder()
                .reps(12)
                .weight(60)
                .setType(Set.SetType.DROPSET)
                .build();

        ExerciseSet benchPressEx = new ExerciseSet(benchPress, List.of(bpSet1, bpSet2));

        routine.setExerciseSets(List.of(benchPressEx));

        connector.updateRoutine(routine);

        Optional<Routine> routineFoundAfterUpdate = connector.findById(1);
        assertTrue(routineFoundAfterUpdate.isPresent(), "Routine should still exist");

        Routine updatedRoutine = routineFoundAfterUpdate.get();
        List<ExerciseSet> updatedSets = updatedRoutine.getExerciseSets();
        assertEquals(1, updatedSets.size(), "Routine should have an exercise after update");

        ExerciseSet updatedExerciseSet = updatedSets.get(0);
        assertEquals("Bench Press", updatedExerciseSet.getExercise().getExerciseName());
        assertEquals(Exercise.SetsType.WEIGHTED_REPETITIONS, updatedExerciseSet.getExercise().getSetsType());

        List<Set> setsUpdated = updatedExerciseSet.getSets();
        assertEquals(2, setsUpdated.size(), "Sets should be two");

        assertEquals(Set.SetType.NORMAL, setsUpdated.get(0).getSetType());
        assertEquals(8, setsUpdated.get(0).getReps());
        assertEquals(60.0, setsUpdated.get(0).getWeight());

        assertEquals(Set.SetType.DROPSET, setsUpdated.get(1).getSetType());
        assertEquals(12, setsUpdated.get(1).getReps());
        assertEquals(60.0, setsUpdated.get(1).getWeight());
    }

    @Test
    public void testErrorUpdatingRoutineNotFound() {
        Routine routine = new Routine();
        routine.setId(500);
        routine.setRoutineName("N/A");

        connector.updateRoutine(routine);

        Optional<Routine> routineFound = connector.findById(500);
        assertFalse(routineFound.isPresent(), "Routine should exists before update");
    }

    @Test
    public void testDeleteRoutine() {
        Routine routine = new Routine();
        routine.setId(1);
        boolean result;

        connector.addRoutine(routine);

        result = connector.deleteRoutine(routine);

        assertTrue(result);
        assertFalse(connector.findById(1).isPresent());
    }

    @Test
    public void testDeleteRoutineNotFound() {
        Routine routine = new Routine();
        routine.setId(1000);
        boolean result;

        result = connector.deleteRoutine(routine);

        assertFalse(result);
    }

    @Test
    public void testDeleteRoutineTwice() {
        Routine routine = new Routine();
        routine.setId(1);
        routine.setRoutineName("Goldito");
        connector.addRoutine(routine);

        boolean firtsDelete = connector.deleteRoutine(routine);
        assertTrue(firtsDelete);
        boolean secondDelete = connector.deleteRoutine(routine);
        assertFalse(secondDelete, "Routine cannot be deleted twice");
    }

}
