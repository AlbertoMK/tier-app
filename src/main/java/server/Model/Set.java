package server.Model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Set {
    private int reps;
    private double weight; // In kg
    private double distance; // In km
    private int duration; // In seconds
    private SetType setType;

    public enum SetType {
        WARMUP,
        FAILURE,
        NORMAL,
        DROPSET
    }

}