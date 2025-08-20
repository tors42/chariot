package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface BroadcastsApi {

    /// Get official broadcasts  
    ///  
    /// First ongoing official broadcasts sorted by tier, then finished broadcasts sorted by most recent sync time.
    Many<Broadcast> official(Consumer<BroadcastParameters> params);

    /// Get official broadcasts  
    ///  
    /// First ongoing official broadcasts sorted by tier, then finished broadcasts sorted by most recent sync time.
    /// @param max Max number of broadcasts to fetch. `1..100` Default `20`
    default Many<Broadcast> official(int max) { return official(p -> p.nb(max)); }

    /// Get official broadcasts  
    /// First ongoing official broadcasts sorted by tier, then finished broadcasts sorted by most recent sync time.
    default Many<Broadcast> official() { return official(__ -> {}); }

    /// Get active top broadcasts
    Many<Broadcast.TourWithLastRound> topActive(Consumer<Params> params);

    /// Get upcoming top broadcasts
    Many<Broadcast.TourWithLastRound> topUpcoming(Consumer<Params> params);

    /// Get past top broadcasts
    Many<Broadcast.TourWithLastRound> topPast(Consumer<Params> params);

    /// Get active top broadcasts
    default Many<Broadcast.TourWithLastRound> topActive() { return topActive(__ -> {}); }

    /// Get upcoming top broadcasts
    default Many<Broadcast.TourWithLastRound> topUpcoming() { return topUpcoming(__ -> {}); }

    /// Get past top broadcasts
    default Many<Broadcast.TourWithLastRound> topPast() { return topPast(__ -> {}); }

    /// Stream an ongoing broadcast tournament as PGN  
    ///  
    /// This streaming endpoint first sends all games of a broadcast tournament in PGN format.  
    /// Then, it waits for new moves to be played. As soon as it happens,
    /// the entire PGN of the game is sent to the stream.  
    /// The stream will also send PGNs when games are added to the tournament.  
    /// This is the best way to get updates about an ongoing tournament.  
    /// Streaming means no polling, and no pollings means no latency, and minimum impact on the server.
    Many<PGN> streamBroadcast(String roundId);

    /// Export one round as PGN  
    ///  
    /// Download all games of a single round of a broadcast tournament in PGN format.  
    /// You _could_ poll this endpoint to get updates about a tournament, but it would be slow, and very inneficient.  
    /// Instead, consider streaming the tournament to get a new PGN every time a game is updated, in real-time. See {@link #streamBroadcast(String)}  
    /// @param roundId The round ID (8 characters).
    Many<PGN> exportOneRoundPgn(String roundId);

    /// Export all rounds as PGN  
    ///  
    /// Download all games of all rounds of a broadcast in PGN format.  
    /// You may want to download only the games of a single round instead. See {@link #exportOneRoundPgn(String)}  
    /// @param tourId The broadcast tournament ID (8 characters).
    Many<PGN> exportPgn(String tourId);

    /// Get information about a broadcast tournament
    /// @param tourId The broadcast tournament ID (8 characters).
    One<Broadcast> broadcastById(String tourId, Consumer<Params> params);

    /// Get information about broadcasts of a user
    /// @param userId The user to get broadcasts from
    Many<Broadcast.TourWithLastRound> byUserId(String userId, Consumer<Params> params);

    /// Search across recent official broadcasts
    Many<Broadcast.TourWithLastRound> search(String searchTerm);

    /// Get information about a broadcast tournament
    /// @param tourId The broadcast tournament ID (8 characters).
    default One<Broadcast> broadcastById(String tourId) { return broadcastById(tourId, __ -> {}); }

    /// Get information about broadcasts of a user
    /// @param userId The user to get broadcasts from
    default Many<Broadcast.TourWithLastRound> byUserId(String userId) { return byUserId(userId, __ -> {}); }

    /// Get a broadcast leaderboard, if available
    /// @param tourId The broadcast tournament ID (8 characters).
    Many<LeaderboardEntry> leaderboardById(String tourId);

    /// Get information about a broadcast round
    /// @param roundId The broadcast round id (8 characters).
    One<RoundInfo> roundById(String roundId);

    interface BroadcastParameters {
        /// @param nb Max number of broadcasts to fetch. `1..100` Default `20`
        BroadcastParameters nb(int nb);

        /// @param html Convert `description` field from Markdown to HTML
        BroadcastParameters html(boolean html);
        /// Convert `description` field from Markdown to HTML
        default BroadcastParameters html() { return html(true); }
    }

    interface Params {
        /// @param html Convert `description` field from Markdown to HTML
        Params html(boolean html);
        /// Convert `description` field from Markdown to HTML
        default Params html() { return html(true); }
    }
}
