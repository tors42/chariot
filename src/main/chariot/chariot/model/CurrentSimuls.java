package chariot.model;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import chariot.internal.Util;

import static chariot.internal.Util.orEmpty;

public record CurrentSimuls(
        List<Simul> pending,
        List<Simul> created,
        List<Simul> started)  {

    public record Simul (
            String id,
            String text,
            String name,
            String fullname,
            boolean isCreated,
            boolean isFinished,
            boolean isRunning,
            int nbApplicants,
            int nbPairings,
            Optional<Long> estimatedStartAt,
            Optional<Long> startedAt,
            Optional<Long> finishedAt,
            Host host,
            List<Variant> variants) {
            public Simul {
                name = orEmpty(name);
                fullname = orEmpty(fullname);
                text = orEmpty(text);
            }

            public Optional<ZonedDateTime> estimatedStartTime() { return estimatedStartAt.map(Util::fromLong); }
            public Optional<ZonedDateTime> startedAtTime() { return startedAt.map(Util::fromLong); }
            public Optional<ZonedDateTime> finishedAtTime() { return finishedAt.map(Util::fromLong); }

        public record Host(String id, String name, int rating, String title) {
            public Host {
                title = orEmpty(title);
            }
        }
        public record Variant(String key, String name, String icon) {}
    }
}
