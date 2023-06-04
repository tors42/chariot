package chariot.model;

public record LightUser(String id, Opt<String> title, String name, boolean patron) implements UserCommon {}
