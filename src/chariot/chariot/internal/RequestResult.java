package chariot.internal;

public sealed interface RequestResult {
    record Failure(int code, String body) implements RequestResult {}
    record Success(java.util.stream.Stream<String> stream) implements RequestResult {}
}


