package it.tournamentapi;

import chariot.ClientAuth;
import chariot.chess.Board;
import chariot.model.*;
import chariot.model.Swiss.Stats;
import util.*;
import static util.Assert.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class SwissStats {

    // Hmm, fails for "ghcr.io/lichess-org/lila-docker:main",
    //      but works with local lila-docker... what is different... *ponder ponder*
    //
    //@IntegrationTest
    public void ongoingGame() {
        Opt<GameTestRunner> noGameRunner = Opt.of();
        var res = runSwiss(noGameRunner);
        if (! (res instanceof Some(Swiss swiss))) {
            fail("Failed to run Swiss: " + res);
            return;
        }

        assertEquals(2, swiss.tourInfo().nbPlayers(), () -> "Two players should have joined");
        assertEquals(1, swiss.nbOngoing(),            () -> "One game should be running");
        assertEquals(1, swiss.round(),                () -> "Expected round 1");


        //var client = IT.clientAuthByUserId(swiss.tourInfo().createdBy());
        //boolean debug = true;
        //if (debug) client.logging(l -> l.request().all().response().all());
        //var fetchRes = client.tournaments().swissById(swiss.id());
        //if (debug) client.logging(l -> l.request().warning().response().warning());
        //System.out.println(fetchRes);

        IT.clientAuthByUserId(swiss.tourInfo().createdBy()).tournaments().terminateSwiss(swiss.id());
    }


    // Hmm, no Stats...
    // When are Stats supposed to be present?
    //@IntegrationTest
    public void drawnGame() {

        Opt<GameTestRunner> draw = Opt.of(new GameTestRunner(PGN.read(pgnDraw)));

        var res = runSwiss(draw);
        if (! (res instanceof Some(Swiss swiss))) {
            fail("Failed to run Swiss: " + res);
            return;
        }

        assertEquals(2, swiss.tourInfo().nbPlayers(), () -> "Two players should have joined");
        assertEquals(1, swiss.nbOngoing(),            () -> "One game should be running");
        assertEquals(2, swiss.round(),                () -> "Expected round 2");

        //var client = IT.clientAuthByUserId(swiss.tourInfo().createdBy());
        //boolean debug = true;
        //if (debug) client.logging(l -> l.request().all().response().all());
        //var fetchRes = client.tournaments().swissById(swiss.id());
        //if (debug) client.logging(l -> l.request().warning().response().warning());
        //System.out.println(fetchRes);


        if (! (swiss.stats() instanceof Some(Stats stats))) {
            fail("There are no Stats");
            return;
        }

        assertEquals(1, stats.draws(), () -> "One game should be drawn");

        IT.clientAuthByUserId(swiss.tourInfo().createdBy()).tournaments().terminateSwiss(swiss.id());
    }

    One<Swiss> runSwiss(Opt<GameTestRunner> gameTestRunner) {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            return One.fail(-1, "Couldn't find team leader for creating a swiss");
        }
        if (! (IT.findPlayers(StatsPerfType.rapid) instanceof Some(IT.Players(var white, var whiteId, var black, var blackId)))) {
            return One.fail(-1, "Couldn't find players for swiss");
        }

        for (var c : List.of(white, black))
            if (c.teams().joinTeam(teamId, p -> p.message("please" + "!".repeat(30))) instanceof Fail<?> f)
                return One.fail(-1, "Failed to join team " + teamId + ": " + f.message());

        List.of(whiteId, blackId).forEach(id -> client.teams().requestAccept(teamId, id));


        Stream<String> gameStartEvents = white.board().connect().stream()
                .filter(e -> e instanceof Event.GameStartEvent)
                .map(e -> e.id());

        CompletableFuture<List<String>> gameIdFu = CompletableFuture.supplyAsync(() -> {
            List<String> ids = Collections.synchronizedList(new ArrayList<>());
            try { gameStartEvents.forEach(ids::add); } catch (Exception ex) {}
            return ids;
        });

        var swissRes = client.tournaments().createSwiss(teamId, params -> params
                        .clockRapid15m10s()
                        .addManualPairing(whiteId, blackId)
                        .manualRoundScheduling()
                        );

        if (! (swissRes instanceof Some(Swiss swiss))) {
            return swissRes instanceof Fail<Swiss> f ? f : One.fail(-1, "Couldn't create Swiss: " + swissRes);
        }

        for (var c : List.of(white, black))
            if (c.tournaments().joinSwiss(swiss.id()) instanceof Fail<?> f)
                return One.fail(-1, "Failed to join swiss " + swiss.id() + ": " + f.message());

        client.tournaments().scheduleNextRoundSwiss(swiss.id(), now -> now.plusSeconds(1));



        try { Thread.sleep(Duration.ofSeconds(2)); } catch (Exception e) {}
        gameStartEvents.close();

        List<String> gameIds = List.of();
        String gameId = "";
        try {
            gameIds = gameIdFu.get();
            gameId = gameIds.getLast();
        } catch(Exception e) {}

        if (gameId == null) {
            return One.fail(-1, "Failed to find ongoing game");
        }

        if (gameTestRunner instanceof Some(var runner)) {
            if (runner.play(gameId, white, black) instanceof Fail<?> f) {
                return One.fail(-1, "Failure from game test runner: " + f);
            }

            var swissUpdateRes = client.tournaments().updateSwiss(swiss.id(), params -> params
                    .clock(swiss.tourInfo().clock())
                    .addManualPairing(blackId, whiteId)
                    );

            if (! (swissUpdateRes instanceof Some(Swiss updatedSwiss))) {
                return One.fail(-1, "Failed to update swiss with next pairing: " + swissUpdateRes);
            }

            client.tournaments().scheduleNextRoundSwiss(swiss.id(), now -> now.plusSeconds(1));
            try { Thread.sleep(Duration.ofSeconds(2)); } catch (InterruptedException ie) {}

        }

        boolean debug = false;
        if (debug) client.logging(l -> l.request().all().response().all());
        var fetchRes = client.tournaments().swissById(swiss.id());
        if (debug) client.logging(l -> l.request().warning().response().warning());
        return fetchRes;
    }

    record GameTestRunner(PGN pgn) {

        public Ack play(String gameId, ClientAuth white, ClientAuth black) {
            String uci = Board.ofStandard().toUCI(pgn.moves());
            Board board = Board.ofStandard().play(pgn.moves());

            var moves = Arrays.stream(uci.split(" ")).toList();
            for (int i = 0; i < moves.size(); i++) {
                if (i % 2 == 0) {
                    if (white.board().move(gameId, moves.get(i)) instanceof Fail<?> f)
                        return new Fail<>(-1, "White failed move ply " + i + " " + moves.get(i) + ": " + f);
                } else {
                    if (black.board().move(gameId, moves.get(i)) instanceof Fail<?> f)
                        return new Fail<>(-1, "Black failed move ply " + i + " " + moves.get(i) + ": " + f);
                }
            }
            switch (pgn.tags().get("Result")) {
                case "1/2-1/2" -> {
                    white.board().handleDrawOffer(gameId, true);
                    black.board().handleDrawOffer(gameId, true);
                }
                case "1-0" -> {
                    if (! board.validMoves().isEmpty()) {
                        black.board().resign(gameId);
                    }
                }
                case "0-1" -> {
                    if (!board.validMoves().isEmpty()) {
                        white.board().resign(gameId);
                    }
                }
            };

            return Ack.ok();
        }
    }

    public static final String pgnDraw = """
        [Event "FIDE Women's Grand Prix 2024/25 - First Leg, Tbilisi"]
        [Site "Tbilisi, Georgia"]
        [Date "2024.08.16"]
        [Round "2.5"]
        [White "Kashlinskaya, Alina"]
        [Black "Muzychuk, Anna"]
        [Result "1/2-1/2"]
        [WhiteElo "2474"]
        [WhiteTitle "IM"]
        [WhiteFideId "4198026"]
        [BlackElo "2525"]
        [BlackTitle "GM"]
        [BlackFideId "14111330"]
        [TimeControl "90min/40moves+30min/end+30sec increment per move starting from move 1"]
        [Variant "Standard"]
        [ECO "D43"]
        [Opening "Semi-Slav Defense: Anti-Moscow Gambit"]
        [Annotator "https://lichess.org/broadcast/-/-/nkVvRgvF"]
        
        1. d4 d5 2. c4 c6 3. Nf3 Nf6 4. Nc3 e6 5. Bg5 h6 6. Bh4 Be7 7. e3 O-O 8. Qc2 Nbd7 9. a3 Ne8 10. Bg3 Nd6 11. cxd5 exd5 12. Bd3 Nf6 13. O-O Re8 14. Nd2 Be6 15. Rad1 Qc8 16. f3 c5 17. Rfe1 b6 18. Bf2 Rb8 19. e4 dxe4 20. Ndxe4 Ndxe4 21. Nxe4 cxd4 22. Bxd4 Nxe4 23. Bxe4 Qxc2 24. Bxc2 Rbc8 25. Be4 Red8 26. Be3 Rxd1 27. Rxd1 f5 28. Bd5 Kf7 29. Bxe6+ Kxe6 30. Re1 Kf7 31. Kf2 Bf6 32. Rc1 Rxc1 33. Bxc1 b5 34. b3 Ke6 35. Ke2 Kd5 36. Kd3 h5 37. h3 g6 38. Be3 a6 39. a4 Be5 40. axb5 axb5 41. Bd2 1/2-1/2""";

}
