package chariot.model;

import java.util.List;

public record StormDashboard(High high, List<Day> days) implements Model {
    public record High(int allTime, int day, int month, int week) {}
    public record Day(String _id, int combo, int errors, int highest, int moves, int runs, int score, int time) {}
}
