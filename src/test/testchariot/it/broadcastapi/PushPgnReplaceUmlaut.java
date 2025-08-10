package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.util.*;


public class PushPgnReplaceUmlaut {

    static ClientAuth client = IT.bobby();

    static String incomingPGN = """
        [White "Matthias Bluebaum"]
        [Black "Magnus Carlsen"]

        1. d4 d5 *


        [White "Jose Angel"]
        [Black "Señor Ramirez"]

        1. d4 d5 *""";

    static String replacements = """
        Magnus Carlsen / / / 2863
        Senor Ramirez / / / 1812
        José Ángel / / / 2002
        Matthias Blübaum / / / 2649
        """;

    static String expectedResult = """
        [White "Matthias Bluebaum"]
        [Black "Magnus Carlsen"]
        [WhiteElo "2649"]
        [BlackElo "2863"]

        1. d4 d5 *


        [White "Jose Angel"]
        [Black "Señor Ramirez"]
        [WhiteElo "2002"]
        [BlackElo "1812"]

        1. d4 d5 *
        """;

    @IntegrationTest
    public void pushPgnReplacements() {
        var broadcast = createBroadcast(replacements);
        var round = client.broadcasts().createRound(broadcast.id(), p -> p.name("Round 1")).get();

        client.broadcasts().pushPgnByRoundId(round.id(), incomingPGN);

        String exported = String.join("\n\n",
                client.broadcasts().exportPgn(broadcast.id()).stream()
                    .map(this::filterExportedPgn)
                    .map(PGN::toString)
                    .toList());

        assertEquals(expectedResult, exported);
    }

    static Set<String> tagsToValidate = Set.of(
            "White", "WhiteElo",
            "Black", "BlackElo"
            );

    PGN filterExportedPgn(PGN pgn) {
        return pgn.filterTags((tag, _) -> tagsToValidate.contains(tag));
    }

    Broadcast createBroadcast(String replacement) {
        return client.broadcasts().create(params -> params
            .name("Broadcast")
            .description("Testing out replacements umlaut")
            .players(replacement)).get();
    }
}
