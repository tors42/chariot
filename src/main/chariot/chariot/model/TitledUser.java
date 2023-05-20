package chariot.model;

public record TitledUser(LightUser user, String title) implements UserCommon {}
