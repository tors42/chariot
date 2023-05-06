package tests.api.games;

import util.*;

import java.util.List;

import chariot.Client;
import chariot.model.*;
import chariot.model.Err.Info;
import chariot.internal.Endpoint;

import static util.Assert.*;

public class TestGameImport {

    @Test
    public void gameImportAuth() throws Exception {

        // the game pgn to import
        String pgn = "1. d4";
        String requestBody = "%s=%s".formatted("pgn", pgn.transform(Util::urlEncode));
        String token = "abcdefghijklmnop";

        // prepare expected response
        var expected = One.entry(new GameImport("R6iLjwz5", "https://lichess.org/R6iLjwz5"));

        String responseBody = """
        {
            "id": "R6iLjwz5",
            "url": "https://lichess.org/R6iLjwz5"
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameImport.endpoint()),
                    Scenario.body(requestBody),
                    Scenario.header("content-length", String.valueOf(requestBody.length())),
                    Scenario.header("content-type", "application/x-www-form-urlencoded"),
                    Scenario.header("accept", "application/json"),
                    Scenario.header("authorization", "Bearer %s".formatted(token))),
                List.of(
                    Scenario.status(200),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri())).withToken(token);
            var result = client.games().importGame(pgn);
            assertEquals(expected, result);
        }
    }

    @Test
    public void gameImportAuthInvalidPGN() throws Exception {

        // the game pgn to import
        String pgn = "1. ";
        String requestBody = "%s=%s".formatted("pgn", pgn.transform(Util::urlEncode));
        String token = "abcdefghijklmnop";

        // prepare expected response
        var expected = One.fail(400, new Info("""
                    {"error":{"pgn":["Invalid PGN"]}}"""));

        String responseBody = """
        {
            "error": {
                "pgn": [
                    "Invalid PGN"
                ]
            }
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameImport.endpoint()),
                    Scenario.body(requestBody),
                    Scenario.header("content-length", String.valueOf(requestBody.length())),
                    Scenario.header("content-type", "application/x-www-form-urlencoded"),
                    Scenario.header("accept", "application/json"),
                    Scenario.header("authorization", "Bearer %s".formatted(token))),
                List.of(
                    Scenario.status(400),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri())).withToken(token);
            var result = client.games().importGame(pgn);
            assertEquals(expected, result);
        }
    }

    @Test
    public void gameImportAuthMissingToken() throws Exception {

        // the game pgn to import
        String pgn = "1. d4";
        String requestBody = "%s=%s".formatted("pgn", pgn.transform(Util::urlEncode));
        String token = "non-existing-token";

        // prepare expected response
        var expected = One.fail(401, new Info("""
                    {"error":"No such token"}"""));

        String responseBody = """
        {
            "error": "No such token"
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameImport.endpoint()),
                    Scenario.body(requestBody),
                    Scenario.header("content-length", String.valueOf(requestBody.length())),
                    Scenario.header("content-type", "application/x-www-form-urlencoded"),
                    Scenario.header("accept", "application/json"),
                    Scenario.header("authorization", "Bearer %s".formatted(token))),
                List.of(
                    Scenario.status(401),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri())).withToken(token);
            var result = client.games().importGame(pgn);
            assertEquals(expected, result);
        }
    }

    @Test
    public void gameImportAnonymous() throws Exception {

        // the game pgn to import
        String pgn = "1. d4";
        String requestBody = "%s=%s".formatted("pgn", pgn.transform(Util::urlEncode));

        // prepare expected response
        var expected = One.entry(new GameImport("R6iLjwz5", "https://lichess.org/R6iLjwz5"));

        String responseBody = """
        {
            "id": "R6iLjwz5",
            "url": "https://lichess.org/R6iLjwz5"
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameImport.endpoint()),
                    Scenario.body(requestBody),
                    Scenario.header("content-length", String.valueOf(requestBody.length())),
                    Scenario.header("content-type", "application/x-www-form-urlencoded"),
                    Scenario.header("accept", "application/json")),
                List.of(
                    Scenario.status(200),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.games().importGame(pgn);
            assertEquals(expected, result);
        }
    }

    @Test
    public void gameImportAnonymousInvalidPGN() throws Exception {

        // the game pgn to import
        String pgn = "1. ";
        String requestBody = "%s=%s".formatted("pgn", pgn.transform(Util::urlEncode));

        // prepare expected response
        var expected = One.fail(400, new Info("""
                    {"error":{"pgn":["Invalid PGN"]}}"""));

        String responseBody = """
        {
            "error": {
                "pgn": [
                    "Invalid PGN"
                ]
            }
        }
        """.transform(Util::compactJson); // single-line

        var scenario = Scenario.single(
                List.of(
                    Scenario.path(Endpoint.gameImport.endpoint()),
                    Scenario.body(requestBody),
                    Scenario.header("content-length", String.valueOf(requestBody.length())),
                    Scenario.header("content-type", "application/x-www-form-urlencoded"),
                    Scenario.header("accept", "application/json")),
                List.of(
                    Scenario.status(400),
                    Scenario.body(responseBody),
                    Scenario.header("content-type", "application/json")));

        // Start the server and verify the request + response
        try (var stubServer = StubServer.start(scenario)) {
            var client = Client.basic(c -> c.api(stubServer.uri()));
            var result = client.games().importGame(pgn);
            assertEquals(expected, result);
        }
    }

}
