package chariot.model;

import chariot.model.Enums.Outcome;

public sealed interface Event {

    record GameStartEvent(GameInfo game, Compat compat)                                    implements GameEvent {}
    record GameStopEvent(GameInfo game, Outcome outcome, Compat compat)                    implements GameEvent {}

    record ChallengeCreatedEvent(ChallengeInfo challenge, Opt<String> rematchOf, Compat compat) implements ChallengeEvent {}
    record ChallengeCanceledEvent(ChallengeInfo challenge)                                 implements ChallengeEvent {}
    record ChallengeDeclinedEvent(ChallengeInfo challenge, DeclineReason reason)           implements ChallengeEvent {}

    enum Type {
        gameStart,
        gameFinish,
        challenge,
        challengeCanceled,
        challengeDeclined
    }

    record DeclineReason(String key, String text) {}
    record Compat(boolean bot, boolean board) {}

    default Type type() {
        return switch(this) {
            case GameStartEvent __         -> Type.gameStart;
            case GameStopEvent __          -> Type.gameFinish;
            case ChallengeCreatedEvent __  -> Type.challenge;
            case ChallengeCanceledEvent __ -> Type.challengeCanceled;
            case ChallengeDeclinedEvent __ -> Type.challengeDeclined;
        };
    }

    default String id() {
        return switch(this) {
            case GameEvent ge -> ge.gameId();
            case ChallengeEvent ce -> ce.id();
        };
    }

    sealed interface GameEvent      extends Event {
        GameInfo game();
        default String gameId() { return game().gameId(); }
    }

    sealed interface ChallengeEvent extends Event {
        ChallengeInfo challenge();
        default String id() { return challenge().id(); }
        default boolean isRematch() { return this instanceof ChallengeCreatedEvent c && c.rematchOf() instanceof Some; }
    }
}
