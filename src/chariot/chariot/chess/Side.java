package chariot.chess;

public enum Side {
    white, black;

    public Side other() {
        return switch(this) {
            case white -> black;
            case black -> white;
        };
    }
}
