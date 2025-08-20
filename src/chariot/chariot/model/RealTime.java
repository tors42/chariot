package chariot.model;

import java.time.Duration;

import chariot.model.Enums.Speed;

public record RealTime(Clock clock, String show, Speed speed) implements TimeControl {
    public Duration initial()   { return clock.initial(); }
    public Duration increment() { return clock.increment(); }
}

