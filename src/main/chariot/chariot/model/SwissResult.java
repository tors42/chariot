package chariot.model;

public record SwissResult(
        int rank,
        float points,
        float tieBreak,
        int rating,
        String username,
        int performance,
        boolean absent)  {}
