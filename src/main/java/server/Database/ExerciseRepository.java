package server.Database;

import server.Model.Exercise;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ExerciseRepository {

    public Optional<Exercise> findExerciseByName(String exerciseName);
    public Set<Exercise> findAllExercises();
    public Set<Exercise> findExerciseWithFilters(Map<String, String> filters);
}
