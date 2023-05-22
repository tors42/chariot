package chariot.model;

import chariot.model.Enums.Outcome;

public sealed interface Event {

    record GameStartEvent(GameInfo game, Compat compat)                                     implements GameEvent {}
    record GameStopEvent(GameInfo game, String lastMove, Result result, Compat compat)      implements GameEvent {}

    record ChallengeCreatedEvent(ChallengeInfo challenge, Compat compat)                    implements ChallengeEvent {}
    record ChallengeRematchEvent(ChallengeInfo challenge, String rematchOf, Compat compat)  implements ChallengeEvent {}
    record ChallengeCanceledEvent(ChallengeInfo challenge)                                  implements ChallengeEvent {}
    record ChallengeDeclinedEvent(ChallengeInfo challenge, DeclineReason reason)            implements ChallengeEvent {}

    enum Type {
        gameStart,
        gameFinish,
        challenge,
        challengeCanceled,
        challengeDeclined
    }

    sealed interface Result {}
    record Casual(Outcome outcome) implements Result {}
    record Rated(Outcome outcome, int ratingDiff) implements Result {}

    record DeclineReason(String key, String text) {}
    record Compat(boolean bot, boolean board) {}

    default Type type() {
        if (this instanceof GameStartEvent) return Type.gameStart;
        if (this instanceof GameStopEvent) return Type.gameFinish;
        if (this instanceof ChallengeCreatedEvent) return Type.challenge;
        if (this instanceof ChallengeRematchEvent) return Type.challenge;
        if (this instanceof ChallengeCanceledEvent) return Type.challengeCanceled;
        if (this instanceof ChallengeDeclinedEvent) return Type.challengeDeclined;
        throw new RuntimeException("Unknown event: " + this);
    }

    default String id() {
        if (this instanceof GameEvent ge) return ge.gameId();
        if (this instanceof ChallengeEvent ce) return ce.id();
        throw new RuntimeException("Unknown event: " + this);
    }

    sealed interface GameEvent      extends Event {
        GameInfo game();
        default String gameId() { return game().gameId(); }
    }

    sealed interface ChallengeEvent extends Event {
        ChallengeInfo challenge();
        default String id() { return challenge().id(); }
    }
}
