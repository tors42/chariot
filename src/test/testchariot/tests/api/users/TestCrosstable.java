package tests.api.users;

import util.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import chariot.Client;
import chariot.model.*;
import chariot.model.Crosstable.*;
import chariot.model.Crosstable.Result;
import chariot.internal.Endpoint;

import static util.Assert.*;

public class TestCrosstable {

    @Test
    public void crosstable() {
        String user1 = "someone";
        String user2 = "else";

        // prepare expected response
        var expected = One.entry(new Crosstable(
                    new Results(
                        new Result("someone", 1.5),
                        new Result("else", 1.5),
                        3),
                    Opt.empty()));

        String responseBody = """
        {
          "users": {
              "someone": 1.5,
              "else": 1.5
          },
          "nbGames": 3
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.crosstableByUserIds.endpoint().formatted(user1, user2)),
                    Scenario.queryParam("matchup", "false"),
                    Scenario.header("accept", "application/json")),
                List.of(
                    Scenario.status(200),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.users().crosstable(user1, user2, params -> params.matchup(false));
            assertEquals(expected, result);
        }
    }

    @Test
    public void crosstableWithMatchup() {
        String user1 = "someone";
        String user2 = "else";

        // prepare expected response
        var expected = One.entry(new Crosstable(
                    new Results(
                        new Result("someone", 1.5),
                        new Result("else", 1.5),
                        3),
                    Opt.some(
                        new Results(
                            new Result("someone", 0),
                            new Result("else", 1),
                            1)
                        )));


        String responseBody = """
        {
          "users": {
              "someone": 1.5,
              "else": 1.5
          },
          "nbGames": 3,
          "matchup": {
              "users": {
                  "someone": 0,
                  "else": 1
              },
              "nbGames": 1
          }
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.crosstableByUserIds.endpoint().formatted(user1, user2)),
                    Scenario.queryParam("matchup", "true"),
                    Scenario.header("accept", "application/json")),
                List.of(
                    Scenario.status(200),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.users().crosstable(user1, user2, params -> params.matchup());
            assertEquals(expected, result);
        }
    }

}
