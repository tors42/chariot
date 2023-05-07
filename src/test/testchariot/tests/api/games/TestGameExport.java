package tests.api.games;

import util.*;

import java.util.*;

import chariot.Client;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.model.Game.*;
import chariot.model.UserData.UserPropertyEnum;
import chariot.internal.Endpoint;

import static util.Assert.*;

public class TestGameExport {

    @Test
    public void gameExportJsonFilters() {

        String gameId = "q7ZvsdUF";


        // prepare expected response
        var expected = One.entry(
                new Game("q7ZvsdUF", true, GameVariant.standard, "blitz", "blitz",
                    1514505150384l, 1514505592843l, Status.draw,
                    new Game.Players(
                        new Player.User(LightUser.of("lance5500", "Lance5500", "LM", true), 2389, 4, false,
                            Optional.empty(), Optional.empty(), Optional.empty()),
                        new Player.User(LightUser.of("tryinghard87", "TryingHard87", "", false), 2498, -4, false,
                            Optional.empty(), Optional.empty(), Optional.empty())
                        ),
                    "",
                    "", "", "",
                    "winter17",
                    "", 0, null,
                    null,
                    new Game.Clock(300, 3, 420),
                    List.of(),
                    List.of()
                    ));

        String responseBody = """
        {
          "id": "q7ZvsdUF",
          "rated": true,
          "variant": "standard",
          "speed": "blitz",
          "perf": "blitz",
          "createdAt": 1514505150384,
          "lastMoveAt": 1514505592843,
          "status": "draw",
          "players": {
            "white": {
              "user": {
                "name": "Lance5500",
                "title": "LM",
                "patron": true,
                "id": "lance5500"
              },
              "rating": 2389,
              "ratingDiff": 4
            },
            "black": {
              "user": {
                "name": "TryingHard87",
                "id": "tryinghard87"
              },
              "rating": 2498,
              "ratingDiff": -4
            }
          },
          "tournament": "winter17",
          "clock": {
            "initial": 300,
            "increment": 3,
            "totalTime": 420
          }
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameById.endpoint().formatted(gameId)),
                    Scenario.queryParam("clocks", "false"),
                    Scenario.queryParam("evals", "false"),
                    Scenario.queryParam("moves", "false"),
                    Scenario.queryParam("accuracy", "false"),
                    Scenario.queryParam("literate", "false"),
                    Scenario.queryParam("opening", "false"),
                    Scenario.queryParam("tags", "false"),
                    Scenario.header("accept", "application/json")),
                List.of(
                    Scenario.status(200),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.games().byGameId(gameId, params -> params
                    .clocks(false)
                    .evals(false)
                    .moves(false)
                    .accuracy(false)
                    .literate(false)
                    .opening(false)
                    .tags(false)
                    );
            assertEquals(expected, result);
        }
    }
}
