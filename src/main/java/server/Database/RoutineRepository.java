package server.Database;

import server.Model.Exercise;
import server.Model.Routine;
import server.Model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RoutineRepository {
    public void addRoutine(Routine routine);
    public Optional<Routine> findById(int id);
    public void updateRoutine(Routine routine);
    public void deleteRoutine(Routine routine);
    public void findAllRoutines();
    public List<Routine> findRoutinesByUser(User user);
    public Optional<Routine> findByFilters(Map<String, String> filter);
    public void addExerciseToRoutine(Routine routine, Exercise exercise);
    public void removeExerciseFromRoutine(Routine routine, Exercise exercise);
    public void duplicatePersonalizedRoutineToAssignToOtherUser(Routine routine, User otherUser);
    public int countRoutinesByUser(User user);
    public List<Routine> getPopularRoutines();
}
