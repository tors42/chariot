package chariot.api;

public sealed interface Err {
    public record Empty()                          implements Err {}
    public record Info(String message)             implements Err {}

    static Err from(String string) {
        if (string == null || string.isEmpty()) return new Empty();
        return new Info(string);
    }
}
