package chariot.model;

import java.util.Optional;

public record Empty<T>() implements Opt<T> {
    @Override public Optional<T> maybe() { return Optional.empty(); }
}
