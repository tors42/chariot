package chariot.model;

public record LiveStreamer(UserCommon user, StreamInfo stream, StreamerInfo streamer) {
    public boolean streaming() { return true; }
    public boolean online()    { return true; }
}
