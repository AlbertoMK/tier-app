package server.Model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Set {
    private int reps;
    private int weight;
    private int distance;
    private int duration;
    private SetType setType;

    public enum SetType {
        WARMUP,
        FAILURE,
        NORMAL,
        DROPSET
    }

}