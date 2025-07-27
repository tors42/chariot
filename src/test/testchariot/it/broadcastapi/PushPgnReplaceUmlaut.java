package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Pgn.Tag;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.util.*;


public class PushPgnReplaceUmlaut {

    static ClientAuth client = IT.bobby();

    static String incomingPGN = """
        [Black "Magnus Carlsen"]
        [White "Matthias Bluebaum"]

        1. d4 d5 *


        [Black "Señor Ramirez"]
        [White "Jose Angel"]

        1. d4 d5 *""";

    static String replacements = """
        Magnus Carlsen / / / 2863
        Senor Ramirez / / / 1812
        José Ángel / / / 2002
        Matthias Blübaum / / / 2649
        """;

    static String expectedResult = """
        [Black "Magnus Carlsen"]
        [BlackElo "2863"]
        [White "Matthias Bluebaum"]
        [WhiteElo "2649"]

        1. d4 d5 *


        [Black "Señor Ramirez"]
        [BlackElo "1812"]
        [White "Jose Angel"]
        [WhiteElo "2002"]

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
                    .map(Pgn::toString)
                    .toList());

        assertEquals(expectedResult, exported);
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

    Broadcast createBroadcast(String replacement) {
        return client.broadcasts().create(params -> params
            .name("Broadcast")
            .description("Testing out replacements umlaut")
            .players(replacement)).get();
    }
}
