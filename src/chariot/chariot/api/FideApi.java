package chariot.api;

import module chariot;

/// FIDE players and federations from [their public download](https://ratings.fide.com/download_lists.phtml)  
/// [https://lichess.org/fide](https://lichess.org/fide)
public interface FideApi {

    /// Get information about a FIDE player.
    One<FidePlayer> byId(int fideId);

    /// Search information about FIDE players by name.
    Many<FidePlayer> searchByName(String name);

    /// Get ratings history of a FIDE player
    One<FideRatingHistory> ratingHistoryById(int fideId);
}
