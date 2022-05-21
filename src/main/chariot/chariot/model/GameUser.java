package chariot.model;

import java.util.Optional;

public sealed interface GameUser {

        default String name() {
            if (this instanceof Anonymous) return "Anonymous";
            if (this instanceof User u) return u.user().name();
            if (this instanceof Computer c) return "Stockfish level " + c.aiLevel();
            return "<unknown user>";

            // --enable-preview
            //return switch(this) {
            //    case Anonymous a -> "Anonymous";
            //    case User u -> u.user().name();
            //    case Computer c -> "Stockfish level " + c.aiLevel();
            //};
        }

        default Optional<Analysis> analysis() {
            if (this instanceof User u) return u.analysis();
            if (this instanceof Anonymous a) return a.analysis();
            return Optional.empty();
        }

        record Anonymous(Optional<Analysis> analysis) implements GameUser {}

        record Computer(Integer aiLevel) implements GameUser {}

        record User(
                LightUser user,
                Integer rating,
                Integer ratingDiff,
                boolean provisional,
                Optional<Boolean> berserk,
                Optional<Analysis> analysis) implements GameUser {
        }


        record Analysis(int inaccuracy, int mistake, int blunder, int acpl) {}
}
