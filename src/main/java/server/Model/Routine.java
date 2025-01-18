package server.Model;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class Routine {

    private String routineName;

    private List<Exercise> exercises;

}
