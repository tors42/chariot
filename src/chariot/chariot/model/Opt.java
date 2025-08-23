package chariot.model;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Opt<T> permits Some, Empty {
    static <T> Opt<T>   of(T value)   { return value == null ? new Empty<T>() : new Some<T>(value); }
    static <T> Opt<T>   of()          { return new Empty<T>(); }
    static <T> Some<T>  some(T value) { return new Some<T>(value); }
    static <T> Empty<T> empty()       { return new Empty<T>(); }

    Optional<T> maybe();
    default <R> Opt<R> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Some(var value) -> Opt.of(mapper.apply(value));
            case Empty() -> Opt.of();
        };
    }

    default boolean isPresent() { return this instanceof Some; }

    default T get() {
        return switch (this) {
            case Some(var value) -> value;
            case Empty() -> null;
        };
    }

    default T orElse(T orElse) {
        return switch (this) {
            case Some(var value) -> value;
            case Empty() -> orElse;
        };
    }

    default T orElseGet(Supplier<T> orElse) {
        return switch (this) {
            case Some(var value) -> value;
            case Empty() -> orElse.get();
        };
    }


}
