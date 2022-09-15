package chariot.api;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.*;
import java.util.stream.*;

import chariot.model.Enums.*;
import chariot.model.*;

/**
 * Access games played on Lichess.
 */
public interface Games {

    /**
     * Download one game.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    One<Game> byGameId(String gameId, Consumer<GameParams> params);
    default One<Game> byGameId(String gameId) { return byGameId(gameId, __ -> {}); }

    /**
     * Download the ongoing game, or the last game played, of a user.<br>
     * If the game is ongoing, the 3 last moves are omitted.
     */
    One<Game> currentByUserId(String userId, Consumer<GameParams> params);
    default One<Game> currentByUserId(String userId) { return currentByUserId(userId, __ -> {}); }


    /**
     * Download all games of any user<br>
     * Games are sorted by reverse chronological order (most recent first)<br>
     * The game stream is throttled, depending on who is making the request:
     * <ul>
     * <li> Anonymous request: 20 games per second
     * <li> Authenticated: 30 games per second.
     * <li> Authenticated, downloading your own games: 60 games per second.
     * </ul>
     * See {@link GamesAuth#currentByUserId(String, Consumer)} for authenticated access.
     */
    Many<Game> byUserId(String userId, Consumer<SearchFilter> params);
    default Many<Game> byUserId(String userId) { return byUserId(userId, __ -> {}); }


    /**
     * Download games by IDs.<br>
     * Games are sorted by reverse chronological order (most recent first)<br>
     * 300 IDs can be submitted.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    Many<Game> byGameIds(Set<String> gameIds, Consumer<GameParams> params);

    default Many<Game> byGameIds(Consumer<GameParams> params, String ... gameIds) { return byGameIds(Set.of(gameIds), params); }
    default Many<Game> byGameIds(String... gameIds) { return byGameIds(Set.of(gameIds)); }
    default Many<Game> byGameIds(Set<String> gameIds) { return byGameIds(gameIds, __ -> {}); }

    /**
     * Import a game from PGN.<br>
     * Rate limiting: 200 games per hour for OAuth requests, 100 games per hour for anonymous requests.<br>
     * See {@link GamesAuth#importGame} for authenticated access.<br>
     * To broadcast ongoing games, consider pushing to a broadcast instead. See {@link BroadcastsAuth#pushPgnByRoundId}
     */
    One<GameImport> importGame(String pgn);

    /**
     * Fetches the PGN of specified game from Masters OTB database
     * @param gameId Example: "aAbqI4ey"
     */
    One<Pgn> openingExplorerMastersOTB(String gameId);

    /**
     * Find Masters games from Opening Explorer
     */
    One<ExploreResult.OpeningDB> openingExplorerMasters(Consumer<MastersBuilder> params);
    default One<ExploreResult.OpeningDB> openingExplorerMasters() { return openingExplorerMasters(__ -> {}); }

    /**
     * Find Lichess games from Opening Explorer
     */
    One<ExploreResult.OpeningDB> openingExplorerLichess(Consumer<LichessBuilder> params);
    default One<ExploreResult.OpeningDB> openingExplorerLichess() { return openingExplorerLichess(__ -> {}); }

    /**
     * Find Player games from Opening Explorer<br>
     */
    One<ExploreResult.OpeningPlayer> openingExplorerPlayer(String userId, Consumer<PlayerBuilder> params);
    default One<ExploreResult.OpeningPlayer> openingExplorerPlayer(String userId) { return openingExplorerPlayer(userId, __ -> {}); }

    /**
     * Lookup positions from the Lichess tablebase server.
     */
    One<TablebaseResult> lookupTablebase(String fen);
    /**
     * Lookup Atomic positions from the Lichess tablebase server.
     */
    One<TablebaseResult> lookupTablebaseAtomic(String fen);
    /**
     * Lookup Antichess positions from the Lichess tablebase server.
     */
    One<TablebaseResult> lookupTablebaseAntichess(String fen);

    /**
     * Get a list of ongoing games for a given TV channel.
     */
    Many<Game> byChannel(Channel channel, Consumer<ChannelFilter> params);
    default Many<Game> byChannel(Channel channel) { return byChannel(channel, __ -> {}); }
    default Many<Game> byChannel(Function<Channel.Provider, Channel> channel) { return byChannel(channel.apply(Channel.provider())); }
    default Many<Game> byChannel(Function<Channel.Provider, Channel> channel, Consumer<ChannelFilter> params) { return byChannel(channel.apply(Channel.provider()), params); }

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
     * Stream the games played between a list of users, in real time.<br>
     * Only games where both players are part of the list are included.<br>
     * Maximum number of users: 300.
     * @param withCurrentGames whether to include ongoing games or not. Default: true
     * @param userIds
     */
    Many<StreamGame> streamGamesByUserIds(boolean withCurrentGames, Set<String> userIds);
    default Many<StreamGame> streamGamesByUserIds(String... userIds) { return streamGamesByUserIds(Set.of(userIds)); }
    default Many<StreamGame> streamGamesByUserIds(Set<String> userIds) { return streamGamesByUserIds(true, userIds); }
    default Many<StreamGame> streamGamesByUserIds(boolean withCurrentGames, String... userIds) { return streamGamesByUserIds(withCurrentGames, Set.of(userIds)); }


    /**
     * Creates a stream of games from an arbitrary streamId, and a list of game IDs.<br>
     * The stream first outputs the games that already exists, then emits an event each time a game is started or finished.<br>
     * Maximum number of games: 500 for anonymous requests, or 1000 for OAuth2 authenticated requests.<br>
     * While the stream is open, it is possible to add new game IDs to watch.<br>
     * @param streamId Arbitrary stream ID that you can later use to add game IDs to the stream. Example: myAppName-someRandomId
     */
    Many<StreamGame> streamGamesByGameIds(String streamId, Set<String> gameIds);
    default Many<StreamGame> streamGamesByGameIds(String streamId, String... gameIds) { return streamGamesByGameIds(streamId, Set.of(gameIds)); }

    /**
     * Add new game IDs for an existing stream to watch.<br>
     * The stream will immediately outputs the games that already exists, then emit an event each time a game is started or finished.<br>
     */
    One<Ack> addGameIdsToStream(String streamId, Set<String> gameIds);
    default One<Ack> addGameIdsToStream(String streamId, String... gameIds) { return addGameIdsToStream(streamId, Set.of(gameIds)); }

    /**
     * Stream positions and moves of any ongoing game.<br>
     * A description of the game is sent as a first message. Then a message is sent
     * each time a move is played. Finally a description of the game is sent when
     * it finishes, and the stream is closed.<br>
     * After move 5, the stream intentionally remains 3 moves behind the game status,
     * as to prevent cheat bots from using this API.<br>
     * No more than 8 game streams can be opened at the same time from the same IP address.
     */
    Many<StreamMove> streamMovesByGameId(String gameId);

    interface CommonGameParameters<T> {
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        T moves(boolean moves);
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        T pgnInJson(boolean pgnInJson);
        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        T tags(boolean tags);
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        T clocks(boolean clocks);
        /**
         * Include the opening name.<br>
         * Default `true`
         */
        T opening(boolean opening);
    }

    interface EvalsAndPlayers<T> {
        /**
         * Include analysis evaluation comments in the PGN, when available.<br>
         * Default `true`
         */
        T evals(boolean evals);
        /**
         * URL of a text file containing real names and ratings, to replace Lichess usernames and ratings in the PGN.<br>
         * Example: https://gist.githubusercontent.com/ornicar/6bfa91eb61a2dcae7bcd14cce1b2a4eb/raw/768b9f6cc8a8471d2555e47ba40fb0095e5fba37/gistfile1.txt
         */
        T players(URL urlToTextFile);
    }

    interface Filter extends CommonGameParameters<Filter> {
        /**
         * Only games of a particular player. Leave empty to fetch games of all players.
         */
        Filter player(String userId);
    }

    interface GameParams extends CommonGameParameters<GameParams>, EvalsAndPlayers<GameParams> {
        /**
         * Insert textual annotations in the PGN about the opening, analysis variations, mistakes, and game termination.<br>
         * Default `false`
         */
        GameParams literate(boolean literate);
    }

    interface SearchFilter extends CommonGameParameters<SearchFilter>, EvalsAndPlayers<SearchFilter> {
        /**
         * Download games played since this timestamp.<br>
         * Default: Account creation date
         */
        SearchFilter since(long since);
        /**
         * Download games played since this timestamp.<br>
         * Default: Account creation date
         */
        default SearchFilter since(ZonedDateTime since) { return since(zdtToMillis(since)); }
        /**
         * Download games played until this timestamp.<br>
         * Default: Now
         */
        SearchFilter until(long until);
        /**
         * Download games played until this timestamp.<br>
         * Default: Now
         */
        default SearchFilter until(ZonedDateTime until) { return until(zdtToMillis(until)); }
        /**
         * How many games to download.<br>
         * Leave empty to download all games.
         */
        SearchFilter max(int max);
        /**
         * Only games played against this opponent
         */
        SearchFilter vs(String vs);
        /**
         *  Only rated (`true`) or casual (`false`) games
         */
        SearchFilter rated(boolean rated);
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
        SearchFilter analyzed(boolean analyzed);
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
        /**
         * Include finished games. Set to {@code false} to only get ongoing games<br>
         * Default: true
         */
        SearchFilter finished(boolean finished);
        /**
         * Sort order of the games, based on date.<br>
         * Default sort order is descending (i.e "false")
         */
        SearchFilter sortAscending(boolean ascending);

        /**
         * Insert textual annotations in the PGN about the opening, analysis variations, mistakes, and game termination.<br>
         * Default `false`
         */
        SearchFilter literate(boolean literate);

        private static long zdtToMillis(ZonedDateTime zdt) { return zdt.toInstant().getEpochSecond() * 1000; }
    }


    interface ChannelFilter extends CommonGameParameters<ChannelFilter> {
        /**
         * Number of games to fetch.<br>
         * Default 10
         */
        ChannelFilter nb(int nb);
    }


    interface CommonOpeningExplorer<T> {
        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         */
        T fen(String fen);
        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional moves starting from {@code fen}.<br>
         * Required to find an opening name, if  {@code fen} is not an exact match for a named position.<br>
         * Example: "e2e4,e7e5,c2c4,c7c6,c4e5"
         */
        T play(String play);
        /**
         * Number of most common moves to display<br>
         * Default 12
         */
        T moves(int moves);
    }

    interface CommonLichessOpeningExplorer<T> {
        /**
         * Include only games from this month or later<br>
         * Default "0000-01"
         */
        T since(String since);
        /**
         * Include only games from this month or earlier<br>
         * Default "3000-12"
         */
        T until(String until);
        /**
         * Number of recent games to display {@code <= 8}<br>
         * Default 4
         */
        T recentGames(int recentGames);
        /**
         * Variant
         */
        T variant(VariantName variant);

        /**
         * One or more game speeds to look for
         */
        T speeds(Set<Speed> speeds);

        /**
         * Variant
         */
        default T variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

        /**
         * One or more game speeds to look for
         */
        default T speeds(Speed... speeds) { return speeds(Set.of(speeds)); }
    }

    interface MastersBuilder extends CommonOpeningExplorer<MastersBuilder> {
        /**
         * Include only games from this year or later<br>
         * Default 1952
         */
        MastersBuilder since(int since);
        /**
         * Include only games from this year or earlier
         */
        MastersBuilder until(int until);
        /**
         * Number of top games to display, {@code <= 15}<br>
         * Default 15
         */
        MastersBuilder topGames(int topGames);
    }

    interface LichessBuilder extends CommonOpeningExplorer<LichessBuilder>, CommonLichessOpeningExplorer<LichessBuilder> {
        /**
         * Number of top games to display {@code <= 8}<br>
         * Default 4
         */
        LichessBuilder topGames(int games);

        /**
         * One or more rating groups, ranging from their value to the next higher group
         */
        LichessBuilder ratings(Set<RatingGroup> ratings);
        default LichessBuilder ratings(RatingGroup... ratings) { return ratings(Set.of(ratings)); }

        /**
         *  Specifies a rating group, which includes ratings up to next rating group.<br/>
         *  _1600 indicates ratings between 1600-1800 and
         *  _2500 indicates ratings from 2500 and up.
         */
        enum RatingGroup { _1600, _1800, _2000, _2200, _2500;
            public String asString() {
                return name().substring(1);
            }
            public interface Provider {
                /**
                 * 1600-1800
                 */
                default RatingGroup _1600() { return _1600; }
                /**
                 * 1800-2000
                 */
                default RatingGroup _1800() { return _1800; }
                /**
                 * 2000-2200
                 */
                default RatingGroup _2000() { return _2000; }
                /**
                 * 2200-2500
                 */
                default RatingGroup _2200() { return _2200; }
                /**
                 * 2500-over 9000!
                 */
                default RatingGroup _2500() { return _2500; }
            }
            public static Provider provider() {return new Provider(){};}
        }
    }

    interface PlayerBuilder extends CommonOpeningExplorer<PlayerBuilder>, CommonLichessOpeningExplorer<PlayerBuilder> {
        /**
         * Specify for which color to explore games.<br>
         * Default: white
         */
        PlayerBuilder color(Color color);

        /**
         * The game modes to include
         */
        PlayerBuilder modes(Set<Mode> modes);
        default PlayerBuilder modes(Mode... modes) { return modes(Set.of(modes)); }


        @SuppressWarnings("unchecked")
        default PlayerBuilder modes(Function<Mode.Provider, Mode>... modes) { return modes(Stream.of(modes).map(f -> f.apply(Mode.provider())).collect(Collectors.toSet())); }
        default PlayerBuilder color(Function<Color.Provider, Color> color) { return color(color.apply(Color.provider())); }

        public enum Mode {
            casual, rated;

            public interface Provider {
                default Mode casual() { return casual; }
                default Mode rated() { return rated; }
            }
            public static Provider provider() {return new Provider(){};}
        }

    }
}
