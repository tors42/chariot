package chariot.chess;

public interface FEN {

    static final String standardStr = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    static final FEN standard       = parse(standardStr);

    String positions();
    Side side();
    String castling();
    String ep();
    int halfMove();
    int move();

    static FEN parse(String fen) {
        String[] fields  = fen.split(" ");
        String positions = fields.length > 0 ?           fields[0]  : "";
        Side side        = fields.length > 1 ? parseSide(fields[1]) : Side.white;
        String castling  = fields.length > 2 ?           fields[2]  : "";
        String ep        = fields.length > 3 ?           fields[3]  : "-";
        int halfMove     = fields.length > 4 ?  parseInt(fields[4]) : 0;
        int move         = fields.length > 5 ?  parseInt(fields[5]) : 1;

        return new Simple(positions, side, castling, ep, halfMove, move);
    }

    FEN withPositions(String positions);
    FEN withSide(Side side);
    FEN withCastling(String castling);
    FEN withEP(String ep);
    FEN withHalfMove(int halfMove);
    FEN withMove(int move);

    private static Side parseSide(String field) {
        return "b".equals(field) ? Side.black : Side.white;
    }

    private static int parseInt(String field) {
        try { return Integer.parseInt(field);
        } catch (Exception e) { return -1; }
    }

    record Simple(String positions, Side side, String castling, String ep, int halfMove, int move) implements FEN {
        @Override
        public String toString() {
            return "%s %s %s %s %d %d".formatted(
                    positions,
                    side == Side.black ? 'b' : 'w',
                    castling.isEmpty() ? "-" : castling,
                    ep,
                    halfMove,
                    move);
        }

        public Simple withPositions(String positions) {
            return new Simple(positions, side(), castling(), ep(), halfMove(), move());
        }
        public Simple withSide(Side side) {
            return new Simple(positions(), side, castling(), ep(), halfMove(), move());
        }
        public Simple withCastling(String castling) {
            return new Simple(positions(), side(), castling, ep(), halfMove(), move());
        }
        public Simple withEP(String ep) {
            return new Simple(positions(), side(), castling(), ep, halfMove(), move());
        }
        public Simple withHalfMove(int halfMove) {
            return new Simple(positions(), side(), castling(), ep(), halfMove, move());
        }
        public Simple withMove(int move) {
            return new Simple(positions(), side(), castling(), ep(), halfMove(), move);
        }
    }
}
