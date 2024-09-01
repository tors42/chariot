package chariot.model;

import java.time.Duration;

public record Clock(Duration initial, Duration increment) {

    public static Clock of(Duration initial)   { return new Clock(initial, Duration.ZERO); }
    public static Clock ofSeconds(int initial) { return of(Duration.ofSeconds(initial)); }
    public static Clock ofMinutes(int initial) { return of(Duration.ofMinutes(initial)); }

    public Clock withIncrement(Duration increment)   { return new Clock(initial, increment); }
    public Clock withIncrementSeconds(int increment) { return new Clock(initial, Duration.ofSeconds(increment)); }

    @Override
    public String toString() {
        return "%d+%d".formatted(initial.toMinutes(), increment().toSeconds());
    }
}
