package chariot.model;

public record UserProfileAuth(User user, UserAuthFlags auth) implements UserAuth {}
