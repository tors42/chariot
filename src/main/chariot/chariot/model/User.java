package chariot.model;

import java.net.URI;
import java.time.*;
import java.util.*;

import chariot.model.UserData.Provided;

public sealed interface User extends UserCommon permits UserAuth, UserProfile, PlayingUser {

    private User user() {
        if (this instanceof PlayingUser playingUser) return playingUser.user();
        if (this instanceof UserProfileAuth userProfileAuth) return userProfileAuth.user();
        return this;
    }

    private UserProfile userProfile() {
        var user = user();
        if (user instanceof UserProfile userProfile) return userProfile;
        if (user instanceof PlayingUser playingUser) return playingUser.user();
        if (user instanceof UserProfileAuth userProfileAuth) return userProfileAuth.user().userProfile();
        return null;
    }

    default boolean           tosViolation()  { return userProfile().flags().tosViolation(); }
    default boolean           disabled()      { return userProfile().flags().disabled();     }
    default boolean           verified()      { return userProfile().flags().verified();     }
    default Map<StatsPerfType, StatsPerf> ratings() { return userProfile().stats().ratings();}
    default ZonedDateTime     createdAt()     { return userProfile().times().created();      }
    default ZonedDateTime     seenAt()        { return userProfile().times().seen();         }
    default Duration          playTimeTotal() { return userProfile().times().played();       }
    default Duration          playTimeTv()    { return userProfile().times().featured();     }
    default UserCount         accountStats()  { return userProfile().stats().counts();       }
    default Provided          profile()       { return userProfile().profile();              }
    default URI               url()           { return userProfile().url();                  }
    default Opt<List<Trophy>> trophies()      { return userProfile().trophies();             }

    default Optional<URI>     playingUri() {
        var user = user();
        if (user instanceof UserProfileAuth userProfileAuth) {
            user = userProfileAuth.user();
        }
        if (user instanceof PlayingUser playingUser) return Optional.of(playingUser.url());
        return Optional.empty();
    }
}
