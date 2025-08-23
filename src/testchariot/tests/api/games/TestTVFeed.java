package tests.api.games;

import util.*;

import java.time.Duration;
import java.util.List;

import chariot.Client;
import chariot.internal.Endpoint;

import chariot.model.Enums.Color;
import chariot.model.*;
import chariot.model.TVFeedEvent.*;

import static util.Assert.*;

public class TestTVFeed {

    @Test
    public void tvFeed() throws Exception {

        // prepare expected response
        var expected = List.of(
                new Featured(
                        "qVSOPtMc", Color.black, List.of(
                            new PlayerInfo(UserInfo.of("lizen9","lizen9", "GM"), Color.white, 2531, Duration.ofSeconds(600)),
                            new PlayerInfo(UserInfo.of("lizen29", "lizen29", "WGM"), Color.black, 2594, Duration.ofSeconds(600))),
                        "rnbqk1r1/ppp1ppbp/8/N2p2p1/8/1PQPP3/P1P2PPn/R1B1K1NR"),
                new Fen("rnbqk1r1/ppp1ppbp/8/N2p2p1/8/1PQPP3/P1P2PPn/R1B1K1NR", "d2d4", Duration.ofSeconds(1), Duration.ofSeconds(1)));

        var responseBodies = List.of(
            """
            {
              "t": "featured",
              "d": {
                "id": "qVSOPtMc",
                "orientation": "black",
                "players": [
                  {
                    "color": "white",
                    "user": {
                      "name": "lizen9",
                      "id": "lizen9",
                      "title": "GM"
                    },
                    "rating": 2531,
                    "seconds": 600
                  },
                  {
                    "color": "black",
                    "user": {
                      "name": "lizen29",
                      "title": "WGM",
                      "id": "lizen29"
                    },
                    "rating": 2594,
                    "seconds": 600
                  }
                ],
                "fen": "rnbqk1r1/ppp1ppbp/8/N2p2p1/8/1PQPP3/P1P2PPn/R1B1K1NR"
              }
            }
            """,
            """
            {
                "t": "fen",
                "d": {
                    "fen": "rnbqk1r1/ppp1ppbp/8/N2p2p1/8/1PQPP3/P1P2PPn/R1B1K1NR",
                    "lm": "d2d4",
                    "wc": 1,
                    "bc": 1
                }
            }
            """
            ).stream().map(Util::compactJson)
            .map(s -> s + "\n")
            .toList();

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameTVFeed.endpoint()),
                    Scenario.header("accept", "application/x-ndjson")),
                List.of(
                    Scenario.status(200),
                    Scenario.streamBodies(responseBodies),
                    Scenario.header("content-type", "application/x-ndjson")
                    ));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.games().tvFeed().stream().toList();
            assertEquals(expected, result);
        }
    }
}
