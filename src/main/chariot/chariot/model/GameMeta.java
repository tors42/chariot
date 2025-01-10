package chariot.model;

import java.time.ZonedDateTime;

import chariot.model.Enums.Color;

public record GameMeta (
     String id,
     boolean rated,
     Variant variant,
     RealTime timeControl,
     Enums.Status status,
     ZonedDateTime createdAt,
     Players players,
     Opt<Color> winner
     )  {

    public record Players(Player white, Player black) {}
    public record Player(String userId, int rating, boolean provisional) {}
}
