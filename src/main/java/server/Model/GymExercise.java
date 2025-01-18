package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class GymExercise extends Exercise {

    public enum DifficultyLevel {
        NOVICE,
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT,
        LEGENDARY,
        MASTER,
        GRANDMASTER
    }

    public enum MuscleGroup {
        ABDOMINALS,
        ABDUCTORS,
        ADDUCTORS,
        BACK,
        BICEPS,
        CALVES,
        CHEST,
        FOREARMS,
        GLUTES,
        HAMSTRINGS,
        HIP_FLEXORS,
        QUADRICEPS,
        SHINS,
        SHOULDERS,
        TRAPEZIUS,
        TRICEPS
    }

    public enum Equipment {
        STABILITY_BALL,
        BODYWEIGHT,
        GYMNASTICS_RINGS,
        PARALLETTE_BARS,
        DUMBBELL,
        KETTLEBELL,
        BARBELL,
        PULL_UP_BAR,
        SLIDERS,
        BULGARIAN_BAG,
        TRAP_BAR,
        EZ_BAR,
        MEDICINE_BALL,
        SLAM_BALL,
        BATTLE_ROPES,
        AB_WHEEL,
        CLUBBELL,
        INDIAN_CLUB,
        MACEBELL,
        RESISTANCE_BAND,
        SUPERBAND,
        MINIBAND,
        SUSPENSION_TRAINER,
        CABLE,
        LANDMINE,
        HEAVY_SANDBAG,
        SANDBAG,
        WEIGHT_PLATE,
        WALL_BALL,
        TIRE,
        BENCH_FLAT,
        BENCH_INCLINE,
        PLYO_BOX,
        BENCH_DECLINE,
        SLANT_BOARD,
        NONE
    }

    public enum Arm {
        NO_ARM,
        ONE_ARM,
        DOUBLE_ARM
    }

    public enum Grip {
        NEUTRAL,
        NO_GRIP,
        FLAT_PALM,
        HEAD_SUPPORTED,
        PRONATED,
        FOREARM,
        CRUSH_GRIP,
        SUPINATED,
        BOTTOMS_UP,
        BOTTOMS_UP_HORN_GRIP,
        HORN_GRIP,
        MIXED_GRIP,
        FINGERTIP,
        HAND_ASSISTED,
        GOBLET,
        WAITER_HOLD,
        FALSE_GRIP,
        OTHER
    }

    public enum BodyRegion {
        LOWER_BODY,
        MID_SECTION,
        UPPER_BODY,
        FULL_BODY
    }

    @Getter
    private DifficultyLevel difficultyLevel;
    @Getter
    private MuscleGroup muscleGroup;
    @Getter
    private Equipment equipment;
    @Getter
    private Arm arm;
    @Getter
    private Grip grip;
    @Getter
    private BodyRegion bodyRegion;
}