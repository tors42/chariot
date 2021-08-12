package chariot.model;

public sealed interface StreamEvent extends Model {

    enum Type { gameStart, gameFinish, challenge, challengeCanceled, challengeDeclined }

    Type type();
    default String id() {

        // --enable-preview
        // return switch(this) {
        //     case GameEvent ge -> ge.game().id();
        //     case ChallengeEvent ce -> ce.challenge().id();
        //     case ChallengeAIEvent cae -> cae.challenge().id();
        // };

        if (this instanceof GameEvent ge) {
            return ge.game().id();
        } else if (this instanceof ChallengeEvent ce) {
            return ce.challenge().id();
        } else if (this instanceof ChallengeAIEvent cae) {
            return cae.challenge().id();
        } else {
            throw new RuntimeException("Unknown event: " + this);
        }
    }

    record GameEvent(Type type, Game game) implements StreamEvent {
        public record Game(String id) {}
    }

    // Todo, check what challenge structure is used in this StreamEvent - maybe something similar generic (as opposed to identical separate structurs)
    record ChallengeEvent(Type type, ChallengeResult.ChallengeInfo.Challenge challenge) implements StreamEvent {}
    record ChallengeAIEvent(Type type, ChallengeResult.ChallengeAI challenge) implements StreamEvent {}

}
