package chariot.api;

import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.model.Enums.*;
import chariot.model.ExploreResult;
import chariot.model.Game;
import chariot.model.GameImport;
import chariot.model.Result;
import chariot.model.StreamGame;
import chariot.model.StreamMove;
import chariot.model.TVChannels;
import chariot.model.TVFeed;
import chariot.model.TablebaseResult;

/**
 * Access games played on Lichess.
 */
public interface Games {

    /**
     * Download one game.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    Result<Game>            byGameId(String gameId, Consumer<GameParams> params);
    /**
     * Download the ongoing game, or the last game played, of a user.<br>
     * If the game is ongoing, the 3 last moves are omitted.
     */
    Result<Game>            currentByUserId(String userId, Consumer<GameParams> params);
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
    Result<Game>            byUserId(String userId, Consumer<SearchFilter> params);

    /**
     * Download games by IDs.<br>
     * Games are sorted by reverse chronological order (most recent first)<br>
     * 300 IDs can be submitted.<br>
     * Ongoing games have their last 3 moves omitted, after move 5.
     */
    Result<Game>            byGameIds(Set<String> gameIds, Consumer<GameParams> params);

    /**
     * Import a game from PGN.<br>
     * Rate limiting: 200 games per hour for OAuth requests, 100 games per hour for anonymous requests.<br>
     * See {@link GamesAuth#importGame} for authenticated access.<br>
     * To broadcast ongoing games, consider pushing to a broadcast instead. See {@link BroadcastsAuth#pushPgnByRoundId}
     */
    Result<GameImport>      importGame(String pgn);
    /**
     * Fetches the PGN of specified game from Masters OTB database
     * @param gameId Example: "aAbqI4ey"
     */
    Result<String>          openingExplorerMastersOTB(String gameId);
    /**
     * Find Masters games from Opening Explorer
     */
    Result<ExploreResult>   openingExplorerMasters(Consumer<MastersBuilder> params);
    /**
     * Find Lichess games from Opening Explorer
     */
    Result<ExploreResult>   openingExplorerLichess(Consumer<LichessBuilder> params);
    /**
     * Find Player games from Opening Explorer<br>
     *
     */
    Result<ExploreResult>   openingExplorerPlayer(String userId, Consumer<PlayerBuilder> params);
     /**
     * Lookup positions from the Lichess tablebase server.
     */
    Result<TablebaseResult> lookupTablebase(String fen);
    /**
     * Lookup Atomic positions from the Lichess tablebase server.
     */
    Result<TablebaseResult> lookupTablebaseAtomic(String fen);
    /**
     * Lookup Antichess positions from the Lichess tablebase server.
     */
    Result<TablebaseResult> lookupTablebaseAntichess(String fen);
    /**
     * Get a list of ongoing games for a given TV channel.
     */
    Result<Game>            byChannel(Channel channel, Consumer<ChannelFilter> params);

    /**
     * Get basic info about the best games being played for each speed and variant, but also computer games and bot games.
     */
    Result<TVChannels>      tvChannels();
    /**
     * Stream positions and moves of the current TV game.<br>
     * A summary of the game is sent as a first message, and when the featured game changes.
     */
    Result<TVFeed>          tvFeed();

    /**
     * Stream the games played between a list of users, in real time.<br>
     * Only games where both players are part of the list are included.<br>
     * Maximum number of users: 300.
     * @param withCurrentGames whether to include ongoing games or not. Default: true
     * @param userIds
     */
    Result<StreamGame> streamGamesByUserIds(boolean withCurrentGames, Set<String> userIds);

    /**
     * {@link #streamGamesByUserIds(boolean, Set)}
     */
    Result<StreamGame> streamGamesByUserIds(Set<String> userIds);

    /**
     * {@link #streamGamesByUserIds(boolean, Set)}
     */
     default Result<StreamGame> streamGamesByUserIds(boolean withCurrentGames, String... userIds) {
        return streamGamesByUserIds(withCurrentGames, Set.of(userIds));
    }

    /**
     * {@link #streamGamesByUserIds(boolean, Set)}
     */
    default Result<StreamGame> streamGamesByUserIds(String... userIds) {
        return streamGamesByUserIds(Set.of(userIds));
    }

    /**
     * Stream positions and moves of any ongoing game.<br>
     * A description of the game is sent as a first message. Then a message is sent
     * each time a move is played. Finally a description of the game is sent when
     * it finishes, and the stream is closed.<br>
     * After move 5, the stream intentionally remains 3 moves behind the game status,
     * as to prevent cheat bots from using this API.<br>
     * No more than 8 game streams can be opened at the same time from the same IP address.
     */
    Result<StreamMove>      streamMovesByGameId(String gameId);

    /**
     * {@link #byGameId(String, Consumer)}
     */
    default Result<Game> byGameId(String gameId) {
        return byGameId(gameId, __ -> {});
    }

    /**
     * {@link #byUserId(String, Consumer)}
     */
    default Result<Game> byUserId(String userId) {
        return byUserId(userId, __ -> {});
    }

    /**
     * {@link #currentByUserId(String, Consumer)}
     */
    default Result<Game> currentByUserId(String userId) {
        return currentByUserId(userId, __ -> {});
    }

    /**
     * {@link #byChannel(Channel, Consumer)}
     */
    default Result<Game> byChannel(Channel channel) {
        return byChannel(channel, __ -> {});
    }

    /**
     * {@link #byChannel(Channel, Consumer)}
     */
     default Result<Game> byChannel(Function<Channel.Provider, Channel> channel) {
        return byChannel(channel.apply(Channel.provider()), __ -> {});
    }

    /**
     * {@link #byChannel(Channel, Consumer)}
     */
     default Result<Game> byChannel(Function<Channel.Provider, Channel> channel, Consumer<ChannelFilter> params) {
        return byChannel(channel.apply(Channel.provider()), params);
    }

    /**
     * {@link #byGameIds(Set, Consumer)}
     */
    default Result<Game> byGameIds(String ... gameIds) {
        return byGameIds(Set.of(gameIds), __ -> {});
    }

    /**
     * {@link #byGameIds(Set, Consumer)}
     */
    default Result<Game> byGameIds(Set<String> gameIds) {
        return byGameIds(gameIds, __ -> {});
    }

    /**
     * {@link #openingExplorerMasters(Consumer)}
     */
    default Result<ExploreResult> openingExplorerMasters() {
        return openingExplorerMasters(__ -> {});
    }

    /**
     * {@link #openingExplorerLichess(Consumer)}
     */
    default Result<ExploreResult> openingExplorerLichess() {
        return openingExplorerLichess(__ -> {});
    }

    /**
     * {@link #openingExplorerPlayer(String, Consumer)}
     */
    default Result<ExploreResult> openingExplorerPlayer(String userId) {
        return openingExplorerPlayer(userId, __ -> {});
    }


    interface Filter {
        /**
         * Only games of a particular player. Leave empty to fetch games of all players.
         */
        Filter player(String userId);
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        Filter moves(boolean moves);
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        Filter pgnInJson(boolean pgnInJson);
        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        Filter tags(boolean tags);
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        Filter clocks(boolean clocks);
        /**
         * Include the opening name.<br>
         * Default `true`
         */
        Filter opening(boolean opening);
    }

    interface GameParams {
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        GameParams moves(boolean moves);
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        GameParams pgnInJson(boolean pgnInJson);
        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        GameParams tags(boolean tags);
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        GameParams clocks(boolean clocks);
        /**
         * Include the opening name.<br>
         * Default `true`
         */
        GameParams opening(boolean opening);

        /**
         * Include analysis evaluation comments in the PGN, when available.<br>
         * Default `true`
         */
        GameParams evals(boolean evals);
        /**
         * URL of a text file containing real names and ratings, to replace Lichess usernames and ratings in the PGN.<br>
         * Example: https://gist.githubusercontent.com/ornicar/6bfa91eb61a2dcae7bcd14cce1b2a4eb/raw/768b9f6cc8a8471d2555e47ba40fb0095e5fba37/gistfile1.txt
         */
        GameParams players(URL urlToTextFile);

        /**
         * Insert textual annotations in the PGN about the opening, analysis variations, mistakes, and game termination.<br>
         * Default `false`
         */
        GameParams literate(boolean literate);
    }

    interface SearchFilter {
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        SearchFilter moves(boolean moves);
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        SearchFilter pgnInJson(boolean pgnInJson);
        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        SearchFilter tags(boolean tags);
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        SearchFilter clocks(boolean clocks);
        /**
         * Include the opening name.<br>
         * Default `true`
         */
        SearchFilter opening(boolean opening);

        /**
         * Include analysis evaluation comments in the PGN, when available.<br>
         * Default `true`
         */
        SearchFilter evals(boolean evals);
        /**
         * URL of a text file containing real names and ratings, to replace Lichess usernames and ratings in the PGN.<br>
         * Example: https://gist.githubusercontent.com/ornicar/6bfa91eb61a2dcae7bcd14cce1b2a4eb/raw/768b9f6cc8a8471d2555e47ba40fb0095e5fba37/gistfile1.txt
         */
        SearchFilter players(URL urlToTextFile);

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

        private static long zdtToMillis(ZonedDateTime zdt) { return zdt.toInstant().getEpochSecond() * 1000; }
    }


    interface ChannelFilter {
        /**
         * Include the PGN moves.<br>
         * Default `true`
         */
        ChannelFilter moves(boolean moves);
        /**
         * Include the full PGN within the JSON response, in a pgn field.<br>
         * Default `false`
         */
        ChannelFilter pgnInJson(boolean pgnInJson);
        /**
         * Include the PGN tags.<br>
         * Default `true
         */
        ChannelFilter tags(boolean tags);
        /**
         * Include clock comments in the PGN moves, when available.<br>
         * Default `true`
         */
        ChannelFilter clocks(boolean clocks);
        /**
         * Include the opening name.<br>
         * Default `true`
         */
        ChannelFilter opening(boolean opening);

        /**
         * Number of games to fetch.<br>
         * Default 10
         */
        ChannelFilter nb(int nb);
    }

    interface MastersBuilder {
        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
         */
        MastersBuilder fen(String fen);
        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional moves starting from {@code fen}.<br>
         * Required to find an opening name, if  {@code fen} is not an exact match for a named position.<br>
         * Example: "e2e4,e7e5,c2c4,c7c6,c4e5"
         */
        MastersBuilder play(String play);
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
         * Number of most common moves to display<br>
         * Default 12
         */
        MastersBuilder moves(int moves);
        /**
         * Number of top games to display, {@code <= 15}<br>
         * Default 15
         */
        MastersBuilder topGames(int topGames);
    }


    interface LichessBuilder {
        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2"
         */
        LichessBuilder fen(String fen);
        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional moves starting from {@code fen}.<br>
         * Required to find an opening name, if  {@code fen} is not an exact match for a named position.<br>
         * Example: "e2e4,e7e5,c2c4,c7c6,c4e5"
         */
        LichessBuilder play(String play);
        /**
         * Number of most common moves to display<br>
         * Default 12
         */
        LichessBuilder moves(int moves);
        /**
         * Number of top games to display {@code <= 8}<br>
         * Default 4
         */
        LichessBuilder topGames(int games);
        /**
         * Number of recent games to display {@code <= 8}<br>
         * Default 4
         */
        LichessBuilder recentGames(int recentGames);
        /**
         * Variant
         */
        LichessBuilder variant(VariantName variant);
        /**
         * Include only games from this month or later<br>
         * Default "0000-01"
         */
        LichessBuilder since(String since);
        /**
         * Include only games from this month or earlier<br>
         * Default "3000-12"
         */
        LichessBuilder until(String until);

        /**
         * One or more game speeds to look for
         */
        LichessBuilder speeds(Set<Speed> speeds);
        default LichessBuilder speeds(Speed... speeds) { return speeds(Set.of(speeds)); }

        /**
         * One or more rating groups, ranging from their value to the next higher group
         */
        LichessBuilder ratings(Set<RatingGroup> ratings);
        default LichessBuilder ratings(RatingGroup... ratings) { return ratings(Set.of(ratings)); }

        default LichessBuilder variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

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

    interface PlayerBuilder {
        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2"
         */
        PlayerBuilder fen(String fen);
        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional moves starting from {@code fen}.<br>
         * Required to find an opening name, if  {@code fen} is not an exact match for a named position.<br>
         * Example: "e2e4,e7e5,c2c4,c7c6,c4e5"
         */
        PlayerBuilder play(String play);
        /**
         * Number of most common moves to display<br>
         */
        PlayerBuilder moves(int moves);
        /**
         * Number of recent games to display {@code <= 8}<br>
         * Default 4
         */
        PlayerBuilder recentGames(int recentGames);
        /**
         * Variant
         */
        PlayerBuilder variant(VariantName variant);
        /**
         * Include only games from this month or later<br>
         * Default "0000-01"
         */
        PlayerBuilder since(String since);
        /**
         * Include only games from this month or earlier<br>
         * Default "3000-12"
         */
        PlayerBuilder until(String until);

        /**
         * Specify for which color to explore games.<br>
         * Default: white
         */
        PlayerBuilder color(Color color);


        /**
         * One or more game speeds to look for
         */
        PlayerBuilder speeds(Set<Speed> speeds);

        /**
         * The game modes to include
         */
        PlayerBuilder modes(Set<Mode> modes);
        default PlayerBuilder modes(Mode... modes) { return modes(Set.of(modes)); }


        @SuppressWarnings("unchecked")
        default PlayerBuilder modes(Function<Mode.Provider, Mode>... modes) { return modes(Stream.of(modes).map(f -> f.apply(Mode.provider())).collect(Collectors.toSet())); }
        default PlayerBuilder color(Function<Color.Provider, Color> color) { return color(color.apply(Color.provider())); }
        default PlayerBuilder speeds(Speed... speeds) { return speeds(Set.of(speeds)); }
        default PlayerBuilder variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

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
