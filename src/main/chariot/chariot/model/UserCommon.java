package chariot.model;

public sealed interface UserCommon permits User, UserStatus, LightUser, Disabled {

    default String id() { return lightUser().id(); }
    default String name() { return lightUser().name(); }
    default boolean patron() { return lightUser().patron(); }
    default Opt<String> title() { return lightUser().title(); }

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
        UserProfileData profileData = (UserProfileData) user;
        return profileData.common();
    }

    private UserCommon userCommon(User user) {
        if (user instanceof UserProfileData profileUser) return profileUser.common();
        return null;
    }

    private UserCommon userCommon(UserStatus userStatus) {
        if (userStatus instanceof UserStatusData ustatus) return ustatus.common();
        return null;
    }

    private LightUser lightUser() {
        var userCommon = userCommon();
        if (userCommon instanceof LightUser lightUser) return lightUser;
        if (userCommon instanceof Disabled disabled) return new LightUser(disabled.id(), Opt.empty(), disabled.name(), false);
        return null;
    }
}
