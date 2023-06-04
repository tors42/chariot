package chariot.model;

public sealed interface TournamentId {
    String id();

    record ArenaId(String id) implements TournamentId {}
    record SwissId(String id) implements TournamentId {}

    static TournamentId arena(String id) { return new ArenaId(id); }
    static TournamentId swiss(String id) { return new SwissId(id); }
}
