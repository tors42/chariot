package chariot.model;

import java.util.List;

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
            Color finalColor,
            TimeControl timeControl,
            Variant variant,
            Player challenger,
            Player destUser,
            Perf perf,
            Open open,
            List<String> rules) {

        public Challenge {
            status = Util.orEmpty(status);
        }
    }

    public record Open(List<String> userIds) {}
}


