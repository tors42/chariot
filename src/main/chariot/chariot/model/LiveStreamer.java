package chariot.model;

public record LiveStreamer(UserCommon user) {
    public boolean streaming() { return true; }
    public boolean online()    { return true; }
}
