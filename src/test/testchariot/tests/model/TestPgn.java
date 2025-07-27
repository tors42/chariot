package tests.model;

import static util.Assert.assertEquals;

import java.util.List;

import chariot.model.Pgn;
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

}
