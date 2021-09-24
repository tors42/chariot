package chariot.api;

import chariot.model.Broadcast;
import chariot.model.Result;

public interface Broadcasts {

    /**
     * Get official broadcasts
     * <p>Get all incoming, ongoing, and finished official broadcasts. The broadcasts are sorted by start date, most recent first.
     * @param nb Max number of broadcasts to fetch. Default 20.
     */
    Result<Broadcast> official(int nb);

    /**
     * See {@link #official(int)}
     */
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

    /**
     * Export one round as PGN
     * <p>Download all games of a single round of a broadcast tournament in PGN format.
     * <p> You <i>could</i> poll this endpoint to get updates about a tournament, but it would be slow, and very inneficient.
     * <p> Instead, consider streaming the tournament to get a new PGN every time a game is updated, in real-time. See {@link #streamBroadcast(String)}
     * @param roundId The round ID (8 characters).
     */
    Result<String> exportOneRoundPgn(String roundId);

    /**
     * Export all rounds as PGN
     * <p>Download all games of all rounds of a broadcast in PGN format.
     * <p>You may want to download only the games of a single round instead. See {@link #exportOneRoundPgn(String)}
     * @param tourId The broadcast tournament ID (8 characters).
     */
    Result<String> exportPgn(String tourId);

}
