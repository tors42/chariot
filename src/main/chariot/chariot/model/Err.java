package chariot.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public sealed interface Err extends Model {

    default String text() {
        if (this instanceof Error e) {
            return e.error();
        } else if (this instanceof Failure f) {
            return f.message().stream().collect(Collectors.joining("\n"));
        } else if (this instanceof Tokens t) {
            return t.tokens().entrySet().stream()
                .map(e -> "\n" + e.getValue() + " : " + e.getKey())
                .collect(Collectors.joining());
        }

        return toString();
    }

    public static Err error(String json) {
        return new Error(json);
    }

    public static Err fail(String text) {
        return new Failure(List.of(text));
    }


    // todo, maybe make a custom parsing for a record Error (Map<String, List<String>> errors) {}
    // {"clock.limit":["Error"]}
    // Could probably be multiple errors per key, therefore the Map.
    // And the key name seems to be one of the parameters used in the input,
    // so dynamic (hence need for custom parsing) / Mapper.mappings.put(Err.class, fun<Json, Err>).
    // Well well.

    public record Error(String error) implements Err {}
    public record Failure(List<String> message) implements Err {}
    public record Tokens(Map<String,String> tokens) implements Err {}

}
