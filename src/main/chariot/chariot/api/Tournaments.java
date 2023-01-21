package chariot.api;

import chariot.model.*;
import chariot.model.Enums.TournamentState;

import java.util.Set;
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
    One<TournamentStatus>  currentTournaments();

    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     * @param page Specify which page of player standings to view. [ 1 .. 200 ] Default: 1
     */
    One<Arena> arenaById(String arenaId, int page);

    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     */
    One<Arena> arenaById(String arenaId);

    /**
     * Players of an Arena tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     *
     * {@snippet :
     * List<ArenaResult> top10 = client.tournaments().resultsByArenaId("QITRjufu", params -> params.max(10)).stream().toList();
     * }
     */
    Many<ArenaResult> resultsByArenaId(String arenaId, Consumer<ArenaResultParams> parameters);

    /** @see #resultsByArenaId(String, Consumer) */
    default Many<ArenaResult> resultsByArenaId(String arenaId) { return resultsByArenaId(arenaId, __ -> {}); }

    /**
     * Get tournaments created by a given user.
     * Tournaments are sorted by reverse chronological order of start date (last starting first).
     * @param specificStatus Optional filtering to only include tournaments of specified tournament status
     */
    Many<Tournament> arenasCreatedByUserId(String userId, Set<TournamentState> specificStatus);
    default Many<Tournament> arenasCreatedByUserId(String userId, TournamentState... specificStatus) { return arenasCreatedByUserId(userId, Set.of(specificStatus)); }
    default Many<Tournament> arenasCreatedByUserId(String userId) { return arenasCreatedByUserId(userId, Set.of()); }

    /**
     * Teams of a team battle tournament, with top players, sorted by rank (best first).
     */
    One<TeamBattleResults> teamBattleResultsById(String tournamentId);

    /**
     * Download games of a arena tournament.<br>
     * Games are sorted by reverse chronological order (most recent first)
     */
    Many<Game> gamesByArenaId(String arenaId, Consumer<Games.Filter> params);
    default Many<Game> gamesByArenaId(String arenaId) { return gamesByArenaId(arenaId, __ -> {}); }

    /**
     * Get info about a Swiss tournament.
     */
    One<Swiss> swissById(String swissId);


    /**
     * Players of a swiss tournament, with their score and performance, sorted by rank (best first).<br>
     * If called on an ongoing tournament, results can be inconsistent due to ranking changes while the players are being streamed.<br>
     * Use on finished tournaments for guaranteed consistency.
     * @param parameters
     *
     * {@snippet :
     * List<SwissResult> top10 = client.tournaments().resultsBySwissId("j8rtJ5GL", params -> params.max(10)).stream().toList();
     * }
     */
    Many<SwissResult> resultsBySwissId(String swissId, Consumer<SwissResultParams> parameters);

    /** @see #resultsBySwissId(String, Consumer) */
    default Many<SwissResult> resultsBySwissId(String swissId) { return resultsBySwissId(swissId, __ -> {}); }

    /**
     * Download a tournament in the Tournament Report File format, the FIDE standard.<br>
     * Documentation: <a href="https://www.fide.com/FIDE/handbook/C04Annex2_TRF16.pdf">PDF</a><br>
     * Example: <a href="https://lichess.org/swiss/j8rtJ5GL.trf">TRF</a>
     */
    Many<String> swissTRF(String swissId);

    /**
     * Download games of a swiss tournament.<br>
     * ames are sorted by reverse chronological order (last round first)
     */
    Many<Game> gamesBySwissId(String swissId, Consumer<Games.Filter> params);
    default Many<Game> gamesBySwissId(String swissId) { return gamesBySwissId(swissId, __ -> {}); }

    interface ArenaResultParams {
        /**
         * @param max Max number of players to fetch. >= 1
         */
        ArenaResultParams max(int max);

        /**
         * Provide values to the sheet field in the results ({@link ArenaResult#sheet()}). It's an expensive server computation that slows down the stream.
         */
        ArenaResultParams sheet(boolean sheet);
        default ArenaResultParams sheet() { return sheet(true); }
    }

    interface SwissResultParams {
        /**
         * @param max Max number of players to fetch. >= 1
         */
        SwissResultParams max(int max);
    }

}
