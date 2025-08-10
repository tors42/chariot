package chariot.api;

import chariot.model.*;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Access Arena and Swiss tournaments played on Lichess.<br>
 * Official Arena tournaments are maintained by Lichess, but you can create your own Arena tournaments as well.
 */
public interface TournamentsApi {

    /**
     * Get recently finished, ongoing, and upcoming tournaments.
     * This API is used to display the Lichess tournament schedule.
     */
    One<TournamentStatus>  currentTournaments();

    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     * @param page Specify which page of player standings to view. [ 1 .. 200 ] Default: 1
     * @deprecated Use {@link #arenaById(String, Consumer)}
     */
    @Deprecated
    default One<Arena> arenaById(String arenaId, int page) { return arenaById(arenaId, params -> params.page(page)); }

    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     */
    default One<Arena> arenaById(String arenaId) { return arenaById(arenaId, __ -> {}); }

    /**
     * Get detailed info about recently finished, current, or upcoming tournament's duels, player standings, and other info.
     */
     One<Arena> arenaById(String arenaId, Consumer<StandingsParams> params);

     // todo? instead of StandingsParams.pageSampleAll(), which only returns answer after combining results.
     // Many<Arena.Standing> arenaStandingById(String arenaId)

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
     * @param params Optional filtering to only include tournaments of specified tournament status
     */
    Many<ArenaLight> arenasCreatedByUserId(String userId, Consumer<CreatedParams> params);
    default Many<ArenaLight> arenasCreatedByUserId(String userId) { return arenasCreatedByUserId(userId, __ -> {}); }

    @Deprecated
    /// @deprecated Use {@link #arenasCreatedByUserId(String, Consumer)}
    default Many<ArenaLight> arenasCreatedByUserId(String userId, Set<TourInfo.Status> status) {
        return arenasCreatedByUserId(userId, p -> p.status(status.stream().toArray(TourInfo.Status[]::new)));
    }
    @Deprecated
    /// @deprecated Use {@link #arenasCreatedByUserId(String, Consumer)}
    default Many<ArenaLight> arenasCreatedByUserId(String userId, TourInfo.Status... status) {
        return arenasCreatedByUserId(userId, p -> p.status(status));
    }

    ///
    /// Get tournaments played by a given user.
    /// @param params Optional filtering to only include tournaments of specified tournament status
    ///
    Many<ArenaPlayed> arenasPlayedByUserId(String userId, Consumer<PlayedParams> params);
    default Many<ArenaPlayed> arenasPlayedByUserId(String userId) { return arenasPlayedByUserId(userId, __ -> {}); }

    /**
     * Teams of a team battle tournament, with top players, sorted by rank (best first).
     */
    Many<Arena.TeamStanding> teamBattleResultsById(String tournamentId);

    /**
     * Download games of a arena tournament.<br>
     * Games are sorted by reverse chronological order (most recent first)
     */
    Many<Game> gamesByArenaId(String arenaId, Consumer<GamesApi.Filter> params);
    default Many<Game> gamesByArenaId(String arenaId) { return gamesByArenaId(arenaId, __ -> {}); }

    /** See {@link #gamesByArenaId(String, Consumer)} */
    Many<PGN> pgnGamesByArenaId(String arenaId, Consumer<GamesApi.Filter> params);
    default Many<PGN> pgnGamesByArenaId(String arenaId) { return pgnGamesByArenaId(arenaId, __ -> {}); }

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
    Many<Game> gamesBySwissId(String swissId, Consumer<GamesApi.Filter> params);
    default Many<Game> gamesBySwissId(String swissId) { return gamesBySwissId(swissId, __ -> {}); }

    /** See {@link #gamesBySwissId(String, Consumer)} */
    Many<PGN> pgnGamesBySwissId(String swissId, Consumer<GamesApi.Filter> params);
    default Many<PGN> pgnGamesBySwissId(String swissId) { return pgnGamesBySwissId(swissId, __ -> {}); }


    interface StandingsParams {
        /**
         * @param page Specify which page of player standings to view. [ 1 .. 200 ] Default: 1
         */
        StandingsParams page(int page);

        /**
         * Iterate through all pages, sampling the result of each page.
         *
         * Note, this is only stable for finished tournaments.
         *
         * If used for non-finished tournaments, the changing state of players during
         * sampling could lead to duplicate, missing and outdated entries.
         */
        StandingsParams pageSampleAll();
    }


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

    interface PlayedParams {
        /// Only include up to `max` tournaments
        PlayedParams max(int max);
        /// Include `performance` rating
        PlayedParams performance(boolean performance);
        /// Include `performance` rating
        default PlayedParams performance() { return performance(true); };
    }

    interface CreatedParams {
        /// Only include tournaments of specific status
        CreatedParams status(TourInfo.Status... specificStatus);
        /// Only include up to `max` tournaments
        CreatedParams max(int max);
    }


}
