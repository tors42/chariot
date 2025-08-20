package chariot.model;
public record Entry<T>(T entry) implements One<T> {}
