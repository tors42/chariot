package chariot.model;

import java.util.Map;

public sealed interface PushResult {
    Map<String,String> tagMap();
    default int moves() { return this instanceof Pass pass ? pass.moves() : 0; }
    default String error() { return this instanceof Fail fail ? fail.error() : ""; }

    record Pass(Map<String, String> tagMap, int moves)    implements PushResult {}
    record Fail(Map<String, String> tagMap, String error) implements PushResult {}
}
