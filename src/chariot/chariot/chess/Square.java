package chariot.chess;

public sealed interface Square<T extends PieceType> permits
    Square.Empty,
    Square.With {

    record Empty<T extends PieceType>(Pos pos) implements Square<T> {}
    record With<T extends PieceType>(Pos pos, T type, Side side) implements Square<T> {}

    sealed interface Pos {
        char file();
        int rank();
        default Pos withFile(char newFile) { return Square.pos(newFile, rank()); }
        default Pos withRank(int newRank)  { return Square.pos(file(), newRank); }

        default Pos delta(int deltaRank, int deltaFile) {
            return withRank(rank() + deltaRank)
                .withFile((char) (file() + deltaFile));
        }
    }

    Pos pos();
    default char file() { return pos().file(); }
    default int rank()  { return pos().rank(); }

    record FileRank(char file, int rank) implements Pos {
        @Override public String toString() { return "%s%s".formatted(file, rank); }
    }

    static <T extends PieceType> Square.Empty<T> empty(char file, int rank) {
        return empty(Square.pos(file, rank));
    }

    static <T extends PieceType> Square.Empty<T> empty(Square<T> square) {
        return new Empty<>(square.pos());
    }

    static <T extends PieceType> Square.Empty<T> empty(Pos pos) {
        return new Empty<>(pos);
    }

    static <T extends PieceType> Square.With<T> withPiece(char file, int rank, T type, Side side) {
        return withPiece(Square.pos(file, rank), type, side);
    }

    static <T extends PieceType> Square.With<T> withPiece(Square<T> square, T type, Side side) {
        return withPiece(square.pos(), type, side);
    }

    static <T extends PieceType> Square.With<T> withPiece(Pos pos, T type, Side side) {
        return new With<>(pos, type, side);
    }

    static Pos pos(String fileRank) {
        return pos(fileRank.charAt(0), Character.getNumericValue(fileRank.charAt(1)));
    }

    static Pos pos(char file, int rank) {
        return new FileRank(file, rank);
    }

    static final Pos a1 = new FileRank('a', 1);
    static final Pos a2 = new FileRank('a', 2);
    static final Pos a3 = new FileRank('a', 3);
    static final Pos a4 = new FileRank('a', 4);
    static final Pos a5 = new FileRank('a', 5);
    static final Pos a6 = new FileRank('a', 6);
    static final Pos a7 = new FileRank('a', 7);
    static final Pos a8 = new FileRank('a', 8);

    static final Pos b1 = new FileRank('b', 1);
    static final Pos b2 = new FileRank('b', 2);
    static final Pos b3 = new FileRank('b', 3);
    static final Pos b4 = new FileRank('b', 4);
    static final Pos b5 = new FileRank('b', 5);
    static final Pos b6 = new FileRank('b', 6);
    static final Pos b7 = new FileRank('b', 7);
    static final Pos b8 = new FileRank('b', 8);

    static final Pos c1 = new FileRank('c', 1);
    static final Pos c2 = new FileRank('c', 2);
    static final Pos c3 = new FileRank('c', 3);
    static final Pos c4 = new FileRank('c', 4);
    static final Pos c5 = new FileRank('c', 5);
    static final Pos c6 = new FileRank('c', 6);
    static final Pos c7 = new FileRank('c', 7);
    static final Pos c8 = new FileRank('c', 8);

    static final Pos d1 = new FileRank('d', 1);
    static final Pos d2 = new FileRank('d', 2);
    static final Pos d3 = new FileRank('d', 3);
    static final Pos d4 = new FileRank('d', 4);
    static final Pos d5 = new FileRank('d', 5);
    static final Pos d6 = new FileRank('d', 6);
    static final Pos d7 = new FileRank('d', 7);
    static final Pos d8 = new FileRank('d', 8);

    static final Pos e1 = new FileRank('e', 1);
    static final Pos e2 = new FileRank('e', 2);
    static final Pos e3 = new FileRank('e', 3);
    static final Pos e4 = new FileRank('e', 4);
    static final Pos e5 = new FileRank('e', 5);
    static final Pos e6 = new FileRank('e', 6);
    static final Pos e7 = new FileRank('e', 7);
    static final Pos e8 = new FileRank('e', 8);

    static final Pos f1 = new FileRank('f', 1);
    static final Pos f2 = new FileRank('f', 2);
    static final Pos f3 = new FileRank('f', 3);
    static final Pos f4 = new FileRank('f', 4);
    static final Pos f5 = new FileRank('f', 5);
    static final Pos f6 = new FileRank('f', 6);
    static final Pos f7 = new FileRank('f', 7);
    static final Pos f8 = new FileRank('f', 8);

    static final Pos g1 = new FileRank('g', 1);
    static final Pos g2 = new FileRank('g', 2);
    static final Pos g3 = new FileRank('g', 3);
    static final Pos g4 = new FileRank('g', 4);
    static final Pos g5 = new FileRank('g', 5);
    static final Pos g6 = new FileRank('g', 6);
    static final Pos g7 = new FileRank('g', 7);
    static final Pos g8 = new FileRank('g', 8);

    static final Pos h1 = new FileRank('h', 1);
    static final Pos h2 = new FileRank('h', 2);
    static final Pos h3 = new FileRank('h', 3);
    static final Pos h4 = new FileRank('h', 4);
    static final Pos h5 = new FileRank('h', 5);
    static final Pos h6 = new FileRank('h', 6);
    static final Pos h7 = new FileRank('h', 7);
    static final Pos h8 = new FileRank('h', 8);

}
