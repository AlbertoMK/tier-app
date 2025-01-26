package server.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Routine {

    private String routineName;

    private List<Exercise> exercises;
    private int id;

    public Routine(int id, String routineName) {
        this.id = id;
        this.routineName = routineName;
        exercises = new ArrayList<>();
    }
}
