package chariot.model;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import chariot.model.UserData.Count;
import chariot.model.UserData.PlayTime;
import chariot.model.UserData.Profile;

public interface User extends UserCommon {
    default boolean           tosViolation()   { return _userData()._tosViolation().orElse(false); }
    default boolean           closed()         { return _userData()._closed().orElse(false); }
    default boolean           disabled()       { return _userData()._disabled().orElse(false); }
    default boolean           verified()       { return _userData()._verified().orElse(false); }
    default Map<StatsPerfType, StatsPerf> ratings() { return _userData()._ratings().orElse(Map.of()); }
    default ZonedDateTime     createdAt()      { return _userData()._createdAt().orElse(null); }
    default ZonedDateTime     seenAt()         { return _userData()._seenAt().orElse(null); }
    default URI               url()            { return _userData()._url().orElse(null); }
    default Duration          playTimeTotal()  { return _userData()._playTime().map(PlayTime::total).orElse(null); }
    default Duration          playTimeTv()     { return _userData()._playTime().map(PlayTime::tv).orElse(null); }
    default Count             accountStats()   { return _userData()._count().orElse(null); }
    default Profile           profile()        { return _userData()._profile().orElse(Profile.emptyProfile); }
    default Optional<URI>     playingUri()     { return _userData()._playing(); }
    // only available if requested trophies,
    // so using Optional instead of empty list,
    // as an empty list could be interpreted as the user hasn't earned any trophies.
    default Optional<List<Trophy>> trophies() { return _userData()._trophies(); }
}
