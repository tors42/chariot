package tests.model;

import static util.Assert.assertEquals;

import java.util.List;

import chariot.model.Pgn;
import chariot.util.Board;
import util.Test;

public class TestPgn {

    @Test
    public void one() {
        String input = """
[Event "Test"]

1. e4 1-0
""";
        List<Pgn> pgns = Pgn.readFromString(input);
        String toString = pgns.getFirst().toString();
        assertEquals(input, toString);
    }

    @Test
    public void two() {
        String input = """
[Event "Test"]

1. e4 1-0


[Event "Test2"]

1. d4 0-1
""";

        List<Pgn> pgns = Pgn.readFromString(input);
        String toString = String.join("\n\n", pgns.stream().map(Pgn::toString).toList());
        assertEquals(input, toString);
    }

    @Test
    public void noTags() {
        String input = """
1. e4 1-0
""";
        List<Pgn> pgns = Pgn.readFromString(input);
        String toString = pgns.getFirst().toString();
        assertEquals(input, toString);
        assertEquals(true, pgns.getFirst().tagMap().isEmpty());
    }

    @Test
    public void board() {
        Board fromStart = Board.fromStandardPosition();
        assertEquals("1. e4 e5 2. Nc3 Nf6 3. Bc4 *", fromStart.toPGN("e2e4 e7e5 b1c3 g8f6 f1c4"));
        assertEquals("1. e4 e5 2. Nc3 Nf6 3. Bc4 *", fromStart.toPGN("e4 e5 Nc3 Nf6 Bc4"));
        assertEquals("1. e4 e5 2. Nc3 *", fromStart.toPGN("e2e4 e7e5 b1c3"));
        assertEquals("1. e4 e5 2. Nc3 *", fromStart.toPGN("e4 e5 Nc3"));
        assertEquals("1. e3 f6 2. Be2 g5 3. Bh5# 1-0", fromStart.toPGN("e2e3 f7f6 f1e2 g7g5 e2h5"));
        assertEquals("1. e3 f6 2. Be2 g5 3. Bh5# 1-0", fromStart.toPGN("e3 f6 Be2 g5 Bh5"));
        assertEquals("1. f3 e6 2. g4 Qh4# 0-1", fromStart.toPGN("f2f3 e7e6 g2g4 d8h4"));
        assertEquals("1. f3 e6 2. g4 Qh4# 0-1", fromStart.toPGN("f3 e6 g4 Qh4"));

        Board fromFen = Board.fromFEN("rnbqkbnr/pppp1ppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq - 1 2");
        assertEquals("2... Nf6 3. Bc4 *", fromFen.toPGN("g8f6 f1c4"));
        assertEquals("2... Nf6 3. Bc4 *", fromFen.toPGN("Nf6 Bc4"));
        assertEquals("2... Nf6 3. Bc4 a6 *", fromFen.toPGN("g8f6 f1c4 a7a6"));
        assertEquals("2... Nf6 3. Bc4 a6 *", fromFen.toPGN("Nf6 Bc4 a6"));
    }

}
