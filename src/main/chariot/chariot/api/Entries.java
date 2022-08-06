package chariot.api;
public record Entries<T>(java.util.stream.Stream<T> stream) implements Many<T> {}
