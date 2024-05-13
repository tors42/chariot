package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Pgn.Tag;
import util.IntegrationTest;

import static util.Assert.*;

import java.time.Duration;
import java.util.*;

public class PushPgnDelay {

    static ClientAuth client() {
        // Note,
        // using a newly initialized client for each request,
        // to bypass the automatic throttling of max 1 request per second.
        return ClientAuth.auth(conf -> conf.api(util.Main.itApi()), "lip_bobby");
    }

    static String firstIncomingPGN = """
        [Black "Hou Yifan"]
        [White "Lei Tingjie"]

         *""";

    static String secondIncomingPGN = """
        [Black "Hou Yifan"]
        [White "Lei Tingjie"]

        1. d4 d5 *""";


    static String expectedNotStartedResult = "\n\n *";

    static String expectedResult = """
        [Black "Hou Yifan"]
        [White "Lei Tingjie"]

        1. d4 d5 *""";


    @IntegrationTest
    public void pushPgnNoDelay() {
        var round = createRound();
        assertEquals(expectedNotStartedResult, exportRoundPgn((round.id())));

        client().broadcasts().pushPgnByRoundId(round.id(), firstIncomingPGN);
        assertEquals(firstIncomingPGN, exportRoundPgn((round.id())));

        client().broadcasts().pushPgnByRoundId(round.id(), secondIncomingPGN);
        assertEquals(expectedResult, exportRoundPgn((round.id())));
    }

    @IntegrationTest
    public void pushPgnDelay() throws InterruptedException {
        var delay = Duration.ofSeconds(2);
        var round = createRoundDelay(delay);
        assertEquals(expectedNotStartedResult, exportRoundPgn((round.id())));

        client().broadcasts().pushPgnByRoundId(round.id(), firstIncomingPGN);
        assertEquals(expectedNotStartedResult, exportRoundPgn((round.id())));

        Thread.sleep(delay.plusMillis(200));
        assertEquals(firstIncomingPGN, exportRoundPgn((round.id())));

        client().broadcasts().pushPgnByRoundId(round.id(), secondIncomingPGN);
        assertEquals(firstIncomingPGN, exportRoundPgn((round.id())));

        Thread.sleep(delay.plusMillis(200));
        assertEquals(expectedResult, exportRoundPgn((round.id())));
    }


    String exportRoundPgn(String roundId) {
        return String.join("\n\n",
                client().broadcasts().exportOneRoundPgn(roundId).stream()
                 .map(this::filterExportedPgn)
                 .map(Pgn::toString)
                 .toList());
    }

    static Set<String> tagsToValidate = Set.of(
            "White", "WhiteElo",
            "Black", "BlackElo"
            );

    Pgn filterExportedPgn(Pgn pgn) {
        return Pgn.of(pgn.tags().stream()
                .filter(tag -> tagsToValidate.contains(tag.name()))
                .sorted(Comparator.comparing(Tag::name))
                .toList(),
                pgn.moves());
    }

    MyRound createRound() {
        return client().broadcasts().createRound(createBroadcast().id(),
                p -> p.name("Round 1")).get();
    }

    MyRound createRoundDelay(Duration delay) {
        return client().broadcasts().createRound(createBroadcast().id(),
                p -> p.name("Round 1").delay(delay)).get();
    }

    Broadcast createBroadcast() {
        return client().broadcasts().create(params -> params
            .name("Broadcast")
            .shortDescription("A broadcast for PushPgnDelay test")
            .longDescription("Testing PushPgnDelay")).get();
    }
}
