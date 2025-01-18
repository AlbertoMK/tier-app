package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Exercise {

    @Getter
    protected String exerciseName;

    @Getter
    protected String description;

}
