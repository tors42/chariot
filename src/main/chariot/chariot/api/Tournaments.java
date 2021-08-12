package chariot.api;

import chariot.model.ArenaResult;
import chariot.model.Arena;
import chariot.model.Game;
import chariot.model.Result;
import chariot.model.SwissResult;
import chariot.model.TeamBattleResults;
import chariot.model.Tournament;
import chariot.model.TournamentStatus;

import java.util.function.Consumer;

/**
 * Access Arena and Swiss tournaments played on Lichess.<br>
 * Official Arena tournaments are maintained by Lichess, but you can create your own Arena tournaments as well.
 */
public interface Tournaments {

    /**
     * Get recently finished, ongoing, and upcoming tournaments.
     * This API is used to display the Lichess tournament schedule.
     */
    Result<TournamentStatus>  currentTournaments();
    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     * @param page Specify which page of player standings to view. [ 1 .. 200 ]<br>
     *             Default: 1
     */
    Result<Arena> arenaById(String arenaId, int page);
    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     */
    Result<Arena> arenaById(String arenaId);
    /**
     * Get all tournaments created by a given user.<br>
     * Tournaments are sorted by reverse chronological order of start date (last starting first).
     */
    Result<Tournament> arenasCreatedByUserId(String userId);

    /**
     * Download games of a arena tournament.<br>
     * Games are sorted by reverse chronological order (most recent first)
     */
    Result<Game> gamesByArenaId(String arenaId, Consumer<Games.Filter> params);
    /**
     * Download games of a arena tournament.<br>
     * Games are sorted by reverse chronological order (most recent first)
     */
    Result<Game> gamesByArenaId(String arenaId);
    /**
     * Download games of a swiss tournament.<br>
     * ames are sorted by reverse chronological order (last round first)
     */
    Result<Game> gamesBySwissId(String swissId, Consumer<Games.Filter> params);
    /**
     * Download games of a swiss tournament.<br>
     * ames are sorted by reverse chronological order (last round first)
     */
    Result<Game> gamesBySwissId(String swissId);
    /**
     * Players of an Arena tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     * @param nb Max number of players to fetch
     */
    Result<ArenaResult>       resultsByArenaId(String arenaId, int nb);
    /**
     * Players of an Arena tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     */
    Result<ArenaResult>       resultsByArenaId(String arenaId);

    /**
     * Players of a swiss tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     * @param nb Max number of players to fetch
     */
    Result<SwissResult>       resultsBySwissId(String swissId, int nb);
    /**
     * Players of a swiss tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     */
    Result<SwissResult>       resultsBySwissId(String swissId);


    /**
     * Download a tournament in the Tournament Report File format, the FIDE standard.<br>
     * Documentation: <a href="https://www.fide.com/FIDE/handbook/C04Annex2_TRF16.pdf">PDF</a><br>
     * Example: <a href="https://lichess.org/swiss/j8rtJ5GL.trf">TRF</a>
     */
    Result<String>            swissTRF(String swissId);

    /**
     * Teams of a team battle tournament, with top players, sorted by rank (best first).
     */
    Result<TeamBattleResults> teamBattleResultsById(String tournamentId);

}
