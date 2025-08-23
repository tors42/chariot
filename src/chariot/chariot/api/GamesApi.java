package chariot.api;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.*;

import chariot.model.Enums.*;
import chariot.model.*;

/**
 * Access games played on Lichess.
 */
public interface GamesApi {

    /**
     * Download one game.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    One<Game> byGameId(String gameId, Consumer<GameParams> params);
    default One<Game> byGameId(String gameId) { return byGameId(gameId, _ -> {}); }

    /** See {@link #byGameId(String, Consumer)} */
    One<PGN> pgnByGameId(String gameId, Consumer<GameParams> params);
    default One<PGN> pgnByGameId(String gameId) { return pgnByGameId(gameId, _ -> {}); }


    /**
     * Download the ongoing game, or the last game played, of a user.<br>
     * If the game is ongoing, the 3 last moves are omitted.
     */
    One<Game> currentByUserId(String userId, Consumer<GameParams> params);
    default One<Game> currentByUserId(String userId) { return currentByUserId(userId, _ -> {}); }

    /** See {@link #currentByUserId(String, Consumer)} */
    One<PGN> pgnCurrentByUserId(String userId, Consumer<GameParams> params);
    default One<PGN> pgnCurrentByUserId(String userId) { return pgnCurrentByUserId(userId, _ -> {}); }

    /**
     * Download all games of any user<br>
     * Games are sorted by reverse chronological order (most recent first)<br>
     * The game stream is throttled, depending on who is making the request:
     * <ul>
     * <li> Anonymous request: 20 games per second
     * <li> Authenticated: 30 games per second.
     * <li> Authenticated, downloading your own games: 60 games per second.
     * </ul>
     * See {@link chariot.ClientAuth} for authenticated access.
     */
    Many<Game> byUserId(String userId, Consumer<SearchFilter> params);
    default Many<Game> byUserId(String userId) { return byUserId(userId, _ -> {}); }

    /** See {@link #byUserId(String, Consumer)} */
    Many<PGN> pgnByUserId(String userId, Consumer<SearchFilter> params);
    default Many<PGN> pgnByUserId(String userId) { return pgnByUserId(userId, _ -> {}); }

    /**
     * Download games by IDs.<br>
     * Games are sorted by reverse chronological order (most recent first)<br>
     * 300 IDs can be submitted.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    Many<Game> byGameIds(Set<String> gameIds, Consumer<GameParams> params);

    default Many<Game> byGameIds(Consumer<GameParams> params, String ... gameIds) { return byGameIds(Set.of(gameIds), params); }
    default Many<Game> byGameIds(String... gameIds) { return byGameIds(Set.of(gameIds)); }
    default Many<Game> byGameIds(Set<String> gameIds) { return byGameIds(gameIds, _ -> {}); }


    /** See {@link #byGameIds(Set, Consumer)} */
    Many<PGN> pgnByGameIds(Set<String> gameIds, Consumer<GameParams> params);

    default Many<PGN> pgnByGameIds(Consumer<GameParams> params, String ... gameIds) { return pgnByGameIds(Set.of(gameIds), params); }
    default Many<PGN> pgnByGameIds(String... gameIds) { return pgnByGameIds(Set.of(gameIds)); }
    default Many<PGN> pgnByGameIds(Set<String> gameIds) { return pgnByGameIds(gameIds, _ -> {}); }


    /**
     * Import a game from PGN.<br>
     * Rate limiting: 200 games per hour for OAuth requests, 100 games per hour for anonymous requests.<br>
     * See {@link GamesApiAuth#importGame} for authenticated access.<br>
     * To broadcast ongoing games, consider pushing to a broadcast instead. See {@link BroadcastsApiAuth#pushPgnByRoundId}
     */
    One<GameImport> importGame(String pgn);

   /**
     * Get a list of ongoing games for a given TV channel.
     */
    Many<Game> byChannel(Channel channel, Consumer<ChannelFilter> params);
    default Many<Game> byChannel(Channel channel) { return byChannel(channel, _ -> {}); }
    default Many<Game> byChannel(Function<Channel.Provider, Channel> channel) { return byChannel(channel.apply(Channel.provider())); }
    default Many<Game> byChannel(Function<Channel.Provider, Channel> channel, Consumer<ChannelFilter> params) { return byChannel(channel.apply(Channel.provider()), params); }


    /** See {@link #pgnByChannel(Channel, Consumer)} */
    Many<PGN> pgnByChannel(Channel channel, Consumer<ChannelFilter> params);
    default Many<PGN> pgnByChannel(Channel channel) { return pgnByChannel(channel, _ -> {}); }
    default Many<PGN> pgnByChannel(Function<Channel.Provider, Channel> channel) { return pgnByChannel(channel.apply(Channel.provider())); }
    default Many<PGN> pgnByChannel(Function<Channel.Provider, Channel> channel, Consumer<ChannelFilter> params) { return pgnByChannel(channel.apply(Channel.provider()), params); }


    /**
     * Get basic info about the best games being played for each speed and variant, but also computer games and bot games.
     */
    One<TVChannels> tvChannels();

    /**
     * Stream positions and moves of the current TV game.<br>
     * A summary of the game is sent as a first message, and when the featured game changes.
     */
    Many<TVFeedEvent> tvFeed();

    /**
     * Stream positions and moves of the current TV game on the specified channel.<br>
     * A summary of the game is sent as a first message, and when the featured game changes.
     */
    Many<TVFeedEvent> tvFeed(Channel channel);

    /**
     * Stream positions and moves of the current TV game on the specified channel.<br>
     * A summary of the game is sent as a first message, and when the featured game changes.
     */
    default Many<TVFeedEvent> tvFeed(Function<Channel.Provider, Channel> channel) { return tvFeed(channel.apply(Channel.provider())); }

    /**
     * Stream the games played between a list of users, in real time.<br>
     * Only games where both players are part of the list are included.<br>
     * Maximum number of users: 300.
     * @param userIds
     * @param params
     */
    Many<GameMeta> gameInfosByUserIds(Set<String> userIds, Consumer<GamesParameters> params);
    default Many<GameMeta> gameInfosByUserIds(Set<String> userIds) { return gameInfosByUserIds(userIds, _ -> {}); }
    default Many<GameMeta> gameInfosByUserIds(String... userIds) { return gameInfosByUserIds(Set.of(userIds)); }

    /**
     * Creates a stream of games from an arbitrary streamId, and a list of game IDs.<br>
     * The stream first outputs the games that already exists, then emits an event each time a game is started or finished.<br>
     * Maximum number of games: 500 for anonymous requests, or 1000 for OAuth2 authenticated requests.<br>
     * While the stream is open, it is possible to add new game IDs to watch.<br>
     * @param streamId Arbitrary stream ID that you can later use to add game IDs to the stream. Example: myAppName-someRandomId
     * @param gameIds The IDs of the games (more can be added at a later point with {@link #addGameIdsToStream(String, String...)}
     */
    Many<GameMeta> gameInfosByGameIds(String streamId, Set<String> gameIds);
    default Many<GameMeta> gameInfosByGameIds(String streamId, String... gameIds) { return gameInfosByGameIds(streamId, Set.of(gameIds)); }

    /**
     * Add new game IDs for an existing stream to watch.<br>
     * The stream will immediately outputs the games that already exists, then emit an event each time a game is started or finished.<br>
     */
    Ack addGameIdsToStream(String streamId, Set<String> gameIds);
    default Ack addGameIdsToStream(String streamId, String... gameIds) { return addGameIdsToStream(streamId, Set.of(gameIds)); }

    /**
     * Stream positions and moves of any ongoing game.<br>
     * A description of the game is sent as a first message. Then a message is sent
     * each time a move is played. Finally a description of the game is sent when
     * it finishes, and the stream is closed.<br>
     * Ongoing games are delayed by a few seconds ranging from 3 to 60 depending on the time control,<br>
     * as to prevent cheat bots from using this API.<br>
     * No more than 8 game streams can be opened at the same time from the same IP address.
     */
    Many<MoveInfo> moveInfosByGameId(String gameId);

    interface GamesParameters {
        /**
         * @param withCurrentGames whether to include ongoing games or not. Default: true
         */
        GamesParameters withCurrentGames(boolean withCurrentGames);
        default GamesParameters withCurrentGames() { return withCurrentGames(true); }
    }

    interface CommonGameParameters<T> {
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        T moves(boolean moves);
        default T moves() { return moves(true); }
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        T pgn(boolean pgn);
        default T pgn() { return pgn(true); }

        /**
         * @deprecated Use {@link #pgn(boolean)} instead
         */
        @Deprecated
        default T pgnInJson(boolean pgnInJson) { return pgn(pgnInJson); }

        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        T tags(boolean tags);
        default T tags() { return tags(true); }
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        T clocks(boolean clocks);
        default T clocks() { return clocks(true); }

        /**
         * Include plies which mark the beginning of the middlegame and endgame. Not available in {@code pgnBy...}-methods<br>
         * Default `false` (except in {@link GamesApi#byGameId(String)}, where it is default `true`)
         */
        T division(boolean division);
        default T division() { return division(true); }

        /**
         * Include the opening name.<br>
         * Default `true`
         */
        T opening(boolean opening);
        default T opening() { return opening(true); }

        /**
         * Include accuracy percent of each player, when available.<br>
         * Default `false`
         */
        T accuracy(boolean accuracy);
        default T accuracy() { return accuracy(true); }
    }

    interface Evals<T> {
         /// Include analysis evaluation comments in the PGN, when available.  
         /// Default `true`
        T evals(boolean evals);
        default T evals() { return evals(true); }
    }

    interface EvalsDefaultFalse<T> {
        /// Include analysis evaluation comments in the PGN, when available.  
        /// Default `false`
        T evals(boolean evals);
        default T evals() { return evals(true); }
    }

    interface Filter extends CommonGameParameters<Filter> {
        /**
         * Only games of a particular player. Leave empty to fetch games of all players.
         */
        Filter player(String userId);

        /**
         * Include analysis evaluation comments in the PGN, when available.
         */
        Filter evals(boolean evals);
        default Filter evals() { return evals(true); }
    }

    interface GameParams extends CommonGameParameters<GameParams>, Evals<GameParams> {
        /**
         * Insert textual annotations in the PGN about the opening, analysis variations, mistakes, and game termination.<br>
         * Default `false`
         */
        GameParams literate(boolean literate);
        default GameParams literate() { return literate(true); }

        /**
         * Include `bookmarked` (if authenticated)
         * Default `false`
         */
        GameParams withBookmarked(boolean withBookmarked);
        default GameParams withBookmarked() { return withBookmarked(true); }
    }

    interface CommonSearchFilterParams<T> {

        /**
         * Download games played since this timestamp.<br>
         * Default: Account creation date
         */
        T since(long since);
        /**
         * Download games played since this timestamp.<br>
         * Default: Account creation date
         */
        default T since(ZonedDateTime since) { return since(zdtToMillis(since)); }
        /**
         * Download games played since this timestamp, from a given {@code ZonedDateTime.now()} instance..<br>
         * Default: Account creation date
         */
        default T since(Function<ZonedDateTime, ZonedDateTime> now) { return since(now.apply(ZonedDateTime.now())); }
        /**
         * Download games played until this timestamp.<br>
         * Default: Now
         */
        T until(long until);
        /**
         * Download games played until this timestamp.<br>
         * Default: Now
         */
        default T until(ZonedDateTime until) { return until(zdtToMillis(until)); }
        /**
         * Download games played until this timestamp, from a given {@code ZonedDateTime.now()} instance..<br>
         * Default: Now
         */
        default T until(Function<ZonedDateTime, ZonedDateTime> now) { return until(now.apply(ZonedDateTime.now())); }

         /**
         * How many games to download.<br>
         * Leave empty to download all games.
         */
        T max(int max);

        /**
         * Include the FEN notation of the last position of the game.<br>
         * Default `false`
         */
        T lastFen(boolean lastFen);
        default T lastFen() { return lastFen(true); }

        /**
         * Insert textual annotations in the PGN about the opening, analysis variations, mistakes, and game termination.<br>
         * Default `false`
         */
        T literate(boolean literate);
        default T literate() { return literate(true); }


        /**
         * Sort order of the games, based on date.<br>
         * Default sort order is descending (i.e "false")
         */
        T sortAscending(boolean ascending);
        default T sortAscending() { return sortAscending(true); }


        private static long zdtToMillis(ZonedDateTime zdt) { return zdt.toInstant().getEpochSecond() * 1000; }
    }

    interface SearchFilter extends CommonGameParameters<SearchFilter>, Evals<SearchFilter>, CommonSearchFilterParams<SearchFilter> {
        /**
         * Only games played against this opponent
         */
        SearchFilter vs(String vs);
        /**
         *  Only rated (`true`) or casual (`false`) games
         */
        SearchFilter rated(boolean rated);
        default SearchFilter rated() { return rated(true); }
        /**
         * Only games played as this color.
         */
        SearchFilter color(Color color);
        /**
         * Only games played as this color.
         */
        default SearchFilter color(Function<Color.Provider, Color> color) { return color(color.apply(Color.provider())); }
        /**
         * Only games with or without a computer analysis available
         */
        SearchFilter analysed(boolean analysed);
        default SearchFilter analysed() { return analysed(true); }

        /**
         * Only games in these speeds or variants.<br>
         * Multiple perf types can be specified.
         */
        SearchFilter perfType(PerfType... perfTypes);
        /**
         * Only games in these speeds or variants.<br>
         * Multiple perf types can be specified.
         */
        default SearchFilter perfType(Set<Function<PerfType.Provider, PerfType>> perfTypes) {
            return perfType(perfTypes.stream().map(f -> f.apply(PerfType.provider())).toList().toArray(new PerfType[0]));
        }
        /**
         * Include ongoing games. The last 3 moves will be omitted.<br>
         * Default: false
         */
        SearchFilter ongoing(boolean ongoing);
        default SearchFilter ongoing() { return ongoing(true); }
        /**
         * Include finished games. Set to {@code false} to only get ongoing games<br>
         * Default: true
         */
        SearchFilter finished(boolean finished);
        default SearchFilter finished() { return finished(true); }

        /**
         * Include `bookmarked` (if authenticated)
         * Default `false`
         */
        SearchFilter withBookmarked(boolean withBookmarked);
        default SearchFilter withBookmarked() { return withBookmarked(true); }

    }

    interface ChannelFilter extends CommonGameParameters<ChannelFilter> {
        /**
         * Number of games to fetch.<br>
         * Default 10
         */
        ChannelFilter nb(int nb);
    }

}
