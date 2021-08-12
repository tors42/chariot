package chariot.internal;

import java.util.stream.Stream;

import chariot.model.Err;

public sealed interface InternalResult<T> {

    public record Success<T>(Stream<T> data) implements InternalResult<T> {}
    public record Failure<T>(int statusCode, Err err) implements InternalResult<T> {}
    public record Error<T>(String error) implements InternalResult<T> {}

}
