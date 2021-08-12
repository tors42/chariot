package chariot.api;

import chariot.model.NowPlaying;
import chariot.model.Result;
import chariot.model.GameImport;

public interface GamesAuth extends Games {

    // /api/account/playing
    Result<NowPlaying> ongoingGames();
    Result<NowPlaying> ongoingGames(int nb);

    // 200 per hour
    Result<GameImport> importGame(String pgn);

}
