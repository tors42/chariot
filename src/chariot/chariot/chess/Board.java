package chariot.chess;

import module java.base;

import chariot.internal.chess.InternalBoardProvider;

public interface Board {

    static Board ofStandard() {
        return BoardProvider.providers()
            .getOrDefault("standard", InternalBoardProvider.provider())
            .init("standard");
    }

    static Board ofStandard(String fen) {
        return ofVariantAndFEN("standard", fen)
            .orElseGet(() -> InternalBoardProvider.provider().fromFEN("standard", fen));
    }

    static Board ofChess960(int position) {
        BoardProvider provider = BoardProvider.providers().get("chess960");
        if (provider instanceof Chess960BoardProvider chess960) {
            return chess960.fromPosition(position);
        }
        if (provider != null) {
            return provider.fromFEN("chess960", InternalBoardProvider.provider().positionToFEN(position));
        }
        return InternalBoardProvider.provider().fromPosition(position);
    }

    static Board ofChess960(String fen) {
        return ofVariantAndFEN("chess960", fen)
            .orElseGet(() -> InternalBoardProvider.provider().fromFEN("chess960", fen));
    }

    static Optional<Board> ofVariantAndFEN(String variant, String fen) {
        return Optional.ofNullable(BoardProvider.providers().get(variant)).map(provider -> provider.fromFEN(variant, fen));
    }

    static Board fromFEN(String fen) { return ofStandard(fen); }

    Board play(Move move);

    String toSAN(Move move);
    String toUCI(Move move);
    String toFEN();
    Collection<String> validMoves();
    String variant();

    default List<? extends Move> asMoves(String... moves) {
        return Arrays.stream(moves)
            .flatMap(s -> Arrays.stream(s.split(" ")))
            .filter(s -> !s.isEmpty())
            .map(Move::wrap)
            .toList();
    }

    default Side sideToMove() {
        return toFEN().contains(" w ") ? Side.white : Side.black;
    }

    default int moveNum() {
        return FEN.parse(toFEN()).move();
    }

    default Board play(String... moves) {
        return asMoves(moves).stream()
            .gather(Gatherers.fold(() -> this, (board, move) -> board.play(move)))
            .findFirst().orElse(this);
    }

    default String toFEN(String... moves) {
        return play(moves).toFEN();
    }

    default String toSAN(String... moves) {
        List<? extends Move> list = asMoves(moves);
        if (list.isEmpty()) return "";
        return list.stream().skip(1)
            .gather(Gatherers.fold(
                () -> toSAN(list.getFirst()),
                (sans, next) -> String.join(" ", sans, play(sans).toSAN(next))))
            .findFirst().orElse("");
    }

    default String toUCI(String... moves) {
        List<? extends Move> list = asMoves(moves);
        if (list.isEmpty()) return "";
        return list.stream().skip(1)
            .gather(Gatherers.fold(
                () -> toUCI(list.getFirst()),
                (ucis, next) -> String.join(" ", ucis, play(ucis).toUCI(next))))
            .findFirst().orElse("");
    }

    default String toPGN(String... moves) {
        Board end = play(moves);
        List<String> sans = Arrays.stream(toSAN(moves).split(" "))
            .filter(s -> !s.isEmpty())
            .toList();

        FEN currentFEN = FEN.parse(toFEN());
        FEN endFEN = FEN.parse(end.toFEN());

        String lastMove = sans.isEmpty() ? "" : sans.getLast();

        String result = switch(Boolean.valueOf(end.validMoves().isEmpty())) {
            case Boolean movesAvailable when movesAvailable ->
                switch (Boolean.valueOf(lastMove.contains("#"))) {
                    case Boolean isMate when isMate ->
                        switch(endFEN.side()) {
                            case white -> "0-1";
                            case black -> "1-0";
                        };
                    case Boolean _ -> "1/2-1/2";
                };
            case Boolean _ -> "*";
        };

        if (sans.isEmpty()) return result;

        StringBuilder sb = new StringBuilder();
        int move = currentFEN.move();

        if (currentFEN.side() == Side.black) {
            sb.append("%d... %s ".formatted(move, sans.getFirst()));
            move++;
        }
        for (int i = currentFEN.side() == Side.black ? 1 : 0; i < sans.size(); i+=2) {
            if (i+1 < sans.size()) sb.append("%d. %s %s ".formatted(move, sans.get(i), sans.get(i+1)));
            else sb.append("%d. %s ".formatted(move, sans.get(i)));
            move++;
        }
        return sb.toString() + result;
    }
}
