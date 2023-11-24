package chariot.model;

public record LightUser(String id, Opt<String> title, String name, boolean patron, Opt<String> flair) implements UserCommon {}
