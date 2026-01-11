package chariot.model;

import java.util.Optional;

public record Fail<T>(int status, String message) implements Ack, One<T>, Many<T> {
     @Override public Optional<T> maybe() { return Optional.empty(); }
     @Override public T get() { return null; }
     @Override public String toString() { return status == -1 ? message : "%d - %s".formatted(status, message); }
}
