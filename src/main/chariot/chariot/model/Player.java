package chariot.model;

import java.util.Optional;

public sealed interface Player {

    default String name() {
        if (this instanceof Anonymous) return "Anonymous";
        if (this instanceof Account account) return account.user().name();
        if (this instanceof AI ai) return ai.name();
        return "<unknown user>";
    }

    record AI(int aiLevel, String name)                              implements Player {
        public AI(int level) { this(level, "Level " + level); }
    }
    record Anonymous()                                               implements Player {}
    record Account(UserCommon user, int rating, boolean provisional) implements Player {}
    record AccountArena(Player account,
            Optional<Boolean> berserk, Optional<String> team)        implements Player {}
    record AccountDiff(Player account, int ratingDiff)               implements Player {}
    record Analyzed(Player player, Analysis analysis)                implements Player {}

    sealed interface Analysis {
        private Base basic() { return this instanceof Accuracy a ? a.analysis() : (Base) this; }
        default int inaccuracy() { return basic().inaccuracy(); }
        default int mistake() { return basic().mistake(); }
        default int blunder() { return basic().blunder(); }
        default int acpl() { return basic().acpl(); }
        default Optional<Integer> accuracyOpt() { return this instanceof Accuracy a ? Optional.of(a.accuracy()) : Optional.empty(); }
    }

    record Base(int inaccuracy, int mistake, int blunder, int acpl) implements Analysis {}
    record Accuracy(Base analysis, int accuracy)                    implements Analysis {}
}
