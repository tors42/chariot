package util;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.logging.Logger;

import chariot.ClientAuth;
import chariot.api.TournamentsApiAuth.JoinArenaParams;
import chariot.model.*;
import chariot.util.Board;

public record ArenaRunner(Arena arena, ClientAuth creator, List<Participant> participants, Opt<Duration> terminateAfter) implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("arenarunner");

    public record Participant(ClientAuth client, UserAuth account, Opt<Team> team) {}

    @Override
    public void run() {
        try (var arenaScope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var participant : participants()) {
                // Maybe this is good fit to try ScopedValues feature!? (arena, team, client)
                arenaScope.fork(() -> {
                    connectRandomMover(arena(), participant);

                    // Setup a "wait forever"
                    var semaphore = new Semaphore(0);
                    semaphore.acquire();
                    return null;
                    //
                });
            }

            Instant runUntil = Instant.now().plus(terminateAfter() instanceof Some(var override)
                    ? override : arena.duration());
            try {
                arenaScope.joinUntil(runUntil);
            } catch (TimeoutException to) {
                // End the tournament
                creator().tournaments().terminateArena(arena().id());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void connectRandomMover(Arena arena, Participant participant) {
        // join to get paired
        join(arena(), participant);

        participant.client().board().connect().stream().forEach(event -> {
            LOGGER.fine("Event for " + participant.account().id() + ": " + event);
            switch (event) {
                case Event.GameStartEvent(var game, _) -> {
                    handleGame(game, participant.client(), participant.account());
                    // re-join to get paired again
                    join(arena, participant);
                }
                default -> {}
            }});
    }

    void join(Arena arena, Participant participant) {
        Consumer<JoinArenaParams> params = switch (participant.team()) {
            case Some(Team team) -> p -> p.team(team.id());
            case Empty()         -> _ -> {};
        };
        params = params.andThen(p -> p.pairMeAsap());
        var joinResult = participant.client().tournaments().joinArena(arena.id(), params);

        if (joinResult instanceof Fail(int status, var err)) {
            LOGGER.warning("Failed to join %s to arena %s\n%d - %s"
                    .formatted(participant.account().id(), arena.id(),  status, err));
        }
    }

    private void handleGame(GameInfo game, ClientAuth client, UserAuth account) {
        String fenAtGameStart = game.fen();

        Function<Enums.Color, String> nameByColor = color ->
            color == game.color() ? account.name() : game.opponent().name();

        Consumer<String> processMoves = moves -> {
            Board board = moves.isBlank()
                ? Board.fromFEN(fenAtGameStart)
                : Board.fromFEN(fenAtGameStart).play(moves);

            if (game.color() == Enums.Color.white
                    ? board.blackToMove()
                    : board.whiteToMove()) return;

            List<Board.Move> validMoves = new ArrayList<>(board.validMoves());

            Collections.shuffle(validMoves, new Random());
            One<?> result = validMoves.stream()
                .map(Board.Move::uci)
                .findFirst()
                .map(uci -> {
                    return client.board().move(game.gameId(), uci);
                }).orElse(One.fail(-1, Err.from("no move")));

            if (result instanceof Fail<?> fail) {
                LOGGER.fine(() -> "Play failed: %s - resigning".formatted(fail));
                client.board().resign(game.gameId());
            }
        };

        LOGGER.fine(() -> "Connecting to game: %s".formatted(game));

        final AtomicInteger movesPlayedSinceStart = new AtomicInteger();
        try (var stream = client.board().connectToGame(game.gameId()).stream()) {
            stream.forEach(event -> { switch(event) {
                case GameStateEvent.Full full -> {
                    LOGGER.fine(() -> "FULL: %s".formatted(full));
                    movesPlayedSinceStart.set(full.state().moveList().size());
                    processMoves.accept("");
                }

                case GameStateEvent.State state -> {
                    List<String> moveList = state.moveList();
                    moveList = moveList.subList(movesPlayedSinceStart.get(), moveList.size());
                    int moves = moveList.size();
                    if (moves > 0) {
                        Board board = Board.fromFEN(fenAtGameStart);
                        if (moves > 1) board = board.play(String.join(" ", moveList.subList(0, moves-1)));
                        String lastMove = moveList.getLast();

                        String infoBeforeMove = "%s (%s) played (%s - %s)".formatted(
                                lastMove,
                                board.toSAN(lastMove),
                                nameByColor.apply(Enums.Color.white) + (board.whiteToMove() ? "*" : ""),
                                nameByColor.apply(Enums.Color.black) + (board.blackToMove() ? "*" : ""));

                        board = board.play(lastMove);

                        String infoAfterMove = "%s %s %s".formatted(
                                board.toFEN(),
                                board.gameState(),
                                state.status());

                        LOGGER.fine("%s\n%s".formatted(infoBeforeMove, infoAfterMove));
                    }

                    if (state.status().ordinal() > Enums.Status.started.ordinal()) {
                        client.board().chat(game.gameId(), "Thanks for the game!");
                        LOGGER.fine(() -> state.winner() instanceof Some(var winner)
                                ? "Winner: %s".formatted(nameByColor.apply(winner))
                                : "No winner: %s".formatted(state.status()));
                        break;
                    }

                    if (state.drawOffer() instanceof Some(var color)
                        && color != game.color()) {
                        client.board().handleDrawOffer(game.gameId(), true);
                        break;
                    }

                    processMoves.accept(String.join(" ", moveList));
                }

                case GameStateEvent.OpponentGone gone                  -> LOGGER.fine(() -> "Gone: %s".formatted(gone));
                case GameStateEvent.Chat(var name, var text, var room) -> LOGGER.fine(() -> "Chat: [%s][%s]: %s".formatted(name, room, text));
            }});
        }

        LOGGER.fine(() -> "GameEvent handler for %s finished".formatted(game.gameId()));
    }

}
