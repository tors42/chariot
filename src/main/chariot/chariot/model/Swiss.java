package chariot.model;

import chariot.model.Enums.VariantName;

public record Swiss (
        String id,
        String name,
        String createdBy,
        String startsAt,
        String status,
        Integer nbOngoing,
        Integer nbPlayers,
        Integer nbRounds,
        Integer round,
        boolean rated,
        VariantName variant,
        Clock clock,
        GreatPlayer greatPlayer,
        NextRound nextRound,
        Quote quote
        ) implements Model {

    public record Clock (Integer limit, Integer increment) {}
    public record GreatPlayer (String name, String url) {}
    public record NextRound (String at, Integer in) {}
    public record Quote (String text, String author) {}
}
