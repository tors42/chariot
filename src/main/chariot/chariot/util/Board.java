package chariot.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

public sealed interface Board {

    static Board fromStandardPosition() {
        return fromFEN(FEN.standard);
    }

    static Board fromFEN(String fen) {
        return fromFEN(FEN.parse(fen));
    }

    static Board fromFEN(FEN fen) {
        return fromFENWithHistory(fen, List.of());
    }

    private static Board fromFENWithHistory(FEN fen, List<FEN> history) {

        GameState gameState = GameState.ongoing;

        if (fen.halfMoveClock() >= 100) gameState = GameState.draw_by_fifty_move_rule;

        if (gameState == GameState.ongoing) {
            if (Board.validMoves(fen).isEmpty()) {
                var pieceMap = Board.pieceMap(fen.positions());
                Coordinate kingCoodinate = pieceMap.entrySet().stream()
                    .filter(entry -> entry.getValue().type() == PieceType.KING
                                  && entry.getValue().color() == fen.whoseTurn())
                    .map(entry -> entry.getKey())
                    .findAny()
                    .orElse(Coordinate.rowCol(-1,-1));

                gameState = Board.isCoordinateAttacked(kingCoodinate, fen.whoseTurn().other(), pieceMap) ?
                    GameState.checkmate : GameState.stalemate;
            }
        }

        if (gameState == GameState.ongoing) {
            record PositionAndMove(String position, int move) {}
            if (Stream.concat(Stream.of(fen), history.stream())
                .map(f -> new PositionAndMove(f.positions(), f.move()))
                .distinct()
                .collect(Collectors.groupingBy(PositionAndMove::position, Collectors.counting()))
                .values().stream().anyMatch(count -> count >= 3))
            {
                gameState = GameState.draw_by_threefold_repetition;
            }
        }

        var bd = new BoardData(pieceMap(fen.positions()), fen, history, gameState);
        return bd;
    }

    String toFEN();
    String to960FEN();
    String toStandardFEN();
    Set<Move> validMoves();
    Board play(String move);

    default boolean ended() {
        return this instanceof BoardData board && board.gameState() != GameState.ongoing;
    }

    default boolean whiteToMove() {
        return !blackToMove();
    }
    default boolean blackToMove() {
        return this instanceof BoardData board && board.fen().whoseTurn() == Side.BLACK;
    }

    default String toString(Consumer<ConsoleRenderer.Config> config) {
        return ConsoleRenderer.render(this, config);
    }

    enum PieceType {
        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
        char toChar() {
            return switch(this) {
                case PAWN   -> 'P';
                case KNIGHT -> 'N';
                case BISHOP -> 'B';
                case ROOK   -> 'R';
                case QUEEN  -> 'Q';
                case KING   -> 'K';
            };
        }
    }
    enum Side {
        BLACK, WHITE;
        Side other() { return this == BLACK ? WHITE : BLACK; }
        char toChar() { return this == BLACK ? 'b' : 'w'; }
    }

    enum GameState {
        ongoing,
        draw_by_threefold_repetition,
        draw_by_fifty_move_rule,
        stalemate,
        checkmate
    }

    record BoardData(Map<Coordinate, Piece> pieceMap, FEN fen, List<FEN> history, GameState gameState) implements Board {

        public BoardData {
            pieceMap = Map.copyOf(pieceMap);
            history = List.copyOf(history);
        }

        @Override
        public Piece get(Coordinate coordinate) {
            return pieceMap.get(coordinate);
        }

        @Override public String toFEN() { return fen().toString(); }
        @Override public String to960FEN() { return fen().as960(); }
        @Override public String toStandardFEN() { return fen().asStandard(); }

        @Override
        public Set<Move> validMoves() {
            return Board.validMoves(fen(), pieceMap());
        }

        @Override
        public String toString() {
            return ConsoleRenderer.render(this);
        }

        @Override
        public Board play(String move) {
            if (move.contains(" ")) {
                return playMultipleMoves(Arrays.stream(move.split(" ")));
            } else {
                return playSingleMove(Move.parse(move, fen));
            }
        }

        private Board playMultipleMoves(Stream<String> moves) {
            return moves.reduce(
                    (Board)this,
                    (board, move) -> board.play(move),
                    (__, ___) -> __);
        }

        private Board playSingleMove(Move move) {
            if (ended()) {
                return this;
            }

            FEN nextFEN = fen();

            //// positions
            //
            Map<Coordinate, Piece> afterMove = new HashMap<>(pieceMap);

            boolean resetHalfMoveClock = false;

            String ep = "-";
            if (move instanceof FromTo fromTo) {
                Piece piece = afterMove.remove(fromTo.from());
                Piece capture = afterMove.put(fromTo.to(), piece);

                switch (piece.type()) {
                    case PAWN -> {
                        // capture
                        if (fromTo.from().col() != fromTo.to().col()) {
                            if (capture == null) {
                                // Aha! Must have been en passant!
                                afterMove.remove(Coordinate.rowCol(fromTo.from().row(), fromTo.to().col()));
                            }
                        }

                        resetHalfMoveClock = true;
                        if (piece.color() == Side.BLACK) {
                            if (fromTo.from().row() == 6 && fromTo.to().row() == 4) {
                                Piece leftPawn = pieceMap().get(Coordinate.rowCol(4, fromTo.from().col() - 1));
                                Piece rightPawn = pieceMap().get(Coordinate.rowCol(4, fromTo.from().col() + 1));
                                if (leftPawn != null && leftPawn.type() == PieceType.PAWN && leftPawn.color() == Side.WHITE) {
                                    ep = Coordinate.rowCol(5, fromTo.from().col()).name();
                                } else if (rightPawn != null && rightPawn.type() == PieceType.PAWN && rightPawn.color() == Side.WHITE) {
                                    ep = Coordinate.rowCol(5, fromTo.from().col()).name();
                                }
                            }
                        } else {
                            if (fromTo.from().row() == 1 && fromTo.to().row() == 3) {
                                Piece leftPawn = pieceMap().get(Coordinate.rowCol(3, fromTo.from().col() - 1));
                                Piece rightPawn = pieceMap().get(Coordinate.rowCol(3, fromTo.from().col() + 1));
                                if (leftPawn != null && leftPawn.type() == PieceType.PAWN && leftPawn.color() == Side.BLACK) {
                                    ep = Coordinate.rowCol(2, fromTo.from().col()).name();
                                } else if (rightPawn != null && rightPawn.type() == PieceType.PAWN && rightPawn.color() == Side.BLACK) {
                                    ep = Coordinate.rowCol(2, fromTo.from().col()).name();
                                }
                            }
                         }
                    }
                    case KING -> {
                        CastlingRights rights = fen().castlingRights().withoutQueenSide(piece.color()).withoutKingSide(piece.color());
                        nextFEN = nextFEN.with(new FEN.Castling(rights));
                    }
                    case ROOK -> {
                        CastlingRights rights = fen().castlingRights();

                        String lowSideCastlingFile = piece.color() == Side.WHITE ? rights.files().Q() : rights.files().q();
                        if (! lowSideCastlingFile.isEmpty()) {
                            if (fromTo.from().equals(Coordinate.name(lowSideCastlingFile + (piece.color() == Side.WHITE ? "1" : "8")))) {
                                rights = rights.withoutQueenSide(piece.color());
                            }
                        }

                        String highSideCastlingFile = piece.color() == Side.WHITE ? rights.files().K() : rights.files().k();
                        if (! highSideCastlingFile.isEmpty()) {
                            if (fromTo.from().equals(Coordinate.name(highSideCastlingFile + (piece.color() == Side.WHITE ? "1" : "8")))) {
                                rights = rights.withoutKingSide(piece.color());
                            }
                        }
                        nextFEN = nextFEN.with(new FEN.Castling(rights));
                    }
                    case BISHOP, KNIGHT, QUEEN -> {}
                };

                if (capture != null) {
                    resetHalfMoveClock = true;
                }
            } else if (move instanceof Promotion promotion) {
                Piece pawn = afterMove.remove(promotion.pawn().from());
                Coordinate to = promotion.pawn().to();
                Piece piece = Piece.piece(promotion.piece(), pawn.color());
                @SuppressWarnings("unused")
                Piece capture = afterMove.put(to, piece);

                // promotion is pawn move, no need ot check if capture is null or not...
                resetHalfMoveClock = true;

            } else if (move instanceof Castling castling) {
                Piece king = afterMove.remove(castling.king().from());
                Piece rook = afterMove.remove(castling.rook().from());

                @SuppressWarnings("unused")
                Piece empty1 = afterMove.put(castling.king().to(), king);
                @SuppressWarnings("unused")
                Piece empty2 = afterMove.put(castling.rook().to(), rook);

                nextFEN = nextFEN.with(new FEN.Castling(fen().castlingRights().withoutQueenSide(king.color()).withoutKingSide(king.color())));
            } else if (move instanceof Invalid invalid) {
                System.err.println("Invalid move: " + invalid);
                return this;
            }
            //
            ////

            nextFEN = nextFEN.with(new FEN.Positions(positions(afterMove)));

            // whose turn
            nextFEN = nextFEN.with(new FEN.WhoseTurn(fen().whoseTurn().other()));

            // en passant
            nextFEN = nextFEN.with(new FEN.EP(ep));

            // half move
            if (resetHalfMoveClock) {
                nextFEN = nextFEN.with(new FEN.HalfMoveClock(0));
            } else {
                nextFEN = nextFEN.with(new FEN.HalfMoveClock(fen().halfMoveClock() + 1));
            }

            // move
            if (fen().whoseTurn() == Side.BLACK) nextFEN = nextFEN.with(new FEN.Move(fen().move() + 1));

            var newHistory = new ArrayList<>(history);
            newHistory.add(fen());

            var nextBoard = Board.fromFENWithHistory(nextFEN, List.copyOf(newHistory));

            return nextBoard;
        }


        @Override
        public String toSAN(String moves) {
            if (moves.contains(" ")) {
                return String.join(" ", multipleMovesToSAN(moves.split(" ")));
            } else {
                return singleMoveToSAN(moves);
            }
        }

        @Override
        public String toSAN(String... moves) {
            return String.join(" ", multipleMovesToSAN(moves));
        }

        @Override
        public List<String> toSAN(List<String> moves) {
            return multipleMovesToSAN(moves.stream());
        }

        private List<String> multipleMovesToSAN(String[] moves) {
            return multipleMovesToSAN(Arrays.stream(moves));
        }

        private List<String> multipleMovesToSAN(Stream<String> moves) {
            record BoardMoves(Board board, List<String> sans) {}
            var boardMoves = moves.reduce(new BoardMoves(this, new ArrayList<>()),
                    (bm, move) -> {
                        bm.sans().add(bm.board().toSAN(move));
                        return new BoardMoves(bm.board().play(move), bm.sans());
                    },
                    (__, ___) -> __);
            return boardMoves.sans();
        }

        private String singleMoveToSAN(String move) {
            return toSAN(Move.parse(move, fen));
        }

        public String toSAN(Move move) {
            if (move instanceof Invalid invalid) return "";

            // todo, make it possible to ask to not include the check/checkmate symbol?
            final String checkSymbol;
            if (playSingleMove(move) instanceof BoardData boardIfPlayed) {
                if (boardIfPlayed.gameState() == GameState.checkmate) {
                    checkSymbol = "#";
                } else {
                    var inCheck = boardIfPlayed.pieceMap().entrySet().stream()
                        .filter(entry -> entry.getValue().type() == PieceType.KING
                                && entry.getValue().color() == fen().whoseTurn().other())
                        .findFirst()
                        .map(king -> Board.isCoordinateAttacked(king.getKey(), fen().whoseTurn() , boardIfPlayed.pieceMap()))
                        .orElse(false);
                    checkSymbol = inCheck ? "+" : "";
                }
            } else {
                checkSymbol = "";
            }

            if (move instanceof FromTo fromTo) {
                Piece piece = pieceMap().get(fromTo.from());
                if (piece == null) return "-";
                if (piece.color() != fen().whoseTurn()) return "-";

                if (piece.type() == PieceType.PAWN) {
                    if (pieceMap().containsKey(fromTo.to()) || fromTo.from().col() != fromTo.to().col()) {
                        String file = fromTo.from().name().substring(0,1);
                        String capture = "x";
                        String destination = fromTo.to().name(); // make it possible to specify format? "ed", "exd", "exd6", "exd6 e.p"
                        String san = "%s%s%s%s".formatted(file, capture, destination, checkSymbol);
                        return san;
                    } else {
                        String destination = fromTo.to().name();
                        String san = "%s%s".formatted(destination, checkSymbol);
                        return san;
                    }
                } else if(piece.type() == PieceType.KING) {
                    String letter = "K";
                    String capture = pieceMap().containsKey(fromTo.to()) ? "x" : "";
                    String destination = fromTo.to().name();
                    String san = "%s%s%s%s".formatted(letter, capture, destination, checkSymbol);
                    return san;
                } else {
                    List<Map.Entry<Coordinate, Piece>> disambiguation = pieceMap().entrySet().stream()
                        .filter(entry -> entry.getValue().type() == piece.type() && entry.getValue().color() == piece.color())
                        .filter(entry -> entry.getValue() != piece)
                        .filter(entry -> Board.coordinatesAttackedByPiece(entry.getKey(), pieceMap()).contains(fromTo.to()))
                        .toList();

                    record Unique(boolean file, boolean rank) {}
                    var unique = disambiguation.stream().reduce(
                            new Unique(true, true),
                            (a, e) -> new Unique(a.file() && e.getKey().col() != fromTo.from().col(),
                                a.rank() && e.getKey().row() != fromTo.from().row()),
                            (u1, u2) -> new Unique(u1.file() && u2.file(), u1.rank() && u2.rank()));

                    String letter = piece.letter().toUpperCase();

                    final String dis;
                    if (unique.file() && unique.rank()) {
                        dis = "";
                    } else if (unique.file()) {
                        dis = fromTo.from().name().substring(1,2);
                    } else if (unique.rank()) {
                        dis = fromTo.from().name().substring(0,1);
                    } else {
                        dis = fromTo.from().name();
                    }

                    String capture = pieceMap().containsKey(fromTo.to()) ? "x" : "";
                    String destination = fromTo.to().name();

                    String san = "%s%s%s%s%s".formatted(letter, dis, capture, destination, checkSymbol);
                    return san;
                }
            }

            if (move instanceof Castling castling) {
                String castleside = castling.king().from().col() < castling.rook().from().col() ?
                    "O-O" : "O-O-O";
                String san = "%s%s".formatted(castleside, checkSymbol);
                return san;
            }

            if (move instanceof Promotion promotion) {
                if (pieceMap().containsKey(promotion.pawn().to())) {
                    // bxa8=R
                    String file = promotion.pawn().from().name().substring(0,1);
                    String capture = "x";
                    String destination = promotion.pawn().to().name();
                    String prom = "=" + promotion.piece().toChar();
                    String san = "%s%s%s%s%s".formatted(file, capture, destination, prom, checkSymbol);
                    return san;
                } else {
                    // a8=Q
                    String destination = promotion.pawn().to().name();
                    String prom = "=" + promotion.piece().toChar();
                    String san = "%s%s%s".formatted(destination, prom, checkSymbol);
                    return san;
                }
            }

            return "";
        }

    }

    record RowCol(int row, int col) implements Coordinate {
        @Override public String toString() { return name(); }
    }

    sealed interface Coordinate {
        int row();
        int col();

        default String name() {
            return Character.toString('a' + col()) + String.valueOf(row()+1);
        }

        static Coordinate rowCol(int row, int col) {
            return new RowCol(row, col);
        }

        static Coordinate name(String name) {
            return rowCol(
                    Character.getNumericValue(name.charAt(1))-1,
                    Character.getNumericValue('7' - ('h' - name.toLowerCase().charAt(0)))
                    );
        }
    }

    record StandardPiece(PieceType type, Side color) implements Piece {}

    sealed interface Piece {
        PieceType type();
        Side color();

        static Piece piece(PieceType type, Side color) { return new StandardPiece(type, color); }

        static Piece parse(char c) {
            return switch(c) {
                case 'p' -> piece(PieceType.PAWN,   Side.BLACK);
                case 'n' -> piece(PieceType.KNIGHT, Side.BLACK);
                case 'b' -> piece(PieceType.BISHOP, Side.BLACK);
                case 'r' -> piece(PieceType.ROOK,   Side.BLACK);
                case 'q' -> piece(PieceType.QUEEN,  Side.BLACK);
                case 'k' -> piece(PieceType.KING,   Side.BLACK);

                case 'P' -> piece(PieceType.PAWN,   Side.WHITE);
                case 'N' -> piece(PieceType.KNIGHT, Side.WHITE);
                case 'B' -> piece(PieceType.BISHOP, Side.WHITE);
                case 'R' -> piece(PieceType.ROOK,   Side.WHITE);
                case 'Q' -> piece(PieceType.QUEEN,  Side.WHITE);
                case 'K' -> piece(PieceType.KING,   Side.WHITE);
                default -> throw new RuntimeException("Unknown piece " + c);
             };
        }

        default String letter() {
            return switch(type()) {
                case PAWN   -> color() == Side.BLACK ? "p" : "P";
                case KNIGHT -> color() == Side.BLACK ? "n" : "N";
                case BISHOP -> color() == Side.BLACK ? "b" : "B";
                case ROOK   -> color() == Side.BLACK ? "r" : "R";
                case QUEEN  -> color() == Side.BLACK ? "q" : "Q";
                case KING   -> color() == Side.BLACK ? "k" : "K";
            };
        }

        default String unicode() {
            return switch(type()) {
                case PAWN   -> color() == Side.BLACK ? "♟" : "♙";
                case KNIGHT -> color() == Side.BLACK ? "♞" : "♘";
                case BISHOP -> color() == Side.BLACK ? "♝" : "♗";
                case ROOK   -> color() == Side.BLACK ? "♜" : "♖";
                case QUEEN  -> color() == Side.BLACK ? "♛" : "♕";
                case KING   -> color() == Side.BLACK ? "♚" : "♔";
            };
        }
    }

    Piece get(Coordinate coordinate);
    default Piece get(int row, int col) { return get(Coordinate.rowCol(row, col)); }
    default Piece get(String coordinate) { return get(Coordinate.name(coordinate)); }

    sealed interface CastlingRights {

        record Standard(RookFiles files) implements CastlingRights {
            @Override public String toString() { return asStandard(); }
        }

        record Chess960(RookFiles files) implements CastlingRights {
            @Override public String toString() { return as960(); }
        }

        default CastlingRights withoutKingSide(Side side) {
            RookFiles without = new RookFiles(
                    side == Side.WHITE ? "" : files().K(),
                    files().Q(),
                    side == Side.WHITE ? files().k() : "",
                    files().q());
            if (this instanceof Standard) {
                return new Standard(without);
            } else {
                return new Chess960(without);
            }
        }

        default CastlingRights withoutQueenSide(Side side) {
            RookFiles without = new RookFiles(
                    files().K(),
                    side == Side.WHITE ? "" : files().Q(),
                    files().k(),
                    side == Side.WHITE ? files().q() : ""
                    );
            if (this instanceof Standard) {
                return new Standard(without);
            } else {
                return new Chess960(without);
            }
        }

        record RookFiles(String K, String Q, String k, String q) {}

        static CastlingRights parse(String rights, String positions) {
            var map = pieceMap(positions);
            var kingEntries = map.entrySet().stream().filter(entry -> entry.getValue().type() == PieceType.KING).toList();
            char whiteKingFile = kingEntries.stream().filter(entry -> entry.getValue().color() == Side.WHITE)
                .map(entry -> entry.getKey().name().charAt(0))
                .findAny()
                .orElse('e');
            char blackKingFile = kingEntries.stream().filter(entry -> entry.getValue().color() == Side.BLACK)
                .map(entry -> entry.getKey().name().charAt(0))
                .findAny()
                .orElse('e');

            var rookEntries = map.entrySet().stream().filter(entry -> entry.getValue().type() == PieceType.ROOK).toList();
            char whiteHighRookFile = rookEntries.stream().filter(entry -> entry.getValue().color() == Side.WHITE)
                .map(entry -> entry.getKey().name().charAt(0))
                .filter(file -> file > whiteKingFile)
                .findAny()
                .orElse('h');
            char whiteLowRookFile = rookEntries.stream().filter(entry -> entry.getValue().color() == Side.WHITE)
                .map(entry -> entry.getKey().name().charAt(0))
                .filter(file -> file < whiteKingFile)
                .findAny()
                .orElse('a');
            char blackHighRookFile = rookEntries.stream().filter(entry -> entry.getValue().color() == Side.BLACK)
                .map(entry -> entry.getKey().name().charAt(0))
                .filter(file -> file > blackKingFile)
                .findAny()
                .orElse('h');
            char blackLowRookFile = rookEntries.stream().filter(entry -> entry.getValue().color() == Side.BLACK)
                .map(entry -> entry.getKey().name().charAt(0))
                .filter(file -> file < blackKingFile)
                .findAny()
                .orElse('a');

            return parse(rights, whiteKingFile, blackKingFile, whiteHighRookFile, whiteLowRookFile, blackHighRookFile, blackLowRookFile);
        }

        static CastlingRights parse(String rights) {
            return parse(rights, 'e', 'e', 'h', 'a', 'h', 'a');
        }

        private static CastlingRights parse(String rights, char whiteKingFile, char blackKingFile, char whiteHighRookFile, char whiteLowRookFile, char blackHighRookFile, char blackLowRookFile) {
            if (rights.isEmpty() || rights.equals("-")) {
                return new Standard(new RookFiles("", "", "", ""));
            }

            if ("abcdefgh".chars().anyMatch(i -> rights.toLowerCase().contains(Character.toString(i)))) {
                String whiteHighFileSide = "abcdefgh".toUpperCase().chars().filter(file -> file > Character.toUpperCase(whiteKingFile))
                    .mapToObj(Character::toString)
                    .filter(rights::contains)
                    .findAny()
                    .orElse("");
                String whiteLowFileSide = "abcdefgh".toUpperCase().chars().filter(file -> file < Character.toUpperCase(whiteKingFile))
                    .mapToObj(Character::toString)
                    .filter(rights::contains)
                    .findAny()
                    .orElse("");
                String blackHighFileSide = "abcdefgh".chars().filter(file -> file > blackKingFile)
                    .mapToObj(Character::toString)
                    .filter(rights::contains)
                    .findAny()
                    .orElse("");
                String blackLowFileSide = "abcdefgh".chars().filter(file -> file < blackKingFile)
                    .mapToObj(Character::toString)
                    .filter(rights::contains)
                    .findAny()
                    .orElse("");
                return new Chess960(new RookFiles(whiteHighFileSide, whiteLowFileSide, blackHighFileSide, blackLowFileSide));
            } else {
                String whiteHighFileSide = rights.contains("K") ? Character.toString(whiteHighRookFile).toUpperCase() : "";
                String whiteLowFileSide = rights.contains("Q") ? Character.toString(whiteLowRookFile).toUpperCase() : "";
                String blackHighFileSide = rights.contains("k") ? Character.toString(blackHighRookFile) : "";
                String blackLowFileSide = rights.contains("q") ? Character.toString(blackLowRookFile) : "";
                return new Standard(new RookFiles(whiteHighFileSide, whiteLowFileSide, blackHighFileSide, blackLowFileSide));
            }
        }

        RookFiles files();

        default String asStandard() {
            return dashIfEmpty(String.join("",
                    files().K.isEmpty() ? "" : "K",
                    files().Q().isEmpty() ? "" : "Q",
                    files().k().isEmpty() ? "" : "k",
                    files().q().isEmpty() ? "" : "q"
                    ));
        }

        default String as960() {
            return dashIfEmpty(String.join("",
                    files().K,
                    files().Q(),
                    files().k(),
                    files().q()
                    ));
         }

        static String dashIfEmpty(String rights) {
            return rights.isEmpty() ? "-" : rights;
        }

        default Set<Castling> validCastlingMoves(
                Coordinate kingFromCoordinate,
                AttackedCoordinatePredicate attackedCoordinatePredicate,
                Map<Coordinate, Piece> pieceMap) {

            Piece king = pieceMap.get(kingFromCoordinate);
            if (king == null) return Set.of();

            if (attackedCoordinatePredicate.test(kingFromCoordinate, king.color().other(), pieceMap)) {
                return Set.of();
            }

            Set<Castling> castlingMoves = new HashSet<>(2);

            var kingTargetSquareKingSide = Coordinate.name("g" + (king.color() == Side.WHITE ? "1" : "8"));
            var rookTargetSquareKingSide = Coordinate.name("f" + (king.color() == Side.WHITE ? "1" : "8"));

            var kingTargetSquareQueenSide  = Coordinate.name("c" + (king.color() == Side.WHITE ? "1" : "8"));
            var rookTargetSquareQueenSide  = Coordinate.name("d" + (king.color() == Side.WHITE ? "1" : "8"));

            Coordinate kingSideRook = king.color() == Side.WHITE ?
                ("".equals(files().K()) ? null : Coordinate.name(files().K().toLowerCase() + "1")) :
                ("".equals(files().k()) ? null : Coordinate.name(files().k().toLowerCase() + "8"));

            if (kingSideRook != null) {
                Piece rook = pieceMap.get(kingSideRook);
                if (rook != null && rook.type() == PieceType.ROOK && rook.color() == king.color()) {
                    Set<Coordinate> rookTravelSquares = new HashSet<>(8);
                    int rookDistance = kingSideRook.col() - rookTargetSquareKingSide.col();
                    for (int i = rookDistance; i != 0; i = i - (rookDistance > 0 ? 1 : -1)) {
                        rookTravelSquares.add(Coordinate.rowCol(kingSideRook.row(), rookTargetSquareKingSide.col() + i));
                    }
                    Set<Coordinate> kingTravelSquares = new HashSet<>(8);
                    int kingDistance = kingFromCoordinate.col() - kingTargetSquareKingSide.col();
                    for (int i = kingDistance; i != 0; i = i - (kingDistance > 0 ? 1 : -1)) {
                        kingTravelSquares.add(Coordinate.rowCol(kingFromCoordinate.row(), kingTargetSquareKingSide.col() + i));
                    }

                    rookTravelSquares.remove(kingFromCoordinate);
                    rookTravelSquares.remove(kingSideRook);
                    kingTravelSquares.remove(kingFromCoordinate);
                    kingTravelSquares.remove(kingSideRook);
                    if (rookTravelSquares.stream().noneMatch(c -> pieceMap.containsKey(c)) &&
                        kingTravelSquares.stream().noneMatch(c -> pieceMap.containsKey(c))) {
                        if (kingTravelSquares.stream().noneMatch(
                                    c -> attackedCoordinatePredicate.test(c, king.color().other(), pieceMap))) {
                            castlingMoves.add(new Castling(
                                        new FromTo(kingFromCoordinate, kingTargetSquareKingSide),
                                        new FromTo(kingSideRook, rookTargetSquareKingSide)));
                        }

                    }
                }
            }

            Coordinate queenSideRook = king.color() == Side.WHITE ?
                ("".equals(files().Q()) ? null : Coordinate.name(files().Q().toLowerCase() + "1")) :
                ("".equals(files().q()) ? null : Coordinate.name(files().q().toLowerCase() + "8"));

            if (queenSideRook != null) {
                Piece rook = pieceMap.get(queenSideRook);
                if (rook != null && rook.type() == PieceType.ROOK && rook.color() == king.color()) {
                    Set<Coordinate> rookTravelSquares = new HashSet<>(8);
                    int rookDistance = queenSideRook.col() - rookTargetSquareQueenSide.col();
                    for (int i = rookDistance; i != 0; i = i - (rookDistance > 0 ? 1 : -1)) {
                        rookTravelSquares.add(Coordinate.rowCol(queenSideRook.row(), rookTargetSquareQueenSide.col() + i));
                    }
                    Set<Coordinate> kingTravelSquares = new HashSet<>(8);
                    int kingDistance = kingFromCoordinate.col() - kingTargetSquareQueenSide.col();
                    for (int i = kingDistance; i != 0; i = i - (kingDistance > 0 ? 1 : -1)) {
                        kingTravelSquares.add(Coordinate.rowCol(kingFromCoordinate.row(), kingTargetSquareQueenSide.col() + i));
                    }
                    rookTravelSquares.remove(kingFromCoordinate);
                    rookTravelSquares.remove(queenSideRook);
                    kingTravelSquares.remove(kingFromCoordinate);
                    kingTravelSquares.remove(queenSideRook);
                    if (rookTravelSquares.stream().noneMatch(c -> pieceMap.containsKey(c)) &&
                        kingTravelSquares.stream().noneMatch(c -> pieceMap.containsKey(c))) {
                        if (kingTravelSquares.stream().noneMatch(
                                    c -> attackedCoordinatePredicate.test(c, king.color().other(), pieceMap))) {
                            castlingMoves.add(new Castling(
                                        new FromTo(kingFromCoordinate, kingTargetSquareQueenSide),
                                        new FromTo(queenSideRook, rookTargetSquareQueenSide)));
                        }
                    }
                }
            }
            return castlingMoves;
        }

        @FunctionalInterface
        interface AttackedCoordinatePredicate {
            boolean test(Coordinate coordinate, Side attacker, Map<Coordinate, Piece> pieceMap);
        }

    }

    record FEN(String positions, Side whoseTurn, CastlingRights castlingRights, String ep, int halfMoveClock, int move) {
        public static final FEN standard = new FEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", Side.WHITE, CastlingRights.parse("KQkq"), "-", 0, 1);


        //sealed interface Component {}
        sealed interface Component permits Positions, WhoseTurn, Castling, EP, HalfMoveClock, Move {}

        record Positions(String value) implements Component {}
        record WhoseTurn(Side value) implements Component {}
        record Castling(CastlingRights value) implements Component {}
        record EP(String value) implements Component {}
        record HalfMoveClock(int value) implements Component {}
        record Move(int value) implements Component {}

        FEN with(Component component) {
            return new FEN(
                    component instanceof Positions p ? p.value : positions(),
                    component instanceof WhoseTurn w ? w.value : whoseTurn(),
                    component instanceof Castling c ? c.value : castlingRights(),
                    component instanceof EP e ? e.value : ep(),
                    component instanceof HalfMoveClock h ? h.value : halfMoveClock(),
                    component instanceof Move m ? m.value : move()
                    );
        }

        FEN with(Component... components) {
            var copy = this;
            for (var component : components) copy = copy.with(component);
            return copy;
        }

        static FEN parse(String fen) {
            FEN f = FEN.standard;
            String[] parts = fen.split(" ");
            if (parts.length > 0) f = f.with(new Positions(parts[0]));
            if (parts.length > 1) f = f.with(new WhoseTurn("b".equals(parts[1]) ? Side.BLACK : Side.WHITE));
            if (parts.length > 2) f = f.with(new Castling(CastlingRights.parse(parts[2], f.positions())));
            if (parts.length > 3) f = f.with(new EP(parts[3]));
            if (parts.length > 4) f = f.with(new HalfMoveClock(Integer.parseInt(parts[4])));
            if (parts.length > 5) f = f.with(new Move(Integer.parseInt(parts[5])));
            return f;
        }

        @Override
        public String toString() {
            return "%s %s %s %s %d %d".formatted(positions, whoseTurn.toChar(), castlingRights.toString(), ep, halfMoveClock, move);
        }

        public String asStandard() {
            return "%s %s %s %s %d %d".formatted(positions, whoseTurn.toChar(), castlingRights.asStandard(), ep, halfMoveClock, move);
        }
        public String as960() {
            return "%s %s %s %s %d %d".formatted(positions, whoseTurn.toChar(), castlingRights.as960(), ep, halfMoveClock, move);
        }
     }

    private static Map<Coordinate, Piece> pieceMap(String positions) {
        Map<Coordinate, Piece> pieceMap = new HashMap<>(32);
        String[] ranks = positions.split("/");
        for (int rank = ranks.length-1; rank >= 0; rank--) {
            int file = 0;
            for (char c : ranks[rank].toCharArray()) {
                if (c >= '1' && c <= '8') {
                    file += Character.getNumericValue(c);
                } else {
                    pieceMap.put(Coordinate.rowCol(7 - rank, file), Piece.parse(c));
                    file++;
                }
            }
        }
        return pieceMap;
    }

    private static String positions(Map<Coordinate, Piece> pieceMap) {
        List<String> rows = new ArrayList<>(8);
        for (int row = 7; row >= 0; row--) {
            String line = "";
            int empty = 0;
            for (int col = 0; col <= 7; col++) {
                var piece = pieceMap.get(Coordinate.rowCol(row, col));
                if (piece == null) {
                    empty++;
                    continue;
                } else {
                    if (empty > 0) {
                        line += empty;
                        empty = 0;
                    }
                    line += piece.letter();
                }
            }
            if (empty > 0) {
                line += empty;
            }
            rows.add(line);
        }
        return String.join("/", rows);
    }

    sealed interface Move {

        static Move parse(String move, String fen) {
            return parse(move, FEN.parse(fen));
        }

        // ucig  1f3    ,  e1g1 (O-O)  e1c1 (O-O-O), b7c8q (bxc8=Q)
        //
        // short aalgebraic notation,  Nf3, a8=Q
        // long algebraic notation     Ng1f3 ?
        //

        // e2e4, e4
        // Rc4
        //
        // disamb
        // Rac4, Rdc4, R2c4, R8c4
        //
        // Explicit Capture
        // Rxc4
        //
        // Castling
        // O-O, O-O-O, 0-0, 0-0-0
        //
        // En passant
        // cxb3
        // cxb
        // c4xb
        // c4xb3
        //
        // Promotion
        //
        // a8=Q
        // bxa8=Q
        //
        private static Move parse(String move, FEN fen) {
            if (Objects.toString(move, "").isBlank()) return new Invalid("move: " + move + " fen: " + fen);

            var pieceMap = pieceMap(fen.positions());

            String uciMove = convertToUCI(move, fen, pieceMap);

            if (uciMove.isEmpty()) return new Invalid("move: " + move + " fen: " + fen);

            return parseUCI(uciMove, fen, pieceMap);
       }

       private static String convertToUCI(String move, FEN fen, Map<Coordinate, Piece> pieceMap) {

            char[] chars = move.toCharArray();
            if (chars.length >= 4) {
                if (chars[0] >= 'a' && chars[0] <= 'h' &&
                    chars[1] >= '1' && chars[1] <= '8' &&
                    chars[2] >= 'a' && chars[2] <= 'h' &&
                    chars[3] >= '1' && chars[3] <= '8')
                {
                    return move;
                }
            }

            // Not UCI format, maybe SAN, i.e
            // "Nf3", "e4", "exd5", "O-O", "0-0"
            // "Qxf7+", "Qf7#"
            // "Rad1"
            //
            // Hmm. So can drop some characters?
            //  "x", "#", "+"
            //  exf -> ef ... hmm
            //
            //  Qxf7+ -> Qf7

            // todo, check if UCI moves can have x,#,+ and if not, the move mutation can happen earlier
            move = move.replace("x", "").replace("#", "").replace("+", "");
            chars = move.toCharArray();

            switch (move) {
                case "O-O", "0-0" -> {
                    return fen.whoseTurn() == Side.WHITE ? "e1g1" : "e8g8";
                }
                case "O-O-O", "0-0-0" -> {
                    return fen.whoseTurn() == Side.WHITE ? "e1c1" : "e8c8";
                }
             }

            return switch(chars[0]) {
                case 'N', 'B', 'R', 'Q', 'K' -> {
                    // piece move
                    final char typeChar = chars[0];
                    // Qe1
                    var to = Coordinate.name(move.substring(move.length()-2));

                    // But could contain disambiguation info: |file| or |rank| or |fileandrank|
                    // Q|h|e1 or Q|4|e1 or Q|h4|e1

                    String disambiguation = move.substring(1, move.length()-2);

                    // Q|h4|e1
                    if (disambiguation.length() == 2) yield disambiguation + to;

                    // Q|h|e1 or Q|4|e1
                    if (disambiguation.length() == 1) {
                        char fileOrRank = disambiguation.charAt(0);
                        if (fileOrRank >= '0' && fileOrRank <= '8') {
                            //rank
                            yield pieceMap.entrySet().stream()
                                .filter(entry -> Character.toUpperCase(entry.getValue().type().toChar()) == typeChar)
                                .filter(entry -> entry.getValue().color() == fen.whoseTurn())
                                .filter(entry -> entry.getKey().name().charAt(1) == fileOrRank)
                                .map(entry -> entry.getKey().name())
                                .filter(fromName -> validMovesByPiece(
                                            Coordinate.name(fromName), pieceMap, fen)
                                        .contains(new FromTo(Coordinate.name(fromName), to)))
                                .map(fromName -> fromName + to.name())
                                .findAny()
                                .orElse("");
                        } else {
                            //file
                            yield pieceMap.entrySet().stream()
                                .filter(entry -> Character.toUpperCase(entry.getValue().type().toChar()) == typeChar)
                                .filter(entry -> entry.getValue().color() == fen.whoseTurn())
                                .filter(entry -> entry.getKey().name().charAt(0) == fileOrRank)
                                .map(entry -> entry.getKey().name())
                                .filter(fromName -> validMovesByPiece(
                                            Coordinate.name(fromName), pieceMap, fen)
                                        .contains(new FromTo(Coordinate.name(fromName), to)))
                                .map(fromName -> fromName + to.name())
                                .findAny()
                                .orElse("");
                        }
                    }

                    // No disambiguation, so only one Q can make it to e1, find it
                    yield pieceMap.entrySet().stream()
                                .filter(entry -> Character.toUpperCase(entry.getValue().type().toChar()) == typeChar)
                                .filter(entry -> entry.getValue().color() == fen.whoseTurn())
                                .map(entry -> entry.getKey().name())
                                .filter(fromName -> validMovesByPiece(
                                            Coordinate.name(fromName), pieceMap, fen)
                                        .contains(new FromTo(Coordinate.name(fromName), to)))
                                .map(fromName -> fromName + to.name())
                                .findAny()
                                .orElse("");
                }
                case 'a','b','c','d','e','f','g','h' -> {
                    final char file = chars[0];
                    var to = Coordinate.name(move.substring(move.length()-2));

                    // d3    d2d3
                    // d4    d2d4 / d3d4
                    // dc6
                    // dc6
                    yield pieceMap.entrySet().stream()
                                .filter(entry -> entry.getValue().type() == PieceType.PAWN)
                                .filter(entry -> entry.getValue().color() == fen.whoseTurn())
                                .map(entry -> entry.getKey().name())
                                .filter(fromName -> fromName.charAt(0) == file)
                                .filter(fromName -> validMovesByPiece(
                                            Coordinate.name(fromName), pieceMap, fen)
                                        .contains(new FromTo(Coordinate.name(fromName), to)))
                                .map(fromName -> fromName + to.name())
                                .findAny()
                                .orElse("");
                }
                default -> "";
            };

        }

        // g1f3, e1g1 (O-O)  e1c1 (O-O-O), b7c8q (bxc8=Q)
        private static Move parseUCI(String uci, FEN fen, Map<Coordinate, Piece> pieceMap) {
            var from = Coordinate.name(uci.substring(0,2));
            var to = Coordinate.name(uci.substring(2,4));

            var piece = pieceMap.get(from);
            if (piece == null || piece.color() != fen.whoseTurn()) {
                return new Invalid("from: " + from + " to: " + to + " piece: " + piece + " fen: " + fen);
            }

            var move = new FromTo(from, to);

            Set<Move> validMoves = validMovesByPiece(from, pieceMap, fen);

            if (! validMoves.contains(move)) {
                return validMoves.stream()
                    .filter(m -> m instanceof Castling c && c.king().equals(move))
                    .findAny().orElseGet(() -> new Invalid("move: " + move + " fen: " + fen));
            }

            if (uci.length() == 5) {
                PieceType type = switch(uci.charAt(4)) {
                    case 'q' -> PieceType.QUEEN;
                    case 'n' -> PieceType.KNIGHT;
                    case 'b' -> PieceType.BISHOP;
                    case 'r' -> PieceType.ROOK;
                    default  -> PieceType.QUEEN;
                };
                return new Promotion(move, type);
            }

            return move;
        }

        default String uci() {
            if (this instanceof FromTo fromTo) return uci(fromTo.from(), fromTo.to());
            if (this instanceof Castling castling) return uci(castling.king().from(), castling.king().to());
            if (this instanceof Promotion promotion) return uci(promotion.pawn().from(), promotion.pawn().to())
                + Character.toLowerCase(promotion.piece().toChar());
            if (this instanceof Invalid invalid) return invalid.info();
            return "gotta catch them all";
        }

        default String uci(Coordinate from, Coordinate to) {
            return from.name() + to.name();
        }
    }

    String toSAN(String move);
    String toSAN(String... move);
    List<String> toSAN(List<String> move);
    String toSAN(Move move);

    record Invalid(String info)                    implements Move {};
    record FromTo(Coordinate from, Coordinate to)  implements Move {
        @Override
        public String toString() {
            return uci();
        }
    };
    record Castling(FromTo king, FromTo rook)      implements Move {
        @Override
        public String toString() {
            return uci();
        }
    };
    record Promotion(FromTo pawn, PieceType piece) implements Move {
        @Override
        public String toString() {
            return uci();
        }
    };


    private static Set<Move> validMoves(FEN fen) {
        return validMoves(fen, pieceMap(fen.positions()));
    }

    private static Set<Move> validMoves(FEN fen, Map<Coordinate, Piece> pieceMap) {
        return pieceMap.entrySet().stream()
            .filter(entry -> entry.getValue().color() == fen.whoseTurn())
            .flatMap(entry -> validMovesByPiece(entry.getKey(), pieceMap, fen).stream())
            .collect(Collectors.toSet());
    }

    static Set<Move> validMovesByPiece(Coordinate coordinate, Map<Coordinate, Piece> pieceMap, FEN fen) {
        Piece piece = pieceMap.get(coordinate);
        if (piece == null) {
            return Set.of();
        }

        var king = piece.type() == PieceType.KING ? coordinate
            : pieceMap.entrySet().stream()
            .filter(entry -> entry.getValue().type() == PieceType.KING && entry.getValue().color() == piece.color())
            .map(entry -> entry.getKey())
            .findAny().orElse(null);

        if (king == null) {
            System.err.println("No king: " + fen);
            return Set.of();
        }

        int row = coordinate.row();
        int col = coordinate.col();

        Set<Coordinate> attackedCoordinates = coordinatesAttackedByPiece(coordinate, pieceMap);

        Set<Move> possiblyInvalidMoves = switch(piece.type()) {
            case PAWN -> {
                Set<Move> moves = new HashSet<>();
                // Captures
                moves.addAll(
                        attackedCoordinates.stream()
                        .filter(c -> pieceMap.containsKey(c) && pieceMap.get(c).color() == piece.color().other()
                                  || c.name().equals(fen.ep()))
                        .map(c -> new FromTo(coordinate, c))
                        .toList());
                var oneForward = Coordinate.rowCol(row + (piece.color() == Side.BLACK ? -1 : 1), col);
                if (! pieceMap.containsKey(oneForward)) {
                    moves.add(new FromTo(coordinate, oneForward));
                    if (row == 1 && piece.color() == Side.WHITE || row == 6 && piece.color() == Side.BLACK) {
                        var twoForward = Coordinate.rowCol(row + (piece.color() == Side.BLACK ? -2 : 2), col);
                        if (! pieceMap.containsKey(twoForward)) moves.add(new FromTo(coordinate, twoForward));
                    }
                }
                yield moves;
            }

            case KNIGHT, BISHOP, ROOK, QUEEN ->
                attackedCoordinates.stream()
                .filter(c -> pieceMap.containsKey(c) == false ||
                        pieceMap.containsKey(c) && pieceMap.get(c).color() == piece.color().other())
                .map(c -> new FromTo(coordinate, c))
                .collect(Collectors.toSet());

            case KING -> {
                Set<Move> moves = new HashSet<>();
                moves.addAll(
                        attackedCoordinates.stream()
                        .filter(c -> pieceMap.containsKey(c) == false ||
                            pieceMap.containsKey(c) && pieceMap.get(c).color() == piece.color().other())
                        .map(c -> new FromTo(coordinate, c))
                        .toList());
                moves.addAll(fen.castlingRights().validCastlingMoves(coordinate, Board::isCoordinateAttacked, pieceMap));
                yield moves;
            }
        };

        var mutableMap = new HashMap<>(pieceMap);

        return possiblyInvalidMoves.stream()
            .filter(move -> {
                boolean includeMove = false;
                if (move instanceof FromTo fromTo) {
                    // Check if move would result in current side being in check

                    // mutate map
                    Piece from = mutableMap.remove(fromTo.from());
                    Piece to   = mutableMap.put(fromTo.to(), from);

                    includeMove = ! isCoordinateAttacked(
                            from.type() == PieceType.KING ? fromTo.to() : king,
                            piece.color().other(),
                            mutableMap);

                    // restore map
                    mutableMap.put(fromTo.from(), from);
                    if (to != null) {
                        mutableMap.put(fromTo.to(), to);
                    } else {
                        mutableMap.remove(fromTo.to());
                    }
                } else if (move instanceof Castling castling) {
                    includeMove = true;
                } else if (move instanceof Invalid invalid) {}
                return includeMove;
            }).collect(Collectors.toSet());
    }

    static boolean isCoordinateAttacked(Coordinate coordinate, Side attacker, Map<Coordinate, Piece> pieceMap) {
        return pieceMap.entrySet().stream()
            .filter(entry -> entry.getValue().color() == attacker)
            .map(entry -> coordinatesAttackedByPiece(entry.getKey(), pieceMap))
            .anyMatch(attackedSquares -> attackedSquares.contains(coordinate));
    }

    static Set<Coordinate> coordinatesAttackedByPiece(Coordinate coordinate, Map<Coordinate, Piece> pieceMap) {
        Piece piece = pieceMap.get(coordinate);
        if (piece == null) {
            return Set.of();
        }

        int row = coordinate.row();
        int col = coordinate.col();

        record Dir(int dx, int dy) {}

        Set<Coordinate> possiblyInvalidCoordinates = switch(piece.type()) {
            case PAWN -> Set.of(
                    Coordinate.rowCol(row + (piece.color() == Side.BLACK ? -1 : 1), col - 1),
                    Coordinate.rowCol(row + (piece.color() == Side.BLACK ? -1 : 1), col + 1));
            case KNIGHT -> Set.of(
                    Coordinate.rowCol(row + 1, col + 2),
                    Coordinate.rowCol(row + 1, col - 2),
                    Coordinate.rowCol(row - 1, col + 2),
                    Coordinate.rowCol(row - 1, col - 2),
                    Coordinate.rowCol(row + 2, col + 1),
                    Coordinate.rowCol(row + 2, col - 1),
                    Coordinate.rowCol(row - 2, col + 1),
                    Coordinate.rowCol(row - 2, col - 1)
                    );
            case BISHOP -> {
                Set<Coordinate> coordinates = new HashSet<>();
                for (var dir : List.of(new Dir(-1,1), new Dir(1,1), new Dir(-1,-1), new Dir(1,-1))) {
                    int r = row + dir.dy();
                    int c = col + dir.dx();
                    while(r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                        Coordinate coord = Coordinate.rowCol(r, c);
                        coordinates.add(coord);
                        if (pieceMap.containsKey(coord)) break;
                        r += dir.dy();
                        c += dir.dx();
                    }
                }
                yield coordinates;
            }
            case ROOK -> {
                Set<Coordinate> coordinates = new HashSet<>();
                for (var dir : List.of(new Dir(0,1), new Dir(0,-1), new Dir(1,0), new Dir(-1,0))) {
                    int r = row + dir.dy();
                    int c = col + dir.dx();
                    while(r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                        Coordinate coord = Coordinate.rowCol(r, c);
                        coordinates.add(coord);
                        if (pieceMap.containsKey(coord)) break;
                        r += dir.dy();
                        c += dir.dx();
                    }
                }
                yield coordinates;
            }
            case QUEEN -> {
                Set<Coordinate> coordinates = new HashSet<>();
                for (var dir : List.of(new Dir(0,1), new Dir(0,-1), new Dir(1,0), new Dir(-1,0),
                                       new Dir(-1,1), new Dir(1,1), new Dir(-1,-1), new Dir(1,-1))) {
                    int r = row + dir.dy();
                    int c = col + dir.dx();
                    while(r >= 0 && r <= 7 && c >= 0 && c <= 7) {
                        Coordinate coord = Coordinate.rowCol(r, c);
                        coordinates.add(coord);
                        if (pieceMap.containsKey(coord)) break;
                        r += dir.dy();
                        c += dir.dx();
                    }
                }
                yield coordinates;
            }
            case KING -> Set.of(
                    Coordinate.rowCol(row + 1, col + 1),
                    Coordinate.rowCol(row + 1, col),
                    Coordinate.rowCol(row + 1, col - 1),
                    Coordinate.rowCol(row , col - 1),
                    Coordinate.rowCol(row , col + 1),
                    Coordinate.rowCol(row - 1, col + 1),
                    Coordinate.rowCol(row - 1, col),
                    Coordinate.rowCol(row - 1, col - 1)
                    );
        };

        return possiblyInvalidCoordinates.stream()
            .filter(c -> c.row() >= 0 && c.row() <= 7 && c.col() >= 0 && c.col() <= 7)
            .collect(Collectors.toSet());
    }
}
