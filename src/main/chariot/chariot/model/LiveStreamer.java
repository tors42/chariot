package chariot.model;

public record LiveStreamer(UserData _userData) implements UserCommon {
    public boolean streaming() { return true; }
    public boolean online()    { return true; }
}
