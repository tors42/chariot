package chariot.api;
public record Entry<T>(T entry) implements One<T> {}
