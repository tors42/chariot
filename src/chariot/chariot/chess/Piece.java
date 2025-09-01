package chariot.chess;

public enum Piece implements PieceType {
    pawn,
    knight,
    bishop,
    rook,
    queen,
    king;

    public PieceAndSide withSide(Side side) { return new PieceAndSide(this, side); }

    public static Piece fromChar(char c) {
        return switch(Character.toLowerCase(c)) {
            case 'p' -> pawn;
            case 'n' -> knight;
            case 'b' -> bishop;
            case 'r' -> rook;
            case 'q' -> queen;
            case 'k' -> king;
            default -> null;
        };
    }

    public char toChar() { return toChar(Side.black); }

    public char toChar(Side side) {
        char c = switch(this) {
            case pawn -> 'p';
            case knight -> 'n';
            case bishop -> 'b';
            case rook -> 'r';
            case queen -> 'q';
            case king -> 'k';
        };
        return side == Side.black
            ? c
            : Character.toUpperCase(c);
    }

    public record PieceAndSide(Piece piece, Side side) {}
    public static PieceAndSide fromCharWithSide(char c) {
        return fromChar(c) instanceof Piece piece
            ? piece.withSide(Character.isLowerCase(c) ? Side.black : Side.white)
            : null;
    }
}
