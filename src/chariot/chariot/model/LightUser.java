package chariot.model;

public record LightUser(String id, Opt<String> title, String name, Opt<Integer> patronColor, Opt<String> flair) implements UserCommon {
    public boolean patron() { return patronColor.isPresent(); }
}
