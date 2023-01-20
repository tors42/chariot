package chariot.model;

import java.util.Optional;

public sealed interface Player {

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

        record Anonymous(Optional<Analysis> analysis) implements Player {}

        record Computer(Integer aiLevel) implements Player {}

        // Conveninece for accessing the LightUser-methods in User-record
        // if (player instanceof User u && u.title().equals("GM")) ...
        interface LUser {
            String id();
            String name();
            String title();
            boolean patron();
        }

        record User(
                LightUser user,
                Integer rating,
                Integer ratingDiff,
                boolean provisional,
                Optional<Boolean> berserk,
                Optional<Analysis> analysis) implements Player, LUser {

            @Override public String id() { return user.id(); }
            @Override public String name() { return user.name(); }
            @Override public String title() { return user.title(); }
            @Override public boolean patron() { return user.patron(); }
        }

        record Analysis(int inaccuracy, int mistake, int blunder, int acpl, int accuracy) {}
}
