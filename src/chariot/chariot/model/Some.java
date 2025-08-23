package chariot.model;

import java.util.Optional;

public record Some<T>(T value) implements One<T>, Opt<T> {
     @Override public Optional<T> maybe() { return Optional.of(value); }
     @Override public T get() { return value; }
}
