package server.Database;

import server.Model.ExerciseSet;
import server.Model.Routine;
import server.Model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RoutineRepository {
    public boolean addRoutine(Routine routine);
    public Optional<Routine> findById(int id);
    public boolean updateRoutine(Routine routine);
    public boolean deleteRoutine(Routine routine);
    public void findAllRoutines();
    public List<Routine> findRoutinesByUser(User user);
    public Optional<Routine> findRoutinesWithFilters(Map<String, String> filter);
    public boolean addExerciseToRoutine(Routine routine, ExerciseSet exercise);
    public boolean removeExerciseFromRoutine(Routine routine, ExerciseSet exercise);
}
