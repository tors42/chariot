package chariot.model;

import java.util.Optional;

public sealed interface UserCommon permits User, UserStatus, LightUser, TitledUser, Disabled {

    default String id() { return lightUser().id(); }
    default String name() { return lightUser().name(); }
    default boolean patron() { return lightUser().patron(); }
    default Optional<String> titleOpt() {
        return userCommon() instanceof TitledUser tu
            ? Optional.of(tu.title())
            : Optional.empty();
    }

    private UserCommon userCommon() {
        if (this instanceof UserAuth auth) {
            return userCommon(auth);
        }
        if (this instanceof User user) {
            return userCommon(user);
        }
        if (this instanceof UserStatus status) {
            return userCommon(status);
        }
        return this;
    }

    private UserCommon userCommon(UserAuth user) {
        UserProfileAuth profileUserAuth = (UserProfileAuth) user;
        return userCommon(profileUserAuth.user());
    }

    private UserCommon userCommon(User user) {
        if (user instanceof UserProfile profileUser) return profileUser.common();
        if (user instanceof PlayingUser playingUser) return playingUser.user().common();
        return null;
    }

    private UserCommon userCommon(UserStatus userStatus) {
       if (userStatus instanceof UStatus ustatus) return ustatus.common();
       if (userStatus instanceof PlayingStatus playingStatus) return playingStatus.status().common();
       return null;
    }

    private LightUser lightUser() {
        var userCommon = userCommon();
        if (userCommon instanceof LightUser lightUser) return lightUser;
        if (userCommon instanceof TitledUser titledUser) return titledUser.user();
        if (userCommon instanceof Disabled disabled) return new LightUser(disabled.id(), disabled.name(), false);
        return null;
    }
}
