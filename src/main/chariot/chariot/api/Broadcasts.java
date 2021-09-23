package chariot.api;

import chariot.model.Broadcast;
import chariot.model.Result;

public interface Broadcasts {

    Result<Broadcast> official(int nb);
    Result<Broadcast> official();

    /**
     * Stream an ongoing broadcast tournament as PGN
     *
     * <p>This streaming endpoint first sends all games of a broadcast tournament in PGN format.
     * <p>Then, it waits for new moves to be played. As soon as it happens, the entire PGN of the game is sent to the stream.
     * <p>The stream will also send PGNs when games are added to the tournament.
     * <p>This is the best way to get updates about an ongoing tournament. Streaming means no polling, and no pollings means no latency, and minimum impact on the server.
     */
    Result<String> streamBroadcast(String roundId);

}
