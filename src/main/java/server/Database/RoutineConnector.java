package server.Database;

import server.Model.Exercise;
import server.Model.Routine;
import server.Model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoutineConnector implements RoutineRepository {

    private MongoDBConnector mongoConnector;
    private MySqlConnector mysqlConnector;

    public RoutineConnector(MongoDBConnector mongoConnector, MySqlConnector mysqlConnector) {
        this.mongoConnector = mongoConnector;
        this.mysqlConnector = mysqlConnector;
    }

    @Override
    public boolean addRoutine(Routine routine) {
        return mongoConnector.addRoutine(routine);
    }

    @Override
    public Optional<Routine> findById(int id) {
        Optional<Routine> routineOptional = mongoConnector.findById(id);
        if (routineOptional.isEmpty()) {
            return routineOptional;
        }
        Routine routine = routineOptional.get();
        routine.getExerciseSets().forEach(exerciseSet -> {
            Exercise temporalExercise = exerciseSet.getExercise();
            Exercise exercise = mysqlConnector.findExerciseByName(temporalExercise.getExerciseName()).orElseThrow();
            exerciseSet.setExercise(exercise);
        });
        return Optional.of(routine);
    }

    @Override
    public boolean updateRoutine(Routine routine) {
        return mongoConnector.updateRoutine(routine);
    }

    @Override
    public boolean deleteRoutine(Routine routine) {
        return mongoConnector.deleteRoutine(routine);
    }

    @Override
    public List<Routine> findAllRoutines() {
        List<Routine> routines = mongoConnector.findAllRoutines();
        routines.forEach(routine -> {
            routine.getExerciseSets().forEach(exerciseSet -> {
                Exercise exercise = mysqlConnector.findExerciseByName(exerciseSet.getExercise().getExerciseName()).orElseThrow();
                exerciseSet.setExercise(exercise);
            });
        });
        return routines;
    }

    @Override
    public List<Routine> findRoutinesCreatedByUser(User user) {
        return null;
    }

    @Override
    public Optional<Routine> findRoutinesWithFilters(Map<String, String> filter) {
        return Optional.empty();
    }

    @Override
    public void removeAllRoutines() {
        mongoConnector.removeAllRoutines();
    }
}
