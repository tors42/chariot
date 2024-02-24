package chariot.internal.modeladapter;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import chariot.model.Enums.*;

public interface EventAdapter {

    static Event nodeToEvent(YayNode node) {
        if (! (node instanceof YayObject yo)) return null;

        String eventType = yo.getString("type");
        Event event = switch(eventType) {

            case "gameStart",
                 "gameFinish" -> {

                Event.GameEvent gameEvent = null;

                if (! (yo.value().get("game") instanceof YayObject gameYo)) yield gameEvent;

                var gameInfo = GameInfoAdapter.nodeToInfo(gameYo);


                Event.Compat compat = null;
                if (gameYo.value().get("compat") instanceof YayObject compatYo) {
                    compat = new Event.Compat(compatYo.getBool("bot"), compatYo.getBool("board"));
                }

                gameEvent = switch(eventType) {
                    case "gameStart"  -> new Event.GameStartEvent(gameInfo, compat);
                    case "gameFinish" -> {
                        Enums.Outcome outcome = gameInfo.status().status() > 25
                            ? Enums.Outcome.draw
                            : Enums.Outcome.none;
                        String winner = gameYo.getString("winner");
                        if (winner != null) {
                            outcome = Color.valueOf(winner) == gameInfo.color()
                                ? Enums.Outcome.win
                                : Enums.Outcome.loss;
                        }

                        yield new Event.GameStopEvent(gameInfo, outcome, compat);
                    }
                    default -> null;
                };

                yield gameEvent;
            }

            case "challenge",
                 "challengeCanceled",
                 "challengeDeclined" -> {

                Event.ChallengeEvent challengeEvent = null;

                if (! (yo.value().get("challenge") instanceof YayObject challengeYo)) yield challengeEvent;

                ChallengeInfo challengeInfo = ChallengesAdapter.nodeToChallengeInfo(challengeYo);

                Event.Compat compat = null;

                if (yo.value().get("compat") instanceof YayObject compatYo) {
                    compat = new Event.Compat(compatYo.getBool("bot"), compatYo.getBool("board"));
                }

                challengeEvent = switch(eventType) {
                    case "challenge"         -> new Event.ChallengeCreatedEvent(challengeInfo,
                                                    Opt.of(challengeYo.getString("rematchOf")), compat);
                    case "challengeCanceled" -> new Event.ChallengeCanceledEvent(challengeInfo);
                    case "challengeDeclined" -> new Event.ChallengeDeclinedEvent(challengeInfo,
                                                    new Event.DeclineReason(
                                                        challengeYo.getString("declineReasonKey"),
                                                        challengeYo.getString("declineReason")));
                    default -> null;
                };

                yield challengeEvent;
            }

            default -> null;
        };

        return event;
    }
}
