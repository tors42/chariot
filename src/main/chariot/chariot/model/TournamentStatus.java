package chariot.model;

import java.util.List;

public record TournamentStatus(
        List<Tournament> created,
        List<Tournament> started,
        List<Tournament> finished) implements Model {}
