package chariot.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import chariot.internal.Util;

public sealed interface GameEvent {

    enum Type { gameFull, gameState, chatLine, opponentGone }

    Type type();

    record Full(
            Type type,
            String id,
            boolean rated,
            Variant variant,
            Clock clock,
            String speed,
            Perf perf,
            Long createdTime,
            LightUser white,
            LightUser black,
            String initialFen,
            State state
            ) implements GameEvent {

        public java.time.ZonedDateTime createdAt() {
            return Util.fromLong(createdTime());
        }

        public record Clock (Integer initial, Integer increment) {}
        public record Perf (String name) {}

    }

    record State(
            Type type,
            String moves,
            long wtime,
            long btime,
            long winc,
            long binc,
            boolean wdraw,
            boolean bdraw,
            boolean wtakeback,
            boolean btakeback,
            Game.Status status,
            String winner) implements GameEvent {

        public List<String> moveList() {
            return Arrays.stream(moves.split(" ")).filter(s -> ! s.isEmpty()).toList();
        }
    }


    record Chat(
            Type type,
            String username,
            String text,
            String room) implements GameEvent {}

    record OpponentGone(
            Type type,
            boolean gone,
            Optional<Integer> claimWinInSeconds) implements GameEvent {}


}
