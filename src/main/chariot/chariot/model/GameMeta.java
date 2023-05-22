package chariot.model;

import chariot.model.Enums.VariantName;
import chariot.internal.Util;

public record GameMeta (
     String id,
     VariantName variant,
     String speed,
     String perf,
     boolean rated,
     Integer status,
     String statusName,
     Long createdTime,
     Clock clock,
     Players players
     )  {

    public java.time.ZonedDateTime createdAt() {
        return Util.fromLong(createdTime());
    }

    public record Clock(Integer initial, Integer increment, Integer totalTime) {}

    public record Players(Player white, Player black) {
        public record Player(String userId, Integer rating) {}
    }
}
