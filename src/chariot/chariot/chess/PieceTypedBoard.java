package chariot.chess;

import module java.base;

public interface PieceTypedBoard<T extends PieceType> {

    Pieces<T> pieces();
    Squares<T> squares();

    default Square<T> get(Square.Pos pos) { return squares().get(pos); }

    interface Squares<T extends PieceType> {
        List<Square<T>> all();
        Square<T> get(Square.Pos pos);
    }

    interface Pieces<T extends PieceType> {
        List<Square.With<T>> all();
        List<Square.With<T>> all(Side side);
        List<Square.With<T>> of(T type);
        List<Square.With<T>> of(T type, Side side);
        List<T> captured(Side side);
    }
}
