package it.boardapi;

import java.time.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Enums.Color;
import chariot.model.Event.*;
import chariot.model.GameStateEvent.*;
import chariot.util.Board;
import util.*;
import static util.Assert.*;

public class Play {

    @IntegrationTest(expectedSeconds = 60)
    public void playRealTimeGame() {
        var white = IT.boris();
        var black = IT.yulia();

        if (! (black.board().connect() instanceof Entries(var blackEvents)
            && black.account().profile() instanceof Some(var blackUser))) {
            fail("Couldn't connect");
            return;
        }

        var gameIdRef = new AtomicReference<String>();
        Thread blackThread = Thread.ofPlatform().start(() -> blackEvents
                .takeWhile(_ -> gameIdRef.get() == null)
                .forEach(event -> {
                    switch(event) {
                        case ChallengeCreatedEvent created -> {
                            black.board().acceptChallenge(created.id());
                            gameIdRef.set(created.id());
                        }
                        default -> {}
                    }
                }));

        if (white.board().challengeKeepAlive(blackUser.id(), params ->
                    params.clock(3*60, 0).color(c -> c.white()))
                .stream().findFirst().isEmpty()) {
            fail("Failed to challenge");
            return;
        }
        assertTrue(() -> blackThread.join(Duration.ofMinutes(1).plusSeconds(10)));


        Deque<Board> positions = new ArrayDeque<>();
        positions.push(Board.fromStandardPosition());

        String gameId = gameIdRef.get();

        var debugEvents = new ArrayBlockingQueue<>(64);
        boolean systemout = true;
        Consumer<String> log = str -> {
            if (systemout) System.out.println(str);
            debugEvents.add(str);
        };

        connectAndPlaySomeMovesThenDisconnect(gameId, positions, 1, white, black, log, Duration.ZERO,         Duration.ZERO);

        log.accept("Reconnect to game\n");
        connectAndPlaySomeMovesThenDisconnect(gameId, positions, 4, white, black, log, Duration.ofSeconds(1), Duration.ofSeconds(1));

        log.accept("Reconnect to game\n");
        connectAndPlaySomeMovesThenDisconnect(gameId, positions, 4, white, black, log, Duration.ofSeconds(1), Duration.ofSeconds(1));

        log.accept("\nWhite resigns");
        white.board().resign(gameId);

        /*
          //unboxEquals(white.games().byGameId(gameId), Color.black, Game::winner);

          SEVERE: http://lila:9663/game/export/HIEMm75t GET
          java.net.ConnectException

          Because:
           - Game DB not available?
           - /game/export/<gameId> not within /api/... ?
        */

        // workaround, use http://lila:9663/api/games/user/yulia GET
        var game = white.games().byUserId(blackUser.id()).stream()
            .findFirst()
            .map(One::entry)
            .orElse(One.fail(404, "not found (generated)"));

        unboxEquals(game, gameId, Game::id);
        unboxEquals(game, Opt.of(Color.black), Game::winner);

    }

    private void connectAndPlaySomeMovesThenDisconnect(
            String gameId,
            Deque<Board> positions,
            int moves,
            ClientAuth white,
            ClientAuth black,
            Consumer<String> log,
            Duration beforeWhiteMove,
            Duration beforeBlackMove) {

        var whiteConnect = white.board().connectToGame(gameId);
        var blackConnect = black.board().connectToGame(gameId);

        if (! (whiteConnect instanceof Entries(var whiteStream)
            && blackConnect instanceof Entries(var blackStream))) {
           fail(String.join("\n",
                        "Couldn't connect to " + gameId,
                        "white " + whiteConnect,
                        "black " + blackConnect));
            return;
        }

        Thread.ofPlatform().start(() -> { try { whiteStream.forEach(event -> {
            switch(event) {
                case Full         full  -> log.accept("Initial event: " + String.join("\n",
                                                      "Clock: " + clock(full.state()),
                                                      full.state().moves()));
                case State        state -> log.accept("event: " + String.join("\n",
                                                      "Clock: " + clock(state),
                                                      state.moves()));
                case Chat         chat  -> {}
                case OpponentGone gone  -> {}
            }});
        } catch (Exception e) {} });

        for (int move = 0; move < moves; move++) {
            sleep(beforeWhiteMove);

            var validMoves = positions.peek().validMoves().stream().toList();
            var whiteMove = validMoves.get(new Random().nextInt(validMoves.size()));
            white.board().move(gameId, whiteMove.uci());
            positions.push(positions.peek().play(whiteMove));

            sleep(beforeBlackMove);

            validMoves = positions.peek().validMoves().stream().toList();
            var blackMove = validMoves.get(new Random().nextInt(validMoves.size()));
            black.board().move(gameId, blackMove.uci());
            positions.push(positions.peek().play(blackMove));
        }

        try { whiteStream.close(); } catch(Exception e) { }
        try { blackStream.close(); } catch(Exception e) { }

    }

    private String clock(State state) { return "%3d - %3d".formatted(state.wtime().toSeconds(), state.btime().toSeconds()); }
    private void sleep(Duration duration) { try { Thread.sleep(duration); } catch (Exception e) {} }
}
