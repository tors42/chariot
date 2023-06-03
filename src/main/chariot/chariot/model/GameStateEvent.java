package chariot.model;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import chariot.model.Enums.Status;

public sealed interface GameStateEvent {

    enum Type { gameFull, gameState, chatLine, opponentGone }

    default Type type() {
        if (this instanceof Full) return Type.gameFull;
        if (this instanceof State) return Type.gameState;
        if (this instanceof Chat) return Type.chatLine;
        return Type.opponentGone;
    }

    record Full(
            String id,
            boolean rated,
            Variant variant,
            Clock clock,
            String speed,
            Perf perf,
            ZonedDateTime createdAt,
            UserCommon white,
            UserCommon black,
            String initialFen,
            State state
            ) implements GameStateEvent {


        public record Clock (Integer initial, Integer increment) {}
        public record Perf (String name) {}

    }

    record State(
            String moves,
            long wtime,
            long btime,
            long winc,
            long binc,
            boolean wdraw,
            boolean bdraw,
            boolean wtakeback,
            boolean btakeback,
            Status status,
            String winner) implements GameStateEvent {

        public List<String> moveList() {
            return Arrays.stream(moves.split(" ")).filter(s -> ! s.isEmpty()).toList();
        }
    }


    record Chat(
            String username,
            String text,
            String room) implements GameStateEvent {}

    record OpponentGone(
            boolean gone,
            Optional<Integer> claimWinInSeconds) implements GameStateEvent {}


}
