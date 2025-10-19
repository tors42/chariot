package chariot.model;

public sealed interface UserCommon permits User, UserStatus, LightUser, Disabled {

    default String id() { return lightUser().id(); }
    default String name() { return lightUser().name(); }
    default boolean patron() { return lightUser().patron(); }
    default Opt<Integer> patronColor() { return lightUser().patronColor(); }
    default Opt<String> title() { return lightUser().title(); }
    default Opt<String> flair() { return lightUser().flair(); }

    private UserCommon userCommon() {
        return switch(this) {
            case UserAuth auth -> userCommon(auth);
            case User user -> userCommon(user);
            case UserStatus status -> userCommon(status);
            default -> this;
        };
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
        return switch(userCommon()) {
            case LightUser light -> light;
            case Disabled disabled -> new LightUser(disabled.id(), Opt.empty(), disabled.name(), Opt.empty(), Opt.empty());
            default -> null;
        };
    }
}
