package chariot.model;

import java.util.Optional;

import chariot.model.Enums.*;

public sealed interface Event {

    enum Type { gameStart, gameFinish, challenge, challengeCanceled, challengeDeclined }

    Type type();
    default String id() {

        // --enable-preview
        // return switch(this) {
        //     case GameEvent ge -> ge.game().gameId();
        //     case ChallengeEvent ce -> ce.challenge().id();
        // };

        if (this instanceof GameEvent ge) {
            return ge.game().gameId();
        } else if (this instanceof ChallengeEvent ce) {
            return ce.challenge().id();
        } else {
            throw new RuntimeException("Unknown event: " + this);
        }
    }

    record GameEvent(Type type, GameInfo game) implements Event {
        public record GameInfo(
                String fullId,
                String gameId,
                String fen,
                Color color,
                String lastMove,
                Variant variant,
                Speed speed,
                PerfTypeWithFromPos perf,
                boolean rated,
                boolean hasMoved,
                Opponent opponent,
                boolean isMyTurn,
                int secondsLeft,
                Optional<String> tournamentId,
                Optional<String> swissId,
                String source,
                Compat compat
                ) {}
        public record Variant (GameVariant key, String name) {}
        public sealed interface Opponent permits Opponent.User, Opponent.AI {
            public record User (String id, String username, Integer rating) implements Opponent {}
            public record AI(String id, String username, Integer ai) implements Opponent {}
        }
    }

    // Todo, check what challenge structure is used in this StreamEvent - maybe something similar generic (as opposed to identical separate structurs)
    record ChallengeEvent(Type type, Challenge challenge, Compat compat) implements Event {}

    record Compat(boolean bot, boolean board) {}
}
