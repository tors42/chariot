package tests.model;

import static util.Assert.assertEquals;
import static util.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import chariot.chess.Board;
import chariot.model.PGN;
import util.Test;

public class TestPgn {

    @Test
    public void one() {
        String input = """
[Event "Test"]

1. e4 1-0
""";
        PGN pgn = PGN.read(input);
        String toString = pgn.toString();
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
        List<PGN> pgns = PGN.stream(input).toList();
        String toString = String.join("\n\n", pgns.stream().map(PGN::toString).toList());
        assertEquals(input, toString);
    }

    @Test
    public void threeFile() {
        String input = """
[Event "Test"]

1. e4 1-0


[Event "Test2"]

1. d4 0-1

[Event "Test3"]

1. c4 1/2-1/2
""";

        Path pgnFile;
        try {
             pgnFile = Files.createTempFile("TestPgn-", ".pgn");
             pgnFile.toFile().deleteOnExit();
             Files.writeString(pgnFile, input);
        } catch (IOException ex) {
            fail(ex);
            return;
        }

        try (var stream = PGN.stream(pgnFile)) {
            String toString = String.join("\n\n", stream.map(PGN::toString).toList());
            assertEquals(input, toString);
        }
    }


    @Test
    public void noTags() {
        String input = """
1. e4 1-0
""";
        PGN pgn = PGN.read(input);
        String toString = pgn.toString();
        assertEquals(input, toString);
        assertEquals(true, pgn.tags().isEmpty());
    }

    @Test
    public void board() {
        Board fromStart = Board.ofStandard();
        assertEquals("1. e4 e5 2. Nc3 Nf6 3. Bc4 *", fromStart.toPGN("e2e4 e7e5 b1c3 g8f6 f1c4"));
        assertEquals("1. e4 e5 2. Nc3 Nf6 3. Bc4 *", fromStart.toPGN("e4 e5 Nc3 Nf6 Bc4"));
        assertEquals("1. e4 e5 2. Nc3 *", fromStart.toPGN("e2e4 e7e5 b1c3"));
        assertEquals("1. e4 e5 2. Nc3 *", fromStart.toPGN("e4 e5 Nc3"));

        assertEquals("1. e3 f6 2. Be2 g5 3. Bh5# 1-0", fromStart.toPGN("e2e3 f7f6 f1e2 g7g5 e2h5"));

        assertEquals("1. e3 f6 2. Be2 g5 3. Bh5# 1-0", fromStart.toPGN("e3 f6 Be2 g5 Bh5"));
        assertEquals("1. f3 e6 2. g4 Qh4# 0-1", fromStart.toPGN("f2f3 e7e6 g2g4 d8h4"));
        assertEquals("1. f3 e6 2. g4 Qh4# 0-1", fromStart.toPGN("f3 e6 g4 Qh4"));

        Board fromFen = Board.ofStandard("rnbqkbnr/pppp1ppp/8/4p3/4P3/2N5/PPPP1PPP/R1BQKBNR b KQkq - 1 2");
        assertEquals("2... Nf6 3. Bc4 *", fromFen.toPGN("g8f6 f1c4"));
        assertEquals("2... Nf6 3. Bc4 *", fromFen.toPGN("Nf6 Bc4"));
        assertEquals("2... Nf6 3. Bc4 a6 *", fromFen.toPGN("g8f6 f1c4 a7a6"));
        assertEquals("2... Nf6 3. Bc4 a6 *", fromFen.toPGN("Nf6 Bc4 a6"));
    }

}
