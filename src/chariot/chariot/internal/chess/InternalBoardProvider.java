package chariot.internal.chess;

import module java.base;

import module chariot;

public class InternalBoardProvider implements Chess960BoardProvider {

    private static final InternalBoardProvider instance = new InternalBoardProvider();
    public static InternalBoardProvider provider() { return instance; }

    @Override
    public Set<String> supportedVariants() {
        return Set.of("standard",
                      "chess960",
                      "fromPosition");
    }

    @Override
    public Board init(String variant) {
        return switch(variant) {
            case "standard"     -> fromFEN(variant, FEN.standard.toString());
            case "chess960"     -> random960();
            case "fromPosition" -> fromFEN(variant, FEN.standard.toString());
            default -> null;
        };
    }

    @Override
    public Board fromFEN(String variant, String fen) {
        return switch(variant) {
            case "standard"     -> NaiveChess.of(variant, fen);
            case "chess960"     -> NaiveChess.of(variant, fen);
            case "fromPosition" -> NaiveChess.of(variant, fen);
            default -> null;
        };
    }
}
