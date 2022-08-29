package chariot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        // todo, result instead of bool for "ended"
        // - draw by half move clock
        // - draw by threefold repetition
        // - draw by stalemate
        // - result by checkmate
        boolean ended = fen.halfMoveClock() >= 100;
        if (!ended) {
            Set<Move> validMoves = Board.validMoves(fen);
            ended = validMoves.isEmpty();
        }

        if (!ended) {
            record PositionAndMove(String position, int move) {}
            ended = Stream.concat(Stream.of(fen), history.stream())
                .map(f -> new PositionAndMove(f.positions(), f.move()))
                .distinct()
                .collect(Collectors.groupingBy(PositionAndMove::position, Collectors.counting()))
                .values().stream().anyMatch(count -> count >= 3);
        }

        var bd = new BoardData(pieceMap(fen.positions()), fen, history, ended);
        return bd;
    }

    String toFEN();
    Set<Move> validMoves();
    Board play(String move);

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

    record BoardData(Map<Coordinate, Piece> pieceMap, FEN fen, List<FEN> history, boolean ended) implements Board {

        public BoardData {
            pieceMap = Map.copyOf(pieceMap);
            history = List.copyOf(history);
        }

        @Override
        public Piece get(Coordinate coordinate) {
            return pieceMap.get(coordinate);
        }

        @Override
        public String toFEN() {
            return fen().toString();
        }

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
            if (ended()) {
                return this;
            }
            return play(Move.parse(move, fen));
        }

        private Board play(Move move) {
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
                        String rights = fen().castling();
                        if (piece.color() == Side.BLACK) {
                            rights = rights.replace("k", "").replace("q", "");
                        } else {
                            rights = rights.replace("K", "").replace("Q", "");
                        }
                        if (rights.isEmpty()) rights = "-";
                        nextFEN = nextFEN.with(new FEN.Castling(rights));
                    }
                    case ROOK -> {
                        String castling = fen().castling();
                        if (piece.color() == Side.BLACK) {
                            if (fromTo.from().name().equals("a8")) {
                                castling = castling.replace("q", "");
                            } else if (fromTo.from().name().equals("h8")) {
                                castling = castling.replace("k", "");
                            }
                        } else {
                            if (fromTo.from().name().equals("a1")) {
                                castling = castling.replace("Q", "");
                            } else if (fromTo.from().name().equals("h1")) {
                                castling = castling.replace("K", "");
                            }
                        }
                        if (castling.isEmpty()) castling = "-";
                        nextFEN = nextFEN.with(new FEN.Castling(castling));
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

                String rights = fen().castling();
                if (king.color() == Side.BLACK) {
                    rights = rights.replace("k", "").replace("q", "");
                } else {
                    rights = rights.replace("K", "").replace("Q", "");
                }
                if (rights.isEmpty()) rights = "-";
                nextFEN = nextFEN.with(new FEN.Castling(rights));
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
    }

    record RowCol(int row, int col) implements Coordinate {
        @Override
        public String toString() {
            return name();
        }
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

    record FEN(String positions, Side whoseTurn, String castling, String ep, int halfMoveClock, int move) {
        public static final FEN standard = new FEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", Side.WHITE, "KQkq", "-", 0, 1);

        //sealed interface Component {}
        sealed interface Component permits Positions, WhoseTurn, Castling, EP, HalfMoveClock, Move {}

        record Positions(String value) implements Component {}
        record WhoseTurn(Side value) implements Component {}
        record Castling(String value) implements Component {}
        record EP(String value) implements Component {}
        record HalfMoveClock(int value) implements Component {}
        record Move(int value) implements Component {}

        FEN with(Component component) {
            return new FEN(
                    component instanceof Positions p ? p.value : positions(),
                    component instanceof WhoseTurn w ? w.value : whoseTurn(),
                    component instanceof Castling c ? c.value : castling(),
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
            if (parts.length > 2) f = f.with(new Castling(parts[2]));
            if (parts.length > 3) f = f.with(new EP(parts[3]));
            if (parts.length > 4) f = f.with(new HalfMoveClock(Integer.parseInt(parts[4])));
            if (parts.length > 5) f = f.with(new Move(Integer.parseInt(parts[5])));
            return f;
        }

        @Override
        public String toString() {
            return "%s %s %s %s %d %d".formatted(positions, whoseTurn.toChar(), castling, ep, halfMoveClock, move);
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

                if (piece.color() == Side.WHITE) {
                    if (fen.castling().contains("K")) {
                        boolean fail = false;
                        for (var name : List.of("f1", "g1")) {
                            fail |= pieceMap.containsKey(Coordinate.name(name));
                        }
                        if (!fail) {
                            for (var name : List.of("e1", "f1", "g1")) {
                                fail |= isCoordinateAttacked(Coordinate.name(name), piece.color().other(), pieceMap);
                                if (fail) break;
                            }
                        }
                        if (!fail) {
                            moves.add(new Castling(
                                        new FromTo(coordinate, Coordinate.name("g1")),
                                        new FromTo(Coordinate.name("h1"), Coordinate.name("f1"))
                                        ));
                        }
                    }
                    if (fen.castling().contains("Q")) {
                        boolean fail = false;
                        for (var name : List.of("d1", "c1")) {
                            fail |= pieceMap.containsKey(Coordinate.name(name));
                        }
                        if (!fail) {
                            for (var name : List.of("e1", "d1", "c1")) {
                                fail |= isCoordinateAttacked(Coordinate.name(name), piece.color().other(), pieceMap);
                                if (fail) break;
                            }
                        }
                        if (!fail) {
                            moves.add(new Castling(
                                        new FromTo(coordinate, Coordinate.name("c1")),
                                        new FromTo(Coordinate.name("a1"), Coordinate.name("d1"))
                                        ));
                        }
                    }
                } else {
                    if (fen.castling().contains("k")) {
                        boolean fail = false;
                        for (var name : List.of("f8", "g8")) {
                            fail |= pieceMap.containsKey(Coordinate.name(name));
                        }
                        if (!fail) {
                            for (var name : List.of("e8", "f8", "g8")) {
                                fail |= isCoordinateAttacked(Coordinate.name(name), piece.color().other(), pieceMap);
                                if (fail) break;
                            }
                        }
                        if (!fail) {
                            moves.add(new Castling(
                                        new FromTo(coordinate, Coordinate.name("g8")),
                                        new FromTo(Coordinate.name("h8"), Coordinate.name("f8"))
                                        ));
                        }
                    }
                    if (fen.castling().contains("q")) {
                        boolean fail = false;
                        for (var name : List.of("d8", "c8")) {
                            fail |= pieceMap.containsKey(Coordinate.name(name));
                        }
                        if (!fail) {
                            for (var name : List.of("e8", "d8", "c8")) {
                                fail |= isCoordinateAttacked(Coordinate.name(name), piece.color().other(), pieceMap);
                                if (fail) break;
                            }
                        }
                        if (!fail) {
                            moves.add(new Castling(
                                        new FromTo(coordinate, Coordinate.name("c8")),
                                        new FromTo(Coordinate.name("a8"), Coordinate.name("d8"))
                                        ));
                        }
                    }
                }
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
