package chariot.model;

public sealed interface Ack permits Fail, Ok {
    static Ack ok() { return new Ok(); }
    static Ack fail(String message) { return fail(-1, message); }
    static Ack fail(int status, String message) { return new Fail<>(status, message); }
}
