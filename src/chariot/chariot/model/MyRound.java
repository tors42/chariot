package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;

public record MyRound(Broadcast.Tour tour, Round round, RoundInfo.Study study) {

    public String id() { return round().id(); }

    public record Round(
            String id,
            String slug,
            String name,
            ZonedDateTime createdAt,
            boolean startsAfterPrevious,
            Opt<ZonedDateTime> startsAt,
            Opt<ZonedDateTime> finishedAt,
            boolean ongoing,
            boolean finished,
            boolean rated,
            URI url,
            Duration delay,
            Opt<Broadcast.CustomScoring> customScoring
            ) {}
}
