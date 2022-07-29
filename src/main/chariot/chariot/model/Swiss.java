package chariot.model;

import java.util.Optional;

import chariot.model.Enums.VariantName;

public record Swiss (
        String id,
        String name,
        String createdBy,
        String startsAt,
        String status,
        int nbOngoing,
        int nbPlayers,
        int nbRounds,
        int round,
        boolean rated,
        boolean isRecentlyFinished,
        VariantName variant,
        Clock clock,
        Optional<GreatPlayer> greatPlayer,
        Optional<NextRound> nextRound,
        Optional<Quote> quote,
        Optional<Stats> stats
        )  {

    public record Clock (Integer limit, Integer increment) {}
    public record GreatPlayer (String name, String url) {}
    public record NextRound (String at, Integer in) {}
    public record Quote (String text, String author) {}
    public record Stats ( int games, int whiteWins, int blackWins, int draws, int byes, int absences, int averageRating)  {}
}
