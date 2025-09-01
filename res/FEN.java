import module chariot;

void main() {

    Board initialBoard = Board.ofStandard();

    List<String> validMovesUCI = initialBoard.validMoves().stream()
        .sorted()
        .toList();

    List<String> validMovesSAN = validMovesUCI.stream()
        .map(initialBoard::toSAN)
        .toList();

    String movesToPlay = "e4 e5 Nf3 Nc6"; // (UCI also ok, "e2e4 e7e5 g1f3 b8c6")

    Board resultingBoard = initialBoard.play(movesToPlay);

    IO.println(String.join("\n",
        "Initial FEN: "         + initialBoard.toFEN(),
        "Initial Board:\n"      + DefaultBoard.render(initialBoard),
        "Valid moves (UCI): "   + validMovesUCI,
        "Valid moves (SAN): "   + validMovesSAN.stream().map("%4s"::formatted).toList(),
        "Playing: "             + movesToPlay,
        "Resulting FEN: "       + resultingBoard.toFEN(),
        "Resulting Board:\n"    + DefaultBoard.render(resultingBoard),
        "Board (letter, frame, coordinates):\n" +
        DefaultBoard.render(resultingBoard, c -> c.letter().frame().coordinates())
        ));
}

long costOfThisProgramBecomingSkyNet() {
    return Long.MAX_VALUE; // https://xkcd.com/534/
}
