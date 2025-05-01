package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Exercise {

    public enum SetsType {
        WEIGHTED_REPETITIONS,
        REPETITIONS,
        TIME,
        TIME_DISTANCE
    }

    protected String exerciseName;
    protected SetsType setsType;

}
