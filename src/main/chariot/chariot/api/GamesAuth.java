package chariot.api;

import chariot.model.NowPlaying;

import chariot.model.Result;
import chariot.model.GameImport;
import chariot.model.GameInfo;

public interface GamesAuth extends Games {

    /**
     * @deprecated See {@link #ongoing()}
     */
    @Deprecated
    Result<NowPlaying> ongoingGames();
    /**
     * @deprecated See {@link #ongoing(int)}
     */
    @Deprecated
    Result<NowPlaying> ongoingGames(int nb);

    Result<GameInfo> ongoing();
    Result<GameInfo> ongoing(int nb);

    // 200 per hour
    Result<GameImport> importGame(String pgn);

}
