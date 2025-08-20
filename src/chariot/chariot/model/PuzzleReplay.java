package chariot.model;

import java.util.List;

public record PuzzleReplay(ReplayInfo replay, AngleInfo angle) {

    public record ReplayInfo(int days, PuzzleAngle theme, int nb, List<String> remaining) {}
    public record AngleInfo(PuzzleAngle key, String name, String desc) {}

}
