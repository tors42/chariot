package chariot.model;

public interface StreamerInfo {
    String name();
    String headline();
    String description();
    Opt<String> twitch();
    Opt<String> youtube();
    Opt<String> image();
}
