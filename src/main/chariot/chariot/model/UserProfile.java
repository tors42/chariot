package chariot.model;

import java.net.URI;
import java.util.List;

import chariot.model.UserData.Provided;

public record UserProfile(
        UserCommon common,
        Provided profile,
        UserStats stats,
        UserTimes times,
        UserFlags flags,
        Opt<List<Trophy>> trophies,
        URI url) implements User {
}
