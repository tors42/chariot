package chariot.model;

import java.util.List;

public record BulkPairing(
            String id,
            String variant,
            boolean rated,
            Long pairAt,
            Long pairedAt,
            Long startClocksAt,
            Long scheduledAt,
            Clock clock,
            List<Game> games) implements Model {

    public record Clock(Integer limit, Integer increment) {}
    public record Game(String id, String black, String white) {}

}
