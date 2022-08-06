package chariot.model;
public record Entries<T>(java.util.stream.Stream<T> stream) implements Many<T> {}
