package chariot.api;

import chariot.model.One;
import chariot.model.FidePlayer;
import chariot.model.Many;

/// FIDE players and federations from [their public download](https://ratings.fide.com/download_lists.phtml)  
/// [https://lichess.org/fide](https://lichess.org/fide)
public interface FideApi {

    /// Get information about a FIDE player.
    One<FidePlayer> byId(int fideId);

    /// Search information about FIDE players by name.
    Many<FidePlayer> searchByName(String name);
}

