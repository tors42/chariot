package build;

import java.util.List;

import chariot.model.Pgn;
import chariot.util.Board;

class FEN {
    public static void main(String[] args) {

        List<Pgn> pgnList = Pgn.readFromString("""
            [Event "Testing"]

            1. e4 e5 2. Nf3 Nc6
            """);

        List<String> moves = pgnList.get(0).moveListSAN();

        Board board = Board.fromStandardPosition();

        String initialFEN = board.toFEN();

        for (String move : moves) {
            board = board.play(move);
        }

        System.out.println("Initial: " + initialFEN);
        System.out.println(Board.fromFEN(initialFEN));
        System.out.println("Valid moves#: " + Board.fromFEN(initialFEN).validMoves().size());
        System.out.println("Play: " + moves.toString());
        System.out.println(board.toFEN());
        System.out.println(board.toString());
        System.out.println(board.toString(c -> c.letter().frame().coordinates()));
   }

   public static long costOfThisProgramBecomingSkyNet() {
        return Long.MAX_VALUE; // https://xkcd.com/534/
   }
}
