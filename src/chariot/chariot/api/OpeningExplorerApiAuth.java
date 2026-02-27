package chariot.api;

import module java.base;
import module chariot;

import chariot.model.Enums.*;

/// Lookup positions from the Lichess opening explorer.
public interface OpeningExplorerApiAuth {

    /**
     * Fetches the PGN of specified game from Masters OTB database
     *
     * @param gameId Example: "aAbqI4ey"
     */
    One<PGN> pgnByMastersGameId(String gameId);

    /**
     * Find Masters games from Opening Explorer<br>
     * <br>
     * Example usage:
     * {@snippet class=OpeningExplorer region=masters }
     */
    One<ExploreResult.OpeningDB> masters(Consumer<MastersBuilder> params);

    /**
     * See {@link #masters(Consumer)}
     */
    default One<ExploreResult.OpeningDB> masters() { return masters(_ -> {}); }

    /**
     * Find Lichess games from Opening Explorer<br>
     * <br>
     * Example usage:
     * {@snippet class=OpeningExplorer region=lichess }
     */
    One<ExploreResult.OpeningDB> lichess(Consumer<LichessBuilder> params);

    /**
     * See {@link #lichess(Consumer)}
     */
    default One<ExploreResult.OpeningDB> lichess() { return lichess(_ -> {}); }

    /**
     * Find Player games from Opening Explorer<br>
     * <br>
     * Example usage:
     * {@snippet class=OpeningExplorer region=player }
     */
    One<ExploreResult.OpeningPlayer> player(String userId, Consumer<PlayerBuilder> params);

    default One<ExploreResult.OpeningPlayer> player(String userId) { return player(userId, _ -> {}); }

    interface CommonOpeningExplorer<T> {
        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0
         *            1"
         */
        T fen(String fen);

        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional
         * moves starting from {@code fen}.<br>
         * Required to find an opening name, if {@code fen} is not an exact match for a
         * named position.<br>
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
         * Default "1952-01"
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
        default T variant(Function<VariantName.Provider, VariantName> variant) {
            return variant(variant.apply(VariantName.provider()));
        }

        /**
         * One or more game speeds to look for
         */
        default T speeds(Speed... speeds) {
            return speeds(Set.of(speeds));
        }
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

    interface LichessBuilder
            extends CommonOpeningExplorer<LichessBuilder>, CommonLichessOpeningExplorer<LichessBuilder> {
        /**
         * Number of top games to display {@code <= 8}<br>
         * Default 4
         */
        LichessBuilder topGames(int games);

        /**
         * One or more rating groups, ranging from their value to the next higher group
         */
        LichessBuilder ratings(Set<RatingGroup> ratings);

        default LichessBuilder ratings(RatingGroup... ratings) {
            return ratings(Set.of(ratings));
        }

        /**
         * Retrieve history<br>
         * Default false
         */
        default LichessBuilder history() { return history(true); }

        /**
         * Retrieve history<br>
         * Default false
         */
        LichessBuilder history(boolean history);
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

        default PlayerBuilder modes(Mode... modes) {
            return modes(Set.of(modes));
        }

        @SuppressWarnings("unchecked")
        default PlayerBuilder modes(Function<Mode.Provider, Mode>... modes) {
            return modes(Stream.of(modes).map(f -> f.apply(Mode.provider())).collect(Collectors.toSet()));
        }

        default PlayerBuilder color(Function<Color.Provider, Color> color) {
            return color(color.apply(Color.provider()));
        }

        public enum Mode {
            casual, rated;

            public interface Provider {
                default Mode casual() {
                    return casual;
                }

                default Mode rated() {
                    return rated;
                }
            }

            public static Provider provider() {
                return new Provider() {
                };
            }
        }
    }

    interface HistoryBuilder {
        /**
         * Variant
         */
        HistoryBuilder variant(VariantName variant);
        /**
         * Variant
         */
        default HistoryBuilder variant(Function<VariantName.Provider, VariantName> variant) {
            return variant(variant.apply(VariantName.provider()));
        }

        /**
         * @param fen FEN of the root position
         *            Example: "rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2"
         */
        HistoryBuilder fen(String fen);

        /**
         * Comma separated sequence of legal moves in UCI notation. Play additional
         * moves starting from {@code fen}.<br>
         * Required to find an opening name, if {@code fen} is not an exact match for a
         * named position.<br>
         * Example: "e2e4,e7e5,c2c4,c7c6,c4e5"
         */
        HistoryBuilder play(String play);

        /**
         * One or more game speeds to look for
         */
        HistoryBuilder speeds(Set<Speed> speeds);

        /**
         * One or more game speeds to look for
         */
        default HistoryBuilder speeds(Speed... speeds) {
            return speeds(Set.of(speeds));
        }

        /**
         * One or more rating groups, ranging from their value to the next higher group
         */
        LichessBuilder ratings(Set<RatingGroup> ratings);

        default LichessBuilder ratings(RatingGroup... ratings) {
            return ratings(Set.of(ratings));
        }

        /**
         * Include only games from this month or later<br>
         * Default "2017-04"
         */
        HistoryBuilder since(String since);

        /**
         * Include only games from this month or earlier<br>
         * Default "3000-12"
         */
        HistoryBuilder until(String until);


    }

}
