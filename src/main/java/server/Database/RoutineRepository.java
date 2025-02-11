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
    public int findAllRoutines();
    public List<Routine> findRoutinesCreatedByUser(User user);
    public Optional<Routine> findRoutinesWithFilters(Map<String, String> filter);
}
