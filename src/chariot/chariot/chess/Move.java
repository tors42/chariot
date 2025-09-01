package chariot.chess;

public interface Move {
    String asString();
    static Move wrap(String s) { return () -> s; }
}
