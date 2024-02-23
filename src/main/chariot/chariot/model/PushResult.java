package chariot.model;

import java.util.Map;

public sealed interface PushResult {
    Map<String,String> tags();
    default int moves()    { return this instanceof Pass pass ? pass.moves() : 0; }
    default String error() { return this instanceof Fail fail ? fail.error() : ""; }

    record Pass(Map<String, String> tags, int moves)    implements PushResult {}
    record Fail(Map<String, String> tags, String error) implements PushResult {}
}
