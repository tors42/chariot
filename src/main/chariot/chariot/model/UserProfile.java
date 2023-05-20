package chariot.model;

import java.net.URI;

import chariot.model.UserData.Provided;

public record UserProfile(
        UserCommon common,
        Provided profile,
        UserStats stats,
        UserTimes times,
        UserFlags flags,
        URI url) implements User {
}
