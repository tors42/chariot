package chariot.model;

import java.util.List;

public record TournamentStatus(
        List<ArenaLight> created,
        List<ArenaLight> started,
        List<ArenaLight> finished)  {}
