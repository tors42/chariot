package chariot.api;
public record Fail<T>(int status, Err info) implements NoEntry<T>, Many<T> {}
