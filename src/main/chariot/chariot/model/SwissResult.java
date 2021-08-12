package chariot.model;

public record SwissResult(
        String username,
        int rank,
        float points,
        float tieBreak,
        int rating,
        int performance,
        boolean absent) implements Model {}
