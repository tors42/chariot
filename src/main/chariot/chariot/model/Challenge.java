package chariot.model;

import java.util.List;
import java.util.Optional;

import chariot.internal.Util;
import chariot.model.ChallengeResult.Perf;
import chariot.model.ChallengeResult.Player;
import chariot.model.ChallengeResult.TimeControl;
import chariot.model.Enums.*;

public record Challenge(
        String id,
        String speed,
        String url,
        String status,
        boolean rated,
        Direction direction,
        TimeControl timeControl,
        ColorPref color,
        Color finalColor,
        Variant variant,
        Player challenger,
        Player destUser,
        Perf perf,
        Optional<String> declineReason,
        Optional<String> rematchOf,
        List<String> rules) {

    public record Variant (GameVariant key, String name) {}

    public Challenge {
        status = Util.orEmpty(status);
    }
}

