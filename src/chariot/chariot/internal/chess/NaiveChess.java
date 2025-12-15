package chariot.internal.chess;

import module java.base;
import module chariot;

// Naive implementation of chess (as opposed to efficient use of bitboard representations & operations)
public record NaiveChess(String variant, FEN fen, Map<Square.Pos, Square<Piece>> squareMap, RookFiles rookFiles) implements Board {

    public static NaiveChess of(String variant, String fenString) {
        FEN fen = FEN.parse(fenString);
        var squareMap = DefaultBoard.fenPositionsToSquares(fen.positions());
        var rookFiles = initRookFiles(variant, fen.castling(), squareMap);
        return new NaiveChess(variant, fen, squareMap, rookFiles);
    }

    @Override
    public Collection<String> validMoves() {
        return piecesMatching(candidate -> candidate.side() == fen.side())
            .flatMap(piece -> validMovesByPiece(piece))
            .map(move -> move.toUCI(variant()))
            .toList();
    }

    @Override
    public Board play(Move move) {
        if (validMoves().isEmpty()) return this;
        InternalMove internalMove = toInternalMove(move.asString());
        if (internalMove == null) return this;
        return _play(internalMove);
    }

    @Override public String toFEN()          { return fen().toString(); }
    @Override public String variant()        { return variant; }
    @Override public String toUCI(Move move) { return anyToUCI(move.asString()); }

    @Override
    public String toSAN(Move move) {
        if (! (toInternalMove(move.asString()) instanceof InternalMove internalMove)) return "";

        NaiveChess boardIfPlayed = _play(internalMove);

        Square.With<Piece> king = boardIfPlayed.piecesMatching(candidate ->
                candidate.side() == fen().side().other() &&
                candidate.type() == Piece.king)
            .findFirst().orElse(null);

        boolean inCheck = boardIfPlayed.piecesMatching(candidate ->
                candidate.side() == fen().side())
            .anyMatch(candidate -> boardIfPlayed.squaresAttackedByPiece(candidate)
                    .anyMatch(square -> square.equals(king.pos())));

        String checkSymbol = inCheck
            ? (boardIfPlayed.validMoves().isEmpty()
                    ? "#"
                    : "+"
              )
            : "";

        return switch(internalMove) {
            case FromTo(Square.With(Square.Pos from, Piece type, Side side), Square.Pos to) -> {
                String letter = String.valueOf(type.toChar(Side.white));
                String capture = squareMap().get(to) instanceof Square.With ? "x" : "";

                yield switch(type) {
                    case pawn -> squareMap().get(to) instanceof Square.With || to.file() != from.file()
                        ? "%s".repeat(4).formatted(from.file(), capture, to, checkSymbol)
                        : "%s".repeat(2).formatted(to, checkSymbol);

                    case king -> "%s".repeat(4).formatted(letter, capture, to, checkSymbol);

                    case knight, bishop, rook, queen -> {
                        List<Square.With<Piece>> disambiguation = piecesMatching(candidate ->
                                candidate.type() == type &&
                                candidate.side() == side &&
                                !candidate.pos().equals(from) &&
                                squaresAttackedByPiece(candidate).anyMatch(square -> square.equals(to)))
                            .toList();

                        String dis = "";
                        if ( ! disambiguation.isEmpty()) {
                            record Unique(boolean file, boolean rank) {}
                            var unique = disambiguation.stream().reduce(
                                    new Unique(true, true),
                                    (result, piece) -> new Unique(result.file() && piece.file() != from.file(),
                                                                  result.rank() && piece.rank() != from.rank()),
                                    (res1, res2) -> new Unique(res1.file() && res2.file(), res1.rank() && res2.rank()));

                            if (unique.file() && unique.rank()) {
                                dis = from.toString().substring(0,1); // specify the file
                            } else if (unique.file()) {
                                dis = from.toString().substring(0,1); // specify the file
                            } else if (unique.rank()) {
                                dis = from.toString().substring(1,2); // specify the rank
                            } else {
                                dis = from.toString();                // specify both file and rank
                            }
                        }
                        yield "%s".repeat(5).formatted(letter, dis, capture, to, checkSymbol);
                    }
                };
            }
            case Castling castling -> {
                String castleside = castling.king().from().file() < castling.rook().from().file() ? "O-O" : "O-O-O";
                yield "%s".repeat(2).formatted(castleside, checkSymbol);
            }
            case Promotion(FromTo(Square.With(Square.Pos from,_,_), Square.Pos to), Piece toPiece) -> {
                String promotion = "=" + String.valueOf(toPiece.toChar(Side.white));
                if (squareMap().get(to) instanceof Square.With) {
                    // bxa8=R
                    yield "%s".repeat(5).formatted(from.file(), "x", to, promotion, checkSymbol);
                } else {
                    // a8=Q
                    yield "%s".repeat(3).formatted(to, promotion, checkSymbol);
                }
            }
        };
    }

    public NaiveChess withFEN(FEN _fen) {
        var _squareMap = DefaultBoard.fenPositionsToSquares(_fen.positions());
        return new NaiveChess(variant(), _fen, _squareMap, rookFiles());
    }

    NaiveChess _play(InternalMove move) {

        Map<Square.Pos, Square<Piece>> mutableMap = new HashMap<>(squareMap());

        FEN nextFen = FEN.parse(fen().toString());

        boolean resetHalfMoveClock = false;
        String ep = "-";
        Square<Piece> maybeCapture = null;
        switch(move) {
            case FromTo(Square.With(Square.Pos from, Piece type, Side side), Square.Pos to) -> {
                mutableMap.put(from, Square.empty(from));
                maybeCapture = mutableMap.put(to, Square.withPiece(to, type, side));

                switch (type) {
                    case pawn -> {
                        resetHalfMoveClock = true;

                        if (from.file() != to.file()) {
                            // capture

                            if (maybeCapture instanceof Square.Empty) {
                                // Aha! Must have been en passant!
                                // Put empty square where pawn was
                                Square.Pos epSquare = Square.pos(to.file(), from.rank());
                                mutableMap.put(epSquare, Square.empty(epSquare));
                            }
                        }

                        int distance = from.rank() - to.rank();
                        if (Math.abs(distance) == 2) {
                            if (mutableMap.get(to.delta(0, -1)) instanceof Square.With(_, Piece maybePawn, Side maybeOther) &&
                                maybePawn == Piece.pawn && maybeOther == side.other()) {
                                ep = to.delta(distance/2, 0).toString();
                            } else if (mutableMap.get(to.delta(0, +1)) instanceof Square.With(_, Piece maybePawn, Side maybeOther) &&
                                maybePawn == Piece.pawn && maybeOther == side.other()) {
                                ep = to.delta(distance/2, 0).toString();
                            }
                        }
                    }

                    case king -> {
                        nextFen = nextFen.withCastling(nextFen.castling().chars()
                                .filter(i -> fen().side() == Side.black
                                    ? Character.isUpperCase(i)
                                    : Character.isLowerCase(i))
                                .mapToObj(i -> String.valueOf((char) i))
                                .collect(Collectors.joining()));
                    }
                    case rook -> {
                        if (castlingRookFiles(fen().side()).contains(from.file()) &&
                            from.rank() == (fen().side() == Side.white ? 1 : 8)) {

                            Square.Pos king = piecesMatching(candidate ->
                                    candidate.side() == fen.side() &&
                                    candidate.type() == Piece.king)
                                .findFirst()
                                .map(Square.With::pos)
                                .orElse(null);

                            if (king != null) {
                                String file = fen().side() == Side.white
                                    ? String.valueOf(from.file()).toUpperCase(Locale.ROOT)
                                    : String.valueOf(from.file());

                                nextFen = nextFen.withCastling(nextFen.castling().replace(file, ""));

                                if (king.file() < from.file()) {
                                    // kingside
                                    String k = fen().side() == Side.white ? "K" : "k";
                                    nextFen = nextFen.withCastling(nextFen.castling().replace(k, ""));
                                } else {
                                    // queenside
                                    String q = fen().side() == Side.white ? "Q" : "q";
                                    nextFen = nextFen.withCastling(nextFen.castling().replace(q, ""));
                                }
                            }
                        }
                    }
                    case bishop, knight, queen -> {}
                };
            }
            case Promotion(FromTo fromTo, Piece promotion) -> {
                mutableMap.put(fromTo.from().pos(), Square.empty(fromTo.from().pos()));
                maybeCapture = mutableMap.put(fromTo.to(), Square.withPiece(fromTo.to(), promotion, fromTo.from().side()));
                resetHalfMoveClock = true;
            }
            case Castling(FromTo king, FromTo rook) -> {

                mutableMap.put(king.from().pos(), Square.empty(king.from().pos()));
                mutableMap.put(rook.from().pos(), Square.empty(rook.from().pos()));

                mutableMap.put(king.to(), Square.withPiece(king.to(), king.from().type(), king.from().side()));
                mutableMap.put(rook.to(), Square.withPiece(rook.to(), rook.from().type(), rook.from().side()));

                nextFen = nextFen.withCastling(nextFen.castling().chars()
                        .filter(i -> fen().side() == Side.black
                            ? Character.isUpperCase(i)
                            : Character.isLowerCase(i))
                        .mapToObj(i -> String.valueOf((char) i))
                        .collect(Collectors.joining()));
            }
        }

        // capture (FromTo or Promotion(FromTo))
        if (maybeCapture instanceof Square.With(Square.Pos capturedPos, Piece capturedPiece, Side capturedSide)) {
            resetHalfMoveClock = true;

            if (capturedPiece == Piece.rook && castlingRookFiles(capturedSide).contains(capturedPos.file()) &&
                    capturedPos.rank() == (capturedSide == Side.white ? 1 : 8)) {

                Square.Pos king = piecesMatching(candidate ->
                        candidate.side() == capturedSide &&
                        candidate.type() == Piece.king)
                    .findFirst()
                    .map(Square.With::pos)
                    .orElse(null);

                if (king != null) {
                    String file = capturedSide == Side.white
                        ? String.valueOf(capturedPos.file()).toUpperCase(Locale.ROOT)
                        : String.valueOf(capturedPos.file());

                    nextFen = nextFen.withCastling(nextFen.castling().replace(file, ""));

                    if (king.file() < capturedPos.file()) {
                        // kingside
                        String k = capturedSide == Side.white ? "K" : "k";
                        nextFen = nextFen.withCastling(nextFen.castling().replace(k, ""));
                    } else {
                        // queenside
                        String q = capturedSide == Side.white ? "Q" : "q";
                        nextFen = nextFen.withCastling(nextFen.castling().replace(q, ""));
                    }
                }
            }
        }

        nextFen = nextFen
            .withPositions(DefaultBoard.squaresToFenPositions(mutableMap))
            .withSide(fen().side().other())
            .withEP(ep)
            .withHalfMove(resetHalfMoveClock ? 0 : fen().halfMove() + 1)
            .withMove(fen().side() == Side.black ? fen().move() + 1 : fen().move());

        NaiveChess nextBoard = withFEN(nextFen);

        return nextBoard;
    }

    InternalMove toInternalMove(String moveStr) {
        String uci = anyToUCI(moveStr);

        Square.Pos fromPos = Square.pos(uci.substring(0,2));
        Square.Pos toPos = Square.pos(uci.substring(2,4));

        if (! (squareMap().get(fromPos) instanceof Square.With<Piece> withPiece && withPiece.side() == fen().side())) {
            return null;
        }

        FromTo fromToMove = new FromTo(withPiece, toPos);
        InternalMove move = fromToMove;

        if (uci.length() == 5) {
            move = new Promotion(fromToMove, switch(uci.charAt(4)) {
                case 'q' -> Piece.queen;
                case 'n' -> Piece.knight;
                case 'b' -> Piece.bishop;
                case 'r' -> Piece.rook;
                default  -> Piece.queen;
            });
        }

        Set<InternalMove> validMoves = validMovesByPiece(withPiece).collect(Collectors.toSet());
        if (! validMoves.contains(move)) {
            return validMoves.stream()
                .filter(m -> m instanceof Castling castling &&
                        (castling.king().equals(fromToMove) || (
                                                                fromToMove.from().equals(castling.king().from()) &&
                                                                fromToMove.to().equals(castling.rook().from().pos()))
                        ))
                .findAny().orElse(null);
        }
        return move;
    }

    String anyToUCI(String any) {
        char[] chars = any.toCharArray();

        // check if already uci
        if (chars.length >= 4 &&
                chars[0] >= 'a' && chars[0] <= 'h' &&
                chars[1] >= '1' && chars[1] <= '8' &&
                chars[2] >= 'a' && chars[2] <= 'h' &&
                chars[3] >= '1' && chars[3] <= '8') {
            return any;
                }

        // Not UCI format, maybe SAN, i.e
        // "Nf3", "e4", "exd5", "O-O", "0-0"
        // "Qxf7+", "Qf7#"
        // "Rad1"

        String move = any.replace("x", "").replace("#", "").replace("+", "");
        chars = move.toCharArray();

        switch (move) {
            case "O-O", "0-0" -> {
                Square.Pos kingPos = piecesMatching(candidate ->
                        candidate.side() == fen.side() &&
                        candidate.type() == Piece.king)
                    .findFirst()
                    .map(Square.With::pos)
                    .orElse(null);
                if (kingPos == null) return "";

                Square.Pos rookPos = castlingRookFiles(fen().side()).stream()
                    .filter(file -> kingPos.file() < file)
                    .map(file -> Square.pos(file, kingPos.rank()))
                    .findFirst().orElse(null);
                if (rookPos == null) return "";

                return "%s%s".formatted(kingPos, rookPos); // king-onto-rook
            }
            case "O-O-O", "0-0-0" -> {
                Square.Pos kingPos = piecesMatching(candidate ->
                        candidate.side() == fen.side() &&
                        candidate.type() == Piece.king)
                    .findFirst()
                    .map(Square.With::pos)
                    .orElse(null);
                if (kingPos == null) return "";

                Square.Pos rookPos = castlingRookFiles(fen().side()).stream()
                    .filter(file -> kingPos.file() > file)
                    .map(file -> Square.pos(file, kingPos.rank()))
                    .findFirst().orElse(null);
                if (rookPos == null) return "";

                return "%s%s".formatted(kingPos, rookPos); // king-onto-rook
            }
        }

        return switch(chars[0]) {
            case 'N', 'B', 'R', 'Q', 'K' -> {
                // Piece move, like Qe1, or disambiguated queen move like Qhe1, Q4e1, Qh4e1

                // Q
                final char typeChar = chars[0];

                // e1
                Square.Pos to = Square.pos(move.substring(move.length()-2));

                // Check disambiguation: |file| or |rank| or |fileandrank|
                // Q|h|e1 or Q|4|e1 or Q|h4|e1

                String disambiguation = move.substring(1, move.length()-2);

                // Q|h4|e1
                if (disambiguation.length() == 2) {
                    yield "%s%s".formatted(disambiguation, to);
                }

                // Q|h|e1 or Q|4|e1
                if (disambiguation.length() == 1) {
                    char fileOrRank = disambiguation.charAt(0);
                    if (fileOrRank >= '1' && fileOrRank <= '8') {
                        // rank
                        Square.Pos from = piecesMatching(candidate ->
                                candidate.side() == fen().side() &&
                                candidate.type().toChar(Side.white) == typeChar &&
                                candidate.pos().rank() == Character.getNumericValue(fileOrRank)
                                )
                            .filter(candidate -> validMovesByPiece(candidate)
                                    .map(validMove -> validMove.toUCI(variant()))
                                    .anyMatch(str -> str.equals("%s%s".formatted(candidate.pos(), to))))
                            .map(Square.With::pos)
                            .findAny().orElse(null);
                        if (from == null) yield "";
                        yield "%s%s".formatted(from, to);
                    } else {
                        // file
                        Square.Pos from = piecesMatching(candidate ->
                                candidate.side() == fen().side() &&
                                candidate.type().toChar(Side.white) == typeChar &&
                                candidate.pos().file() == fileOrRank
                                )
                            .filter(candidate -> validMovesByPiece(candidate)
                                    .map(validMove -> validMove.toUCI(variant()))
                                    .anyMatch(str -> str.equals("%s%s".formatted(candidate.pos(), to))))
                            .map(Square.With::pos)
                            .findAny().orElse(null);
                        if (from == null) yield "";
                        yield "%s%s".formatted(from, to);
                    }
                }

                // No disambiguation, so only one Q can make it to e1, find it
                Square.Pos from = piecesMatching(candidate ->
                        candidate.side() == fen().side() &&
                        candidate.type().toChar(Side.white) == typeChar
                        )
                    .filter(candidate -> validMovesByPiece(candidate)
                            .map(validMove -> validMove.toUCI(variant()))
                            .anyMatch(str -> str.equals("%s%s".formatted(candidate.pos(), to))))
                    .map(Square.With::pos)
                    .findAny().orElse(null);
                if (from == null) yield "";
                yield "%s%s".formatted(from, to);
            }

            case 'a','b','c','d','e','f','g','h' -> {
                // pawn move
                final char file = chars[0];

                // a1=Q -> a2a1q
                String promotion = "";
                if (move.contains("=")) {
                    promotion = move.substring(move.indexOf("=")+1).toLowerCase();
                    move = move.substring(0, move.indexOf("="));
                }
                Square.Pos to = Square.pos(move.substring(move.length()-2));

                // d3    d2d3
                // d4    d2d4 / d3d4
                // dc6

                Square.Pos from = piecesMatching(candidate ->
                        candidate.type() == Piece.pawn &&
                        candidate.side() == fen().side() &&
                        candidate.pos().file() == file)
                    .filter(candidate -> validMovesByPiece(candidate)
                            .map(validMove -> validMove.toUCI(variant()))
                            .map(str -> str.substring(0,4)) // truncate any promotion piece
                            .anyMatch(str -> str.equals("%s%s".formatted(candidate.pos(), to))))
                    .map(Square.With::pos)
                    .findAny().orElse(null);
                if (from == null) yield "";
                yield "%s%s".formatted(from, to + promotion);
            }
            default -> "";
        };
    }

    Stream<Square.With<Piece>> piecesMatching(Predicate<Square.With<Piece>> filter) {
        return squareMap().values().stream()
            .<Square.With<Piece>>mapMulti((square, mapper) -> {
                if (square instanceof Square.With<Piece> piece && filter.test(piece))
                    mapper.accept(piece);
            });
    }

    sealed interface InternalMove {
        default String toUCI(String variant) {
            return switch(this) {
                case FromTo(var from, var to)
                    -> "%s%s".formatted(from.pos(), to);
                case Promotion(FromTo(var from, var to), var promote)
                    -> "%s%s%s".formatted(from.pos(), to, promote.toChar(Side.black));
                case Castling(var king, var rook) -> switch(variant) {
                    case "standard" -> rook.from().file() == 'a'
                        ? "%s%s".formatted(king.from().pos(), "c" + king.from().pos().rank())
                        : "%s%s".formatted(king.from().pos(), "g" + king.from().pos().rank());
                    default -> "%s%s".formatted(king.from().pos(), rook.from().pos());
                };
            };
        }
    }

    record FromTo(Square.With<Piece> from, Square.Pos to)  implements InternalMove {}
    record Castling(FromTo king, FromTo rook)              implements InternalMove {}
    record Promotion(FromTo pawn, Piece piece)             implements InternalMove {}

    public Stream<InternalMove> validMovesByPiece(Square.With<Piece> piece) {

        Stream<Square.Pos> attackedSquares = squaresAttackedByPiece(piece);

        Stream<InternalMove> movesByPiece = switch(piece.type()) {

            case knight, bishop, rook, queen -> attackedSquares
                .<InternalMove>mapMulti( (pos, mapper) -> {
                    var square = squareMap().get(pos);
                    if (square instanceof Square.Empty(_) ||
                            (square instanceof Square.With(_, _, Side side) && side == fen().side().other())) {
                        mapper.accept(new FromTo(piece, pos));
                    }
                });

            case pawn -> {
                Stream<FromTo> fromTo = attackedSquares
                    .<FromTo>mapMulti( (pos, mapper) -> {
                        if (squareMap().get(pos) instanceof Square.With(_, _, Side side) && side == fen().side().other()
                            || fen.ep().equals(pos.toString())) {
                            mapper.accept(new FromTo(piece, pos));
                        }
                    });

                int dir = piece.side() == Side.black ? -1 : 1;
                FromTo oneForward = new FromTo(piece, piece.pos().delta(dir, 0));

                if (squareMap().get(oneForward.to()) instanceof Square.Empty) {
                    fromTo = Stream.concat(fromTo, Stream.of(oneForward));

                    if (piece.side() == Side.black && piece.rank() == 7 ||
                            piece.side() == Side.white && piece.rank() == 2) {

                        FromTo twoForward = new FromTo(piece, piece.pos().delta(dir*2, 0));

                        if (squareMap().get(twoForward.to()) instanceof Square.Empty) {
                            fromTo = Stream.concat(fromTo, Stream.of(twoForward));
                        }
                    }
                }

                yield switch(piece) {
                    // check if any moves are promotions
                    case Square.With(var pos, _, var side) when
                        (pos.rank() == 7 && side == Side.white) ||
                        (pos.rank() == 2 && side == Side.black) -> fromTo
                        .<InternalMove>mapMulti((move, mapper) -> {
                            List.of(Piece.knight, Piece.bishop, Piece.rook, Piece.queen)
                                .forEach(type -> mapper.accept(new Promotion(move, type)));
                        });
                    default -> fromTo.map(m -> (InternalMove) m);
                };
            }

            case king -> {
                Stream<InternalMove> moves = attackedSquares
                    .<InternalMove>mapMulti( (pos, mapper) -> {
                        var square = squareMap().get(pos);
                        if (square instanceof Square.With(_, _, Side side) && side == fen().side().other()
                                || square instanceof Square.Empty(_)) {
                            mapper.accept(new FromTo(piece, pos));
                        }
                    });

                Stream<InternalMove> castlings = Stream.of();

                List<Character> rookFiles = castlingRookFiles(fen().side());

                if (! rookFiles.isEmpty()) {
                    int rank = fen().side() == Side.black ? 8 : 1;
                    for (char file : rookFiles) {
                        if (! (squareMap().get(Square.pos(file, rank)) instanceof Square.With<Piece> rook &&
                                    rook.type() == Piece.rook && rook.side() == fen().side())) {
                            IO.println("""
                                    Huh. Couldn't find rook for castling!
                                    file: %s
                                    Variant: %s
                                    FEN: %s
                                    """.formatted(file, variant(), fen()));
                            new Exception().printStackTrace();
                            continue;
                        }

                        Castling castling = rook.pos().file() < piece.file()
                            ? new Castling(new FromTo(piece, Square.pos('c', rank)), new FromTo(rook,  Square.pos('d', rank)))
                            : new Castling(new FromTo(piece, Square.pos('g', rank)), new FromTo(rook,  Square.pos('f', rank)));

                        // check that rook doesn't move through pieces (other than self and own king)
                        Set<Square.Pos> rookSquares = travelSquares(castling.rook());
                        if (blocked(rookSquares, Set.of(piece, rook))) continue;

                        // check that king doesn't move through pieces (other than the castling rook)
                        Set<Square.Pos> kingSquares = travelSquares(castling.king());
                        boolean kingBlocked = blocked(kingSquares, Set.of(piece, rook));
                        if (kingBlocked) continue;

                        // check that king doesn't move through check
                        boolean kingMovesThroughCheck = piecesMatching(candidate -> candidate.side() == fen().side().other())
                            .anyMatch(candidate -> squaresAttackedByPiece(candidate).anyMatch(kingSquares::contains));
                        if (kingMovesThroughCheck) continue;

                        castlings = Stream.concat(castlings, Stream.of(castling));
                    }
                }

                yield Stream.concat(moves, castlings);
            }
        };

        return movesByPiece
            .filter(move -> switch(move) {
                case FromTo fromTo -> ! isSelfCheck(fromTo);
                case Promotion(FromTo fromTo, _) -> ! isSelfCheck(fromTo);
                case Castling _ -> true;
            });
    }

    Set<Square.Pos> travelSquares(FromTo piece) {
        return IntStream.rangeClosed(
                Math.min(piece.from().file(), piece.to().file()),
                Math.max(piece.from().file(), piece.to().file()))
            .mapToObj(i -> Square.pos((char)i, piece.from().rank()))
            .collect(Collectors.toSet());
    }

    List<Character> castlingRookFiles(Side side) {
        return fen.castling().chars()
            .filter(i -> side == Side.black ? Character.isLowerCase(i) : Character.isUpperCase(i))
            .map(Character::toLowerCase)
            .mapToObj(i -> switch(Character.valueOf((char) i)) {
                case 'k' -> rookFiles().k();
                case 'q' -> rookFiles().q();
                case Character c -> c;
            }).toList();
    }

    boolean blocked(Set<Square.Pos> squares, Set<Square.With<Piece>> ignoredPieces) {
        return piecesMatching(candidate -> !ignoredPieces.contains(candidate) && squares.contains(candidate.pos()))
            .findAny().isPresent();
    }

    boolean isSelfCheck(FromTo fromTo) {
        // Check if move would result in current side being in check

        Square.Pos kingPos = fromTo.from().type() == Piece.king
            ? fromTo.to()
            : piecesMatching(candidate ->
                    candidate.side() == fen.side() &&
                    candidate.type() == Piece.king)
            .findFirst()
            .map(Square.With::pos)
            .orElse(null);

        if (kingPos == null) {
            // How embarassing, we've misplaced the king!
            // Let's pretend this means that we would be in check,
            // as something weird is cleary going on...
            return true;
        }

        var mutableMap = new HashMap<>(squareMap());
        mutableMap.put(fromTo.from().pos(), Square.empty(fromTo.from().pos()));
        mutableMap.put(fromTo.to(), Square.withPiece(fromTo.to(), fromTo.from().type(), fromTo.from().side()));

        String mutatedPositions = DefaultBoard.squaresToFenPositions(mutableMap);

        NaiveChess mutatedBoard = withFEN(fen()
                .withPositions(mutatedPositions)
                .withSide(fen().side().other()));

        return mutatedBoard.piecesMatching(candidate ->
                candidate.side() == fen().side().other() &&
                mutatedBoard.squaresAttackedByPiece(candidate).anyMatch(square -> Objects.equals(kingPos, square)))
            .findAny().isPresent();
    }

    record RookFiles(char k, char q) {}

    static RookFiles initRookFiles(String variant, String castling, Map<Square.Pos, Square<Piece>> squareMap) {
        return switch(variant) {
            case "chess960" -> {
                char k = ' ';
                char q = ' ';

                char whiteKingFile = findPieceFile(0, 7, Piece.king, 1, squareMap);
                char blackKingFile = findPieceFile(0, 7, Piece.king, 8, squareMap);

                for (char c : castling.toCharArray()) {
                    char lowercase = Character.toLowerCase(c);
                    int rank = Character.isLowerCase(c) ? 8 : 1;
                    char kingFile = Character.isLowerCase(c) ? blackKingFile : whiteKingFile;

                    if (lowercase >= 'a' && lowercase <= 'h') {
                        if (squareMap.get(Square.pos(lowercase, rank))
                                instanceof Square.With(_, Piece p, _) && p == Piece.rook) {
                            if (kingFile < lowercase) {
                                k = lowercase;
                            } else {
                                q = lowercase;
                            }
                        }
                    } else if (lowercase == 'k') {
                        k = findPieceFile(kingFile-'a', 7, Piece.rook, rank, squareMap);
                    } else if (lowercase == 'q') {
                        q = findPieceFile(0, kingFile-'a', Piece.rook, rank, squareMap);
                    }
                }
                yield new RookFiles(k, q);
            }
            default -> new RookFiles('h', 'a');
        };
    }

    static char findPieceFile(int rangeStart, int rangeEnd, Piece piece, int rank, Map<Square.Pos, Square<Piece>> squareMap) {
        return (char) IntStream.rangeClosed(rangeStart, rangeEnd)
            .filter(i -> squareMap.get(Square.pos((char) ('a'+i), rank))
                    instanceof Square.With(_, Piece p, _) && p == piece)
            .map(i -> (char) ('a' + i))
            .findAny().orElse(' ');
    }

    record Dir(int dx, int dy) {}

    public Stream<Square.Pos> squaresAttackedByPiece(Square.With<Piece> piece) {
        Square.Pos pos = piece.pos();
        Stream<Square.Pos> unboundedCoordinates = switch(piece.type()) {
            case pawn -> Stream.of(
                    pos.delta(piece.side() == Side.black ? -1 : 1, +1),
                    pos.delta(piece.side() == Side.black ? -1 : 1, -1));
            case bishop -> reachableSquaresInDirectionsFromSquare(pos,
                    List.of(new Dir(-1, 1), new Dir( 1, 1), new Dir(-1,-1), new Dir( 1,-1)));
            case rook   -> reachableSquaresInDirectionsFromSquare(pos,
                    List.of(new Dir( 0, 1), new Dir( 0,-1), new Dir( 1, 0), new Dir(-1, 0)));
            case queen  -> reachableSquaresInDirectionsFromSquare(pos,
                    List.of(new Dir( 0, 1), new Dir( 0,-1), new Dir( 1, 0), new Dir(-1, 0),
                        new Dir(-1, 1), new Dir( 1, 1), new Dir(-1,-1), new Dir( 1,-1)));
            case king -> Stream.of(
                    pos.delta(-1, -1), pos.delta(-1, 0), pos.delta(-1, 1),
                    pos.delta( 0, -1),                   pos.delta( 0, 1),
                    pos.delta( 1, -1), pos.delta( 1, 0), pos.delta( 1, 1));
            case knight -> Stream.of(
                       pos.delta(-2,-1), pos.delta(-2, 1),
                    pos.delta(-1,-2),         pos.delta(-1, 2),
                    pos.delta( 1,-2),         pos.delta( 1, 2),
                       pos.delta( 2,-1), pos.delta( 2, 1));
        };
        return unboundedCoordinates
            .filter(candidate -> candidate.rank() >=  1  && candidate.rank() <=  8
                    && candidate.file() >= 'a' && candidate.file() <= 'h');
    }

    Stream<Square.Pos> reachableSquaresInDirectionsFromSquare(Square.Pos pos, List<Dir> directions) {
        var builder = Stream.<Square.Pos>builder();
        for (Dir dir : directions) {
            int r = pos.rank() + dir.dy();
            char c = (char) (pos.file() + dir.dx());
            while(r >= 1 && r <= 8 && c >= 'a' && c <= 'h') {
                var current = Square.pos(c, r);
                builder.accept(current);
                if (squareMap().get(current) instanceof Square.With) break;
                r += dir.dy();
                c += dir.dx();
            }
        }
        return builder.build();
    }
}
