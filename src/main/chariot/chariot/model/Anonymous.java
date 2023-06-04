package chariot.model;

public record Anonymous() implements Player, GameStateEvent.Side, GameInfo.Opponent {
    @Override public String name() { return "Anonymous"; }
    @Override public String id()   { return ""; }
}
