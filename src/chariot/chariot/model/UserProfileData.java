package chariot.model;

import java.net.URI;
import java.util.List;

public record UserProfileData(
        UserCommon common,
        ProvidedProfile profile,
        UserStats stats,
        UserTimes times,
        UserFlags flags,
        Opt<URI> playingUrl,
        Opt<UserAuthFlags> authFlags,
        Opt<List<Trophy>> trophies,
        Opt<Boolean> canChallenge,
        Opt<String> twitchStream,
        Opt<String> youtubeStream,
        URI url) implements User, UserAuth {
}
