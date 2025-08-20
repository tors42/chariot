package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.IT;
import util.IntegrationTest;

import static util.Assert.*;

import java.time.Duration;
import java.util.*;

public class PushPgnDelay {

    static ClientAuth client = IT.bobby();

    static String firstIncomingPGN = """
        [White "Lei Tingjie"]
        [Black "Hou Yifan"]

         *
        """;

    static String secondIncomingPGN = """
        [White "Lei Tingjie"]
        [Black "Hou Yifan"]

        1. d4 d5 *
        """;


    static String expectedEmptyRound = """
         *
        """;
    static String expectedPlayersWithNoMoves = """
        [White "Lei Tingjie"]
        [Black "Hou Yifan"]

         *
        """;

    static String expectedResult = """
        [White "Lei Tingjie"]
        [Black "Hou Yifan"]

        1. d4 d5 *
        """;


    @IntegrationTest
    public void pushPgnNoDelay() {
        var round = createRound();
        assertEquals(expectedEmptyRound, exportRoundPgn((round.id())));

        client.broadcasts().pushPgnByRoundId(round.id(), firstIncomingPGN);
        assertEquals(firstIncomingPGN, exportRoundPgn((round.id())));

        client.broadcasts().pushPgnByRoundId(round.id(), secondIncomingPGN);
        assertEquals(expectedResult, exportRoundPgn((round.id())));
    }

    @IntegrationTest
    public void pushPgnDelay() throws InterruptedException {
        var delay = Duration.ofSeconds(2);
        var round = createRoundDelay(delay);
        assertEquals(expectedEmptyRound, exportRoundPgn((round.id())));

        client.broadcasts().pushPgnByRoundId(round.id(), firstIncomingPGN);
        assertEquals(expectedPlayersWithNoMoves, exportRoundPgn((round.id())));

        Thread.sleep(delay.plusMillis(200));
        assertEquals(expectedPlayersWithNoMoves, exportRoundPgn((round.id())));

        client.broadcasts().pushPgnByRoundId(round.id(), secondIncomingPGN);
        assertEquals(expectedPlayersWithNoMoves, exportRoundPgn((round.id())));

        Thread.sleep(delay.plusMillis(200));
        assertEquals(expectedResult, exportRoundPgn((round.id())));
    }


    String exportRoundPgn(String roundId) {
        return String.join("\n\n",
                client.broadcasts().exportOneRoundPgn(roundId).stream()
                 .map(this::filterExportedPgn)
                 .map(PGN::toString)
                 .toList());
    }

    static Set<String> tagsToValidate = Set.of(
            "White", "WhiteElo",
            "Black", "BlackElo"
            );

    PGN filterExportedPgn(PGN pgn) {
        pgn.withTags(s -> s.filter(_ -> true));
        return pgn.filterTags((tag, _) -> tagsToValidate.contains(tag));
    }

    MyRound createRound() {
        return client.broadcasts().createRound(createBroadcast().id(),
                p -> p.name("Round 1")).get();
    }

    MyRound createRoundDelay(Duration delay) {
        return client.broadcasts().createRound(createBroadcast().id(),
                p -> p.name("Round 1").delay(delay)).get();
    }

    Broadcast createBroadcast() {
        return client.broadcasts().create(params -> params
            .name("Broadcast")
            .description("Testing PushPgnDelay")).get();
    }
}
