package chariot.model;

import java.net.URI;
import java.time.*;
import java.util.*;

public sealed interface User extends UserCommon permits UserAuth, UserProfileData {

    private UserProfileData userProfile() {
        if (this instanceof UserProfileData userProfile) return userProfile;
        return null;
    }

    default boolean           tosViolation()  { return userProfile().flags().tosViolation(); }
    default boolean           disabled()      { return userProfile().flags().disabled();     }
    default boolean           verified()      { return userProfile().flags().verified();     }
    default boolean           streaming()     { return userProfile().flags().streaming();    }
    default Map<StatsPerfType, StatsPerf> ratings() { return userProfile().stats().ratings();}
    default ZonedDateTime     createdAt()     { return userProfile().times().created();      }
    default ZonedDateTime     seenAt()        { return userProfile().times().seen();         }
    default Duration          playTimeTotal() { return userProfile().times().played();       }
    default Duration          playTimeTv()    { return userProfile().times().featured();     }
    default UserCount         accountStats()  { return userProfile().stats().counts();       }
    default ProvidedProfile   profile()       { return userProfile().profile();              }
    default URI               url()           { return userProfile().url();                  }
    default Opt<URI>          playingUrl()    { return userProfile().playingUrl();           }
    default Opt<List<Trophy>> trophies()      { return userProfile().trophies();             }
    default Opt<String>       twitchStream()  { return userProfile().twitchStream();         }
    default Opt<String>       youtubeStream() { return userProfile().youtubeStream();        }
}
