package build;

import java.util.List;

import chariot.util.Board;

class FEN {
    public static void main(String[] args) {

        Board initialBoard = Board.fromStandardPosition();

        List<String> validMovesUCI = initialBoard.validMoves().stream()
            .map(Board.Move::uci)
            .sorted()
            .toList();

        List<String> validMovesSAN = validMovesUCI.stream()
            .map(initialBoard::toSAN)
            .toList();

        String movesToPlay = "e4 e5 Nf3 Nc6"; // (UCI also ok, "e2e4 e7e5 g1f3 b8c6")

        Board resultingBoard = initialBoard.play(movesToPlay);

        System.out.println(String.join("\n",
                "Initial FEN: "         + initialBoard.toFEN(),
                "Initial Board:\n"      + initialBoard,
                "Valid moves (UCI): "   + validMovesUCI,
                "Valid moves (SAN): "   + validMovesSAN.stream().map("%4s"::formatted).toList(),
                "Playing: "             + movesToPlay,
                "Resulting FEN: "       + resultingBoard.toFEN(),
                "Resulting Board:\n"    + resultingBoard,
                "Board (letter, frame, coordinates):\n" +
                resultingBoard.toString(c -> c.letter().frame().coordinates())
                ));
    }

   public static long costOfThisProgramBecomingSkyNet() {
        return Long.MAX_VALUE; // https://xkcd.com/534/
   }
}
