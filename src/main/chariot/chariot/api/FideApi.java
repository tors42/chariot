package chariot.api;

import chariot.model.One;
import chariot.model.FidePlayer;

/// FIDE players and federations from [their public download](https://ratings.fide.com/download_lists.phtml)  
/// [https://lichess.org/fide](https://lichess.org/fide)
public interface FideApi {

    /// Get information about a FIDE player.
    One<FidePlayer> playerById(int fideId);
}

