package chariot.model;

public record Fail<T>(int status, String message) implements Ack, One<T>, Many<T> {}
