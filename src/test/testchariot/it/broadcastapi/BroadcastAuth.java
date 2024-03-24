package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.IntegrationTest;
import util.IT;

import static util.Assert.assertTrue;
import static util.Assert.fail;
import static util.Assert.unboxEquals;
import static util.Assert.assertEquals;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.*;

public class BroadcastAuth {

    static ClientAuth client = IT.diego();

    @IntegrationTest
    public void createSimpleBroadcast() {
        String name = "Simple Broadcast";
        String descrShort = "Simple short description";
        String descrLong = "Simple long description";

        var broadcastRequest = client.broadcasts().create(p -> p
                .name(name)
                .shortDescription(descrShort)
                .longDescription(descrLong));

        unboxEquals(broadcastRequest, name,       b -> b.tour().name());
        unboxEquals(broadcastRequest, descrShort, b -> b.tour().description());
        // "<p>Simple long description<p>\n"
        assertTrue(broadcastRequest.map(b -> b.tour().markup()).orElse("").contains(descrLong));
    }

    @IntegrationTest
    public void createSimpleRound() {
        if (! (createNewBroadcast() instanceof Entry(var broadcast))) {
            fail("Failed to create broadcast");
            return;
        }
        String roundName = "Simple Round";
        var roundRequest = client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName));
        unboxEquals(roundRequest, roundName, r -> r.round().name());
    }

    // lila checks for rounds to start syncing, every 1 minute,
    // so this is a long running(waiting) test.
    @IntegrationTest
    public void createWithSyncURL() {
        if (! (createNewBroadcast() instanceof Entry(var broadcast))) {
            fail("Failed to create broadcast");
            return;
        }
        String roundName = "Sync URL Round";

        List<String> pgnResponses = Pgn.readFromString("""
                [Event "Integration Test sync URL"]
                [Round "Sync URL Round"]

                1. d4 *

                [Event "Integration Test sync URL"]
                [Round "Sync URL Round"]

                1. d4 d5 *

                [Event "Integration Test sync URL"]
                [Round "Sync URL Round"]
                [Result "1-0"]

                1. d4 d5 2. a3 1-0
                """).stream().map(Pgn::toString).toList();

        Semaphore threeRequests = new Semaphore(0);
        AtomicInteger responseIndex = new AtomicInteger();
        var httpContext = createPgnContext("/it/games.pgn", exchange -> {
            threeRequests.release();
            int index = Math.min(responseIndex.getAndIncrement(), pgnResponses.size()-1);
            byte[] bytes = pgnResponses.get(index).getBytes();
            exchange.getResponseHeaders().put("content-type", List.of("application/x-chess-pgn"));
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });

        var serverAddress = httpContext.getServer().getAddress();
        URI syncUrl = URI.create("http://" + serverAddress.getHostName() + "/it/games.pgn");

        var roundRequest = client.broadcasts().createRound(broadcast.id(), p -> p
                .name(roundName)
                .syncUrl(syncUrl.toString())
                .startsAt(now -> now.minusHours(1)));

        unboxEquals(roundRequest, false, r -> r.round().finished());

        if (! (roundRequest instanceof Entry(var round))) {
            fail(roundRequest);
            return;
        }

        // It seems lila can't initialize the round from the first poll,
        // so waiting until lila has polled one more time than this test
        // would normally expect - i.e the test wants to say first poll that
        // game is ongoing, and in second poll that there's a result -
        // however after two polls (when lila has gotten the info that there's a result),
        // when querying lila the result of the first poll is received...
        int neededNumberOfPolls = 4;
        try {
            for (int i = 0; i < neededNumberOfPolls; i++) {
                System.out.println("Waiting for lila to poll " + (i+1) + "/" + neededNumberOfPolls);
                if (! threeRequests.tryAcquire(1, 2, TimeUnit.MINUTES)) {
                    fail("Missing poll");
                    return;
                }
            }
        } catch(InterruptedException ie) {}
        httpContext.getServer().stop(0);

        var pgns = client.broadcasts().exportOneRoundPgn(round.id()).stream().toList();

        assertEquals(pgns.size(), 1);
        Pgn pgn = pgns.getFirst();

        var filteredTags = pgn.tags().stream()
            .filter(tag -> Set.of("Event", "Round", "Result").contains(tag.name()))
            .toList();
        var moves = pgn.moves();

        String expectedMatchingPgnString = Pgn.of(filteredTags, moves).toString();

        assertEquals("""
                [Event "Integration Test sync URL"]
                [Round "Sync URL Round"]
                [Result "1-0"]

                1. d4 d5 2. a3 1-0""",
                expectedMatchingPgnString);
    }

    @IntegrationTest
    public void createWithPushPGN() {
        if (! (createNewBroadcast() instanceof Entry(var broadcast))) {
            fail("Failed to create broadcast");
            return;
        }
        String roundName = "Push PGN Round";

        List<Pgn> pgnPush = Pgn.readFromString("""
                [Event "Integration Test Push"]
                [Round "Push Round"]

                1. d4 *

                [Event "Integration Test Push"]
                [Round "Push Round"]

                1. d4 d5 *

                [Event "Integration Test Push"]
                [Round "Push Round"]
                [Result "1-0"]

                1. d4 d5 2. a3 1-0
                """);

        var roundRequest = client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName));

        unboxEquals(roundRequest, false, r -> r.round().finished());

        if (! (roundRequest instanceof Entry(var round))) {
            fail(roundRequest);
            return;
        }

        for (var pgn : pgnPush) {
            if (! (client.broadcasts().pushPgnByRoundId(round.id(), pgn.toString()) instanceof Entries(var stream))) {
                fail(pgn);
                return;
            }
            var results = stream.toList();
            assertEquals(1, results.size());
            assertEquals(pgn.moveListSAN().size(), results.getFirst().moves());
        }

        var pgns = client.broadcasts().exportOneRoundPgn(round.id()).stream().toList();

        assertEquals(pgns.size(), 1);
        Pgn pgn = pgns.getFirst();

        var filteredTags = pgn.tags().stream()
            .filter(tag -> Set.of("Event", "Round", "Result").contains(tag.name()))
            .toList();
        var moves = pgn.moves();

        String expectedMatchingPgnString = Pgn.of(filteredTags, moves).toString();

        assertEquals("""
                [Event "Integration Test Push"]
                [Round "Push Round"]
                [Result "1-0"]

                1. d4 d5 2. a3 1-0""",
                expectedMatchingPgnString);
    }

    static AtomicInteger broadcastCounter = new AtomicInteger();
    private One<Broadcast> createNewBroadcast() {
        int index = broadcastCounter.incrementAndGet();
        return client.broadcasts().create(p -> p
                .name("Broadcast " + index + " IT")
                .shortDescription("Broadcast " + index + " short description IT")
                .longDescription("Broadcast " + index + " long description (markup) IT"));
    }

    static HttpContext createPgnContext(String path, HttpHandler handler) {
        try {
            var server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 80), 0);
            var ctx = server.createContext(path, handler);
            server.start();
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
