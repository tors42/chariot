package chariot.model;

public record AI(int level, String name) implements Player, GameStateEvent.Side, GameInfo.Opponent {
    public AI(int level) { this(level, "Level " + level); }
    public String id() { return "AI"; }
}
