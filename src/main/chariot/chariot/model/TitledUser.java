package chariot.model;

public record TitledUser(String title, LightUser user) implements UserCommon {}
