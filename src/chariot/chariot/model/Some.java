package chariot.model;

import java.util.Objects;
import java.util.function.Function;

public record Some<T>(T value) implements Opt<T> {
    public Some {
        Objects.requireNonNull(value);
    }

    @Override
    public <R> Opt<R> map(Function<T, R> mapper) {
        return Opt.some(mapper.apply(value()));
    }
}
