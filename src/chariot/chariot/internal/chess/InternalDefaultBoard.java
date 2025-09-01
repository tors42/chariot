package chariot.internal.chess;

import module java.base;
import module chariot;

/**
 * DefaultStandardBoard is the internal implementation of StandardBoard.
 * StandardBoard exists to provide a user-friendly typed API which allows for
 * making moves and querying about pieces.
 * StandardBoard wraps a `Board delegate`,
 * allowing for the chess logic to come from user-provided code via BoardProvider SPI.
 * To begin with, a NaiveChess implementation will be provided by chariot.
 *
 * NaiveChess will internally use the same piece representation
 * that StandardBoard provides in its typed API (a non-naive solution would be
 * using bitboards).
 *
 * So the delegate knows the current FEN and how FEN is modified given a move.
 * StandardBoard knows how to translate FEN to typed API and from typed API to FEN.
 */
public record InternalDefaultBoard(
        Board delegate,
        List<Piece.PieceAndSide> captures,
        String initialFEN,
        List<MoveAndFen> history
        ) implements DefaultBoard {

    record MoveAndFen(String move, String fen) {}

    public static InternalDefaultBoard of(Board board) {
        return new InternalDefaultBoard(board, List.of(), board.toFEN(), List.of());
    }

    @Override
    public DefaultBoard play(String... uciOrSan) {
        var moves = delegate().asMoves(uciOrSan);
        InternalDefaultBoard board = this;
        for (Move move : moves) {
            String uci = board.toUCI(move.asString());
            board = board.playUCI(uci);
        }
        return board;
    }

    InternalDefaultBoard playUCI(String uci) {
        Board next = delegate.play(uci);
        if (delegate.toFEN().equals(next.toFEN())) return this;

        var withCaptures = captures();
        Square.Pos to = Square.pos(uci.substring(2,4));
        if (DefaultBoard.fenPositionsToSquares(delegate.toFEN()).get(to) instanceof Square.With(_, Piece piece, Side side)) {
            withCaptures = Stream.concat(withCaptures.stream(), Stream.of(piece.withSide(side))).toList();
        }

        var withHistory = Stream.concat(history().stream(), Stream.of(new MoveAndFen(uci, next.toFEN()))).toList();
        return new InternalDefaultBoard(next, withCaptures, initialFEN(), withHistory);
    }

    // delegate...
    @Override public String toSAN(String move)          { return delegate.toSAN(move); }
    @Override public String toUCI(String move)          { return delegate.toUCI(move); }
    @Override public String toFEN()                     { return delegate.toFEN(); }
    @Override public String variant()                   { return delegate.variant(); }
    @Override public Collection<String> validMoves()    { return delegate.validMoves(); }
    @Override public Side sideToMove()                  { return delegate.sideToMove(); }
    // ...delegate


    @Override
    public List<String> historyFEN() {
        return Stream.concat(Stream.of(initialFEN()), history().stream().map(MoveAndFen::fen)).toList();
    }

    @Override
    public List<String> historyMove() {
        return history().stream().map(MoveAndFen::move).toList();
    }

    @Override
    public Squares<Piece> squares() {
        Map<Square.Pos, Square<Piece>> squares = DefaultBoard.fenPositionsToSquares(delegate.toFEN());
        return new Squares<Piece>() {
            @Override
            public Square<Piece> get(Square.Pos pos) {
                return squares.get(pos);
            }

            @Override
            public List<Square<Piece>> all() {
                return squares.values().stream().toList();
            }
        };
    }

    @Override
    public Pieces<Piece> pieces() {
        Map<Square.Pos, Square<Piece>> squares = DefaultBoard.fenPositionsToSquares(delegate.toFEN());
        return new Pieces<Piece>() {
            @Override
            public List<Square.With<Piece>> all() {
                return squares.values().stream().filter(s -> s instanceof Square.With).map(s -> (Square.With<Piece>) s).toList();
            }

            @Override
            public List<Square.With<Piece>> all(Side side) {
                return all().stream().filter(p -> p.side() == side).toList();
            }

            @Override
            public List<Square.With<Piece>> of(Piece type) {
                return all().stream().filter(p -> p.type() == type).toList();
            }

            @Override
            public List<Square.With<Piece>> of(Piece type, Side side) {
                return of(type).stream().filter(p -> p.side() == side).toList();
            }

            @Override
            public List<Piece> captured(Side side) {
                return captures().stream().filter(pieceWithSide -> pieceWithSide.side() == side).map(Piece.PieceAndSide::piece).toList();
            }
        };
    }
}
