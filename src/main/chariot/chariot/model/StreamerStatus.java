package chariot.model;

import java.util.Optional;

public record StreamerStatus(
        String id,
        String name,
        Optional<String> title,
        Optional<String> playingId,
        boolean online,
        boolean playing,
        boolean streaming,
        boolean patron,
        StreamInfo stream,
        StreamerInfo streamer)  {

    public record StreamInfo(String service, String status, String lang) {}
    public record StreamerInfo(String name, String headline, String description, Optional<String> twitch, Optional<String> youTube) {}
}
