package chariot.model;

import java.util.function.Function;

public sealed interface Opt<T> permits Some, Empty {
    static <T> Opt<T>   of(T value)   { return value == null ? new Empty<T>() : new Some<T>(value); }
    static <T> Some<T>  some(T value) { return new Some<T>(value); }
    static <T> Empty<T> empty()       { return new Empty<T>(); }

    default <R> Opt<R> map(Function<T,R> mapper) {
        if (this instanceof Some<T> some) return Opt.some(mapper.apply(some.value()));
        return Opt.empty();
    }

    default T orElse(T other) {
        if (this instanceof Some<T> some) return some.value();
        return other;
    }
}
