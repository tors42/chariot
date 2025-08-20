package chariot.model;

import java.time.ZonedDateTime;

import chariot.model.Enums.Color;

public record GameMeta (
     String id,
     boolean rated,
     Variant variant,
     TimeControl timeControl,
     Enums.Status status,
     ZonedDateTime createdAt,
     Players players,
     Opt<Color> winner
     )  {

    public record Players(Player white, Player black) {}

    public sealed interface Player {
        default String  userId()      { return this instanceof Account a ? a.userId() : ""; }
        default int     rating()      { return this instanceof Account a ? a.rating() : 0; }
        default boolean provisional() { return this instanceof Account a ? a.provisional() : true; }
    }

    public record Account(String userId, int rating, boolean provisional) implements Player {}
    public record AI(int level) implements Player {}
    public record Anonymous() implements Player {}
}
