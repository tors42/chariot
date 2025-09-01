package chariot.chess;

import module java.base;

import chariot.internal.chess.InternalDefaultBoard;

public interface DefaultBoard extends PieceTypedBoard<Piece> {

    DefaultBoard play(String... uciOrSan);

    default DefaultBoard play(Square.Pos from, Square.Pos to) {
        return play(from.toString() + to.toString());
    }
    default DefaultBoard play(Square.Pos from, Square.Pos to, Piece promotion) {
        return play(from.toString() + to.toString() + promotion.toChar());
    }
    default DefaultBoard play(Square<Piece> from, Square<Piece> to) {
        return play(from.pos(), to.pos());
    }
    default DefaultBoard play(Square<Piece> from, Square<Piece> to, Piece promotion) {
        return play(from.pos(), to.pos(), promotion);
    }

    String toSAN(String move);
    String toUCI(String move);
    String toFEN();
    String variant();
    Collection<String> validMoves();
    Side sideToMove();

    List<String> historyFEN();
    List<String> historyMove();

    default boolean whiteToMove() { return sideToMove() == Side.white; }
    default boolean blackToMove() { return !whiteToMove(); }

    static DefaultBoard ofStandard() {
        return of(Board.ofStandard());
    }

    static DefaultBoard of(Board board) {
        return InternalDefaultBoard.of(board);
    }

    static DefaultBoard fromFEN(String fen) {
        return of(Board.fromFEN(fen));
    }



    static Map<Square.Pos, Square<Piece>> fenPositionsToSquares(String positionsOrFen) {
        // in case complete FEN, just keep positions part
        String positions = positionsOrFen.trim().split(" ")[0];

        List<Square<Piece>> squareList = new ArrayList<>();
        String[] ranks = positions.split("/");
        for (int rank = ranks.length-1; rank >= 0; rank--) {
            int file = 0;
            for (char c : ranks[rank].toCharArray()) {
                if (c >= '1' && c <= '8') {
                    int numEmptySquares = Character.getNumericValue(c);
                    for (int i = 0; i < numEmptySquares; i++) {
                        squareList.add(Square.empty((char)('a'+i+file) , 8 - rank));
                    }
                    file += numEmptySquares;
                } else {
                    if (Piece.fromCharWithSide(c) instanceof Piece.PieceAndSide(Piece piece, Side side)) {
                        squareList.add(Square.withPiece((char) ('a'+file), 8 - rank, piece, side));
                    }
                    file++;
                }
            }
        }

        Comparator<Square<Piece>> rank = Comparator.comparing(s -> s.pos().rank());
        Comparator<Square<Piece>> file = Comparator.comparing(s -> s.pos().file());

        Map<Square.Pos, Square<Piece>> squareMap = squareList.stream().sorted(file.thenComparing(rank))
            .collect(Collectors.toMap(Square::pos, Function.identity(), (orig,_) -> orig, LinkedHashMap::new));

        return squareMap;
    }

    static String squaresToFenPositions(Map<Square.Pos, Square<Piece>> squareMap) {
        List<String> rows = new ArrayList<>(8);
        for (int rank = 7; rank >= 0; rank--) {
            String line = "";
            int empty = 0;
            for (int file = 0; file <= 7; file++) {
                if (! (squareMap.get(Square.pos((char) ('a' + file), rank+1)) instanceof Square.With(_, Piece piece, Side side))) {
                    empty++;
                    continue;
                }
                if (empty > 0) {
                    line += empty;
                    empty = 0;
                }
                line += piece.toChar(side);
            }
            if (empty > 0) {
                line += empty;
            }
            rows.add(line);
        }
        return String.join("/", rows);
    }

    static String noframeTemplate = """
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s""";

    static String frameTemplate = """
        ┌───┬───┬───┬───┬───┬───┬───┬───┐
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        └───┴───┴───┴───┴───┴───┴───┴───┘""";

    public interface Config {
        Config frame(boolean frame);
        Config letter(boolean letter);
        Config coordinates(boolean coordinates);
        Config flipped(boolean flipped);

        default Config frame() { return frame(true); }
        default Config letter() { return letter(true); }
        default Config coordinates() { return coordinates(true); }
        default Config flipped() { return flipped(true); }
    }

    public static String render(Board board) {
        return render(board, _ -> {});
    }

    public static String render(Board board, Consumer<Config> config) {
        var toConsume = new Config() {
            Data mutate = new Data(false, false, false, false);
            @Override public Config frame(boolean frame)             { mutate = mutate.with(new Data.Frame(frame));             return this; }
            @Override public Config letter(boolean letter)           { mutate = mutate.with(new Data.Letter(letter));           return this; }
            @Override public Config coordinates(boolean coordinates) { mutate = mutate.with(new Data.Coordinates(coordinates)); return this; }
            @Override public Config flipped(boolean flipped)         { mutate = mutate.with(new Data.Flipped(flipped));         return this; }
        };
        config.accept(toConsume);
        Data data = toConsume.mutate;
        return render(board, data);
    }

    record Data(boolean frame, boolean letter, boolean coordinates, boolean flipped) {

        sealed interface Component {}

        record Frame(boolean value)       implements Component {}
        record Letter(boolean value)      implements Component {}
        record Coordinates(boolean value) implements Component {}
        record Flipped(boolean value)     implements Component {}

        Data with(Component component) {
            return new Data(
                    component instanceof Frame       f ? f.value : frame,
                    component instanceof Letter      u ? u.value : letter,
                    component instanceof Coordinates c ? c.value : coordinates,
                    component instanceof Flipped     f ? f.value : flipped
                    );
        }

        Data with(Component... components) {
            var copy = this;
            for (var component : components) copy = copy.with(component);
            return copy;
        }

    }

    static String toLetter(Square.With<Piece> square) {
        return String.valueOf(square.type().toChar(square.side()));
    }
    static String toUnicode(Square.With<Piece> square) {
        return switch(square.type()) {
            case pawn   -> square.side() == Side.black ? "♟" : "♙";
            case knight -> square.side() == Side.black ? "♞" : "♘";
            case bishop -> square.side() == Side.black ? "♝" : "♗";
            case rook   -> square.side() == Side.black ? "♜" : "♖";
            case queen  -> square.side() == Side.black ? "♛" : "♕";
            case king   -> square.side() == Side.black ? "♚" : "♔";
        };
    }

    private static String render(Board board, Data config) {
        Function<Square.With<Piece>, String> render = p ->
            (config.letter()
                ? toLetter(p)
                : toUnicode(p)
            )
            + (config.frame() ? " " : "");

        Supplier<String> empty = () -> config.frame() ? "  " : " ";
        String template = config.frame() ? frameTemplate : noframeTemplate;

        if (config.coordinates()) {
            var withoutCoordinates = template.lines().toList();
            var withCoordinates = new ArrayList<String>();

            Comparator<String> rankComparator = config.flipped() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            Comparator<String> fileComparator = config.flipped() ? Comparator.reverseOrder() : Comparator.naturalOrder();

            var ranks = Arrays.stream("12345678".split("")).sorted(rankComparator).iterator();
            var files = Stream.concat(Stream.of(" "), Arrays.stream("abcdefgh".split("")).sorted(fileComparator)).toList();

            for (int line = 0; line < withoutCoordinates.size(); line++) {
                boolean renderRank = (!config.frame()) || line % 2 != 0;
                String prefix = renderRank ? ranks.next() : " ";
                withCoordinates.add(prefix + " " + withoutCoordinates.get(line) + "\n");
            }
            withCoordinates.add(String.join("%1$s", files).formatted(config.frame() ? "   " : " "));
            template = withCoordinates.stream().collect(Collectors.joining());
        }
        DefaultBoard standardBoard = DefaultBoard.of(board);

        Map<Square.Pos, Square<Piece>> map = standardBoard.squares().all()
            .stream().collect(Collectors.toMap(Square::pos, Function.identity()));

        var pieces = new ArrayList<String>();
        if (! config.flipped()) {
            for (int row = 7; row >= 0; row--)
                for (int col = 0; col <= 7; col++)
                    pieces.add(map.get(Square.pos((char)('a' + col), row+1)) instanceof Square.With<Piece> with ? (render.apply(with)) : empty.get());
        } else {
            for (int row = 0; row <= 7; row++)
                for (int col = 7; col >= 0; col--)
                    pieces.add(map.get(Square.pos((char)('a' + col), row+1)) instanceof Square.With<Piece> with ? (render.apply(with)) : empty.get());
        }
        return template.formatted(pieces.toArray());
    }
}
