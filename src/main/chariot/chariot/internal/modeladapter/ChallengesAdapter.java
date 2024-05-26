package chariot.internal.modeladapter;

import java.util.List;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import java.net.URI;

import chariot.model.Enums.*;

public interface ChallengesAdapter {

    /**
     * <pre>
     * </pre>
     *
     */
    static ChallengeOpenEnded nodeToChallengeOpenEnded(YayNode node) {
        return switch(node) {
            case YayObject yo when yo.value().get("challenge") instanceof YayObject challengeYo
                -> new ChallengeOpenEnded(
                        nodeToChallengeInfo(challengeYo),
                        yo.getString("urlWhite"),
                        yo.getString("urlBlack"),
                        challengeYo.value().get("open") instanceof YayObject openYo
                          && openYo.value().get("userIds") instanceof YayArray ya
                          && ya.filterCastMap(YayString::value, YayString.class) instanceof List<String> users
                          && users.size() == 2
                          ? new ChallengeOpenEnded.Reserved(users.get(0), users.get(1))
                          : new ChallengeOpenEnded.Any(),
                        challengeYo.value().get("rules") instanceof YayArray ya
                          ? ya.filterCastMap(YayString::value, YayString.class)
                          : List.of());
            case null, default -> null;
        };
    }


    /**
     * <pre>
     * </pre>
     *
     */
    static ChallengeInfo nodeToChallengeInfo(YayNode node) {
        return switch(node) {
            case YayObject challengeYo -> new ChallengeInfo(
                    challengeYo.getString("id"),
                    URI.create(challengeYo.getString("url")),
                    nodeToPlayers(challengeYo.value().get("challenger")),
                    GameTypeAdapter.nodeToGameType(challengeYo),
                    challengeYo.getString("finalColor") instanceof String colorOutcome
                        ? new ChallengeInfo.ColorOutcome(ColorPref.valueOf(challengeYo.getString("color")), Color.valueOf(colorOutcome))
                        : new ChallengeInfo.ColorRequest(ColorPref.valueOf(challengeYo.getString("color"))),
                    challengeYo.getArray("rules") instanceof List<YayNode> rulesNodes
                        ? Util.filterCast(rulesNodes, YayString.class).map(YayString::value).toList()
                        : List.of()
                    );
            case null, default -> null;
        };
    }

    private static ChallengeInfo.Player nodeToPlayer(YayNode node) {
        if (node instanceof YayObject yo
            && yo.getString("id") instanceof String id
            && yo.getString("name") instanceof String name
            && yo.value().get("rating") instanceof YayNumber(var rating)) {
            return new ChallengeInfo.Player(
                    UserInfo.of(id, name, yo.getString("title")),
                    rating.intValue(),
                    yo.getBool("provisional"),
                    yo.getBool("online"),
                    Opt.of(yo.getInteger("lag")));
        }
        return null;
    }

    private static ChallengeInfo.Players nodeToPlayers(YayNode node) {
        return switch(node) {
            case YayObject yo
                when nodeToPlayer(yo.value().get("challenger")) instanceof ChallengeInfo.Player challenger
                -> switch(nodeToPlayer(yo.value().get("destUser"))) {
                    case null                            -> new ChallengeInfo.From(challenger);
                    case ChallengeInfo.Player challenged -> new ChallengeInfo.FromTo(challenger, challenged);
                };
            default -> new ChallengeInfo.OpenEnded();
        };
    }

    static Challenge nodeToChallenge(YayNode node) {
        Challenge challenge = null;
        if (! (node instanceof YayObject yo)) return challenge;

        ///
        // keepAliveStream responds with challenge info json on first line,
        // and then waits for the opponent to accept or decline the challenge,
        // before writing a {"done":"accepted"} or {"done":"declined"} line.
        // In case the json here is the "done"-object,
        // it means the keepAliveStream has finished so we can return
        // a result to the user.
        // If the challenge wasn't "accepted", we generate
        // a DeclinedChallenge here, which will be assembled with a previous first line
        // json challenge info - and if it was "accepted", we send a null sentinel value
        // to indicate that the previous first line json challenge info can be used as
        // response to the user.
        String done = yo.getString("done");
        if (done != null) {
            return done.equals("accepted")
                ? null // sentinel for "accepted"
                : new Challenge.DeclinedChallenge("generic", done, null);
        }
        //
        ///

        // PendingChallenges for instance,
        // uses top level challenge,
        // i.e { "id":"...", "speed", ... },
        // as opposed to a wrapped challenge,
        // i.e "{ "challenge": { "id":"...", "speed", ...}, ... }"
        YayObject challengeYo = yo;

        if (yo.value().get("challenge") instanceof YayObject wrappedYo) {
            challengeYo = wrappedYo;
        }

        ChallengeInfo challengeInfo = nodeToChallengeInfo(challengeYo);
        challenge = challengeInfo;

        if (challengeYo.value().get("rules") instanceof YayArray rulesYarr) {
            challenge = new Challenge.ChallengeWithRules(rulesYarr.filterCastMap(YayString::value, YayString.class), challenge);
        }

        String rematchOf = challengeYo.getString("rematchOf");
        if (rematchOf != null) {
            challenge = new Challenge.RematchChallenge(rematchOf, challenge);
        }

        String declineKey = challengeYo.getString("declineReasonKey");
        String declineReason = challengeYo.getString("declineReason");
        if (declineKey != null) {
            challenge = new Challenge.DeclinedChallenge(declineKey, declineReason, challenge);
        }

        return challenge;
    }


}
