package chariot.model;
public record Fail<T>(int status, Err info) implements NoEntry<T>, Many<T> {
    public String message() { return info.message(); }
}
