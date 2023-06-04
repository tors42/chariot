package chariot.model;

import java.time.Duration;

import chariot.model.Enums.Speed;

public record RealTime(Duration initial, Duration increment, String show, Speed speed) implements TimeControl {}

