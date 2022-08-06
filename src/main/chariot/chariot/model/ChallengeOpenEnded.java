package chariot.model;

import chariot.internal.Util;
import chariot.model.Enums.*;

public record ChallengeOpenEnded(
            Challenge challenge,
            String urlWhite,
            String urlBlack,
            int socketVersion) implements ChallengeResult {

    public record Challenge(
            String id,
            String url,
            String speed,
            String status,
            boolean rated,
            ColorPref color,
            TimeControl timeControl,
            Variant variant,
            Player challenger,
            Player destUser,
            Perf perf) {

        public Challenge {
            status = Util.orEmpty(status);
        }
    }
}


