package server.Model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ExerciseSet {

    private Exercise exercise;
    private List<Set> sets;

}
