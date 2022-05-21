package chariot.model;

import java.util.Optional;
import java.util.function.Consumer;

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

        record Anonymous(Analysis analysis) implements GameUser {
            public Anonymous {
                analysis = analysis == null ? new Analysis.None() : analysis;
            }
        }

        record User(
                LightUser user,
                Integer rating,
                Integer ratingDiff,
                boolean provisional,
                Optional<Boolean> berserk,
                Analysis analysis) implements GameUser {
            public User {
                analysis = analysis == null ? Analysis.none : analysis;
            }
        }

        record Computer(Integer aiLevel) implements GameUser {}

        sealed interface Analysis permits Analysis.None, Analysis.Values {
            static None none = new None();
            default Optional<Values> maybe() {
                return this instanceof Values v ? Optional.of(v) : Optional.empty();
            }
            default void ifPresent(Consumer<Values> consumer) { if (this instanceof Values v) consumer.accept(v); }
            record None() implements Analysis {}
            record Values(Integer inaccuracy, Integer mistake, Integer blunder, Integer acpl) implements Analysis {}
        }
}
