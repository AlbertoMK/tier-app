package server.Model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Routine {

    private int id;
    private String routineName;
    private List<ExerciseSet> exerciseSets;

    public Routine(int id, String routineName) {
        this.id = id;
        this.routineName = routineName;
        exerciseSets = new ArrayList<>();
    }
}
