package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class Routine {

    @Getter
    private String routineName;

    @Getter
    private List<Exercise> exercises;

}
