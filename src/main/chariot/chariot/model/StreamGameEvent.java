package chariot.model;

import chariot.model.Enums.Room;
import chariot.internal.Util;

public sealed interface StreamGameEvent extends Model {

    enum Type { gameFull, gameState, chatLine }

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
            ) implements StreamGameEvent {

        public java.time.ZonedDateTime createdAt() {
            return Util.fromLong(createdTime());
        }

        public record Clock (Integer limit, Integer increment) {}
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
            String winner) implements StreamGameEvent {}


    record Chat(
            Type type,
            String username,
            String text,
            Room room) implements StreamGameEvent {}

}
