package chariot.api;

import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.internal.Util.MapBuilder;
import chariot.model.*;
import chariot.model.Broadcast.Round;

public interface BroadcastsApiAuth extends BroadcastsApi {

    One<Broadcast> create(Consumer<BroadcastBuilder> params);
    Ack      update(String tourId, Consumer<BroadcastBuilder> params);

    One<MyRound>   createRound(String tourId, Consumer<RoundBuilder> params);
    One<Round>     updateRound(String roundId, Consumer<RoundBuilder> params);

    /**
     * Deletes all games in a round
     */
    Ack      resetRound(String roundId);

    /**
     * Update your broadcast with new PGN. Only for broadcast without a source URL.<br>
     *
     * @param roundId The broadcast round ID (8 characters).
     * @param pgn The PGN. It can contain up to 64 games, separated by a double new line.
     * @return The PGN tags and number of moves which were updated.
     */
    Many<PushResult>   pushPgnByRoundId(String roundId, String pgn);

    /**
     * Stream all broadcast rounds you are a member of.<br>
     *
     * Also includes broadcasts rounds you did not create, but were invited to. Also
     * includes broadcasts rounds where you're a non-writing member. See the
     * {@code writeable} flag in the response. Rounds are ordered by rank, which is roughly
     * chronological, most recent first, slightly pondered with popularity.
     */
    Many<MyRound> myRounds(Consumer<RoundsParameters> params);

    /**
     * See {@link #myRounds(Consumer)}
     */
    default Many<MyRound> myRounds() { return myRounds(__ -> {}); }

    interface BroadcastBuilder {

        /**
         * @param name Name of the broadcast tournament.<br/>
         *             Length must be between 3 and 80 characters.<br/>
         *        Example: Sinquefield Cup
         */
        BroadcastBuilder name(String name);

        /**
         * @param description Optional long description of the broadcast. Markdown is supported.<br/>
         *        Length must be less than 20,000 characters.
         */
        BroadcastBuilder description(String description);

        BroadcastBuilder infoLocation(String location);
        BroadcastBuilder infoStandings(URI standings);
        BroadcastBuilder infoTimeZone(String timeZone);
        default BroadcastBuilder infoStandings(String standings) { return infoStandings(URI.create(standings)); }
        BroadcastBuilder infoWebsite(URI website);
        default BroadcastBuilder infoWebsite(String website) { return infoWebsite(URI.create(website)); }
        BroadcastBuilder infoTimeControl(String timeControl);
        BroadcastBuilder infoTournamentFormat(String format);
        BroadcastBuilder infoFeaturedPlayers(String players);
        default BroadcastBuilder infoFeaturedPlayers(String player1, String player2) {
            return infoFeaturedPlayers(String.join(",", player1, player2));
        };
        default BroadcastBuilder infoFeaturedPlayers(String player1, String player2, String player3) {
            return infoFeaturedPlayers(String.join(",", player1, player2, player3));
        }
        default BroadcastBuilder infoFeaturedPlayers(String player1, String player2, String player3, String player4) {
            return infoFeaturedPlayers(String.join(",", player1, player2, player3, player4));
        }

        BroadcastBuilder infoTimeControlFIDE(FideTC fideTimeControl);
        default BroadcastBuilder infoTimeControlFIDE(Function<FideTC.Provider, FideTC> provider) { return infoTimeControlFIDE(provider.apply(FideTC.provider())); }

        /**
         * Only for admins.
         * @param tier Broadcast tier. [3 4 5]. 3 normal, 4 high, 5 best.
         */
        BroadcastBuilder tier(int tier);

        /**
         * Show player's rating diffs. Default: `true`
         */
        BroadcastBuilder showRatingDiffs(boolean showRatingDiffs);
        default BroadcastBuilder showRatingDiffs() { return showRatingDiffs(true); }

        /**
         * Show players scores based on game results. Default: `true`
         */
        BroadcastBuilder showScores(boolean showScores);
        default BroadcastBuilder showScores() { return showScores(true); }


        /**
         * Show a team leaderboard.
         */
        BroadcastBuilder teamTable(boolean teamTable);

        /**
         * Show a team leaderboard.
         */
        default BroadcastBuilder teamTable() { return teamTable(true); }


        /// Replace player names, ratings and titles.<br>
        ///
        /// When broadcast rounds receive PGN data (pushed or polled),
        /// it is possible to apply player information replacement of the tags in the PGN,
        /// based on the player name tags.  
        /// The replacements are specified in the broadcast with one line per player.  
        /// It is possible to augment the PGN tags with information from their FIDE profile by matching a player name to a FIDE ID,
        /// or by manually specifying values of PGN tags.
        ///
        /// *Format*  
        /// `<player name> / <FIDE ID> / <title> / <rating> / <new name>`  
        ///
        /// The fields are optional and can be left blank.  
        /// Player names ignore case and punctuation, and match all possible combinations of 2 words:  
        /// "Jorge Rick Vito" will match "Jorge Rick", "jorge vito", "Rick, Vito", etc.
        ///
        /// Example:
        /// {@snippet :
        ///   var broadcast = client.broadcasts().create(params -> params
        ///      .name("Broadcast Name")
        ///      .description("Broadcast Description")
        ///      .players("""
        ///          Anna / 14111330
        ///          Art / 7818424 / NM
        ///          Some One / / WGM / 2700 / Numero Uno
        ///          Someone Else / / / / Deuce
        ///          Other Player / / / 1300
        ///          """));
        /// }
        ///
        BroadcastBuilder players(String players);

        /**
         * Assign players to teams.<br>
         * By default the PGN tags WhiteTeam and BlackTeam are used.<br>
         *
         * One line per player, formatted as such:<br>
         * {@code <Team name>;<FIDE ID or Player name>}<br>
         *
         * {@snippet :
         *   var broadcast = client.broadcasts().create(params -> params
         *      .name("Broadcast Name")
         *      .description("Broadcast Description")
         *      .teamTable()
         *      .teams("""
         *          Team Cats;3408230
         *          Team Dogs;Scooby Doo
         *          """));
         * }
         */
        BroadcastBuilder teams(String teams);


        /// Who can view the broadcast.
        ///
        /// public: Default. Anyone can view the broadcast
        /// unlisted: Only people with the link can view the broadcast
        /// private: Only the broadcast owner(s) can view the broadcast
        BroadcastBuilder visibility(Visibility visibility);

        /// Who can view the broadcast.
        ///
        /// public: Default. Anyone can view the broadcast
        /// unlisted: Only people with the link can view the broadcast
        /// private: Only the broadcast owner(s) can view the broadcast
        default BroadcastBuilder visibility(Function<Visibility.Provider, Visibility> provider) {
            return visibility(provider.apply(Visibility.provider()));
        }


        /// Tiebreak short codes  
        ///
        /// Up to 5 entries
        ///
        /// `AOB` `APPO` `APRO` `ARO` `ARO-C1` `ARO-C2` `ARO-M1` `ARO-M2`  
        /// `BH` `BH-C1` `BH-C2` `BH-M1` `BH-M2` `BPG` `BWG`  
        /// `DE` `FB` `FB-C1` `FB-C2` `FB-M1` `FB-M2` `KS`  
        /// `PS` `PS-C1` `PS-C2` `PS-M1` `PS-M2` `PTP`  
        /// `SB` `SB-C1` `SB-C2` `SB-M1` `SB-M2` `TPR` `WON`
        BroadcastBuilder tiebreaks(String... names);

        default BroadcastBuilder tiebreaks(Consumer<TiebreakParameters> parameters) {
            var map = MapBuilder.of(TiebreakParameters.class).toMap(parameters);
            return tiebreaks(map.keySet().stream().map(name -> name.replace('_', '-')).toArray(String[]::new));
        }

    }

    interface RoundBuilder {

        /**
         * @param name Name of the broadcast round. Length must be between 3 and 80 characters.<br/>
         *        Example: Round 1
         */
        public RoundBuilder name(String name);

        /**
         * @param syncUrl URL that Lichess will poll to get updates about the games.<br/>
         *                It must be publicly accessible from the Internet.<br/>
         *                If the syncUrl is missing, then the broadcast needs to be fed by pushing PGN to it.<br/>
         *        Example: https://myserver.org/myevent/round-10/games.pgn
         */
        RoundBuilder syncUrl(URI syncUrl);

        /**
         * @param syncUrl URL that Lichess will poll to get updates about the games.<br/>
         *                It must be publicly accessible from the Internet.<br/>
         *                If the syncUrl is missing, then the broadcast needs to be fed by pushing PGN to it.<br/>
         *        Example: https://myserver.org/myevent/round-10/games.pgn
         */
        default RoundBuilder syncUrl(String syncUrl) { return syncUrl(URI.create(syncUrl)); }

        /// @param syncUrls URLs that Lichess will poll to get updates about the games.  
        ///                 They must be publicly accessible from the Internet.  
        ///                 Example: https://myserver.org/myevent/round-10/games.pgn
        RoundBuilder syncUrls(List<URI> syncUrls);

        /// See [#syncUrls(List)]
        default RoundBuilder syncUrls(String... syncUrls) { return syncUrls(Arrays.stream(syncUrls).map(URI::create).toList()); }

        /// @param syncIds Lichess game IDs - Up to 64 Lichess game IDs
        RoundBuilder syncIds(List<String> syncIds);

        /// @param syncIds Lichess game IDs - Up to 64 Lichess game IDs
        default RoundBuilder syncIds(String... syncIds) { return syncIds(Arrays.stream(syncIds).toList()); }

        /// @param syncUsers Up to 100 Lichess usernames
        RoundBuilder syncUsers(List<String> syncUsers);

        /// @param syncUsers Up to 100 Lichess usernames
        default RoundBuilder syncUsers(String... syncUsers) { return syncUsers(Arrays.stream(syncUsers).toList()); }

        /// Whether the round is used when calculating players' rating changes. Default `true`
        RoundBuilder rated(boolean rated);

        /// Whether the round is used when calculating players' rating changes. Default `true`
        default RoundBuilder rated() { return rated(true); }

        /// Custom scoring of game results in round
        RoundBuilder customScoringWhiteWin(double points);
        /// Custom scoring of game results in round
        RoundBuilder customScoringBlackWin(double points);
        /// Custom scoring of game results in round
        RoundBuilder customScoringWhiteDraw(double points);
        /// Custom scoring of game results in round
        RoundBuilder customScoringBlackDraw(double points);

        /**
         * @param delay The delay of the broadcast, in seconds
         */
        RoundBuilder delay(long delay);

        /**
         * @param delay The delay duration of the broadcast
         */
        default RoundBuilder delay(Duration delay) { return delay(delay.toSeconds()); }

        /**
         * Only for Admins. Waiting time for each poll, in seconds. Between 2 and 60 seconds.
         * @param period seconds wait between polls
         */
        RoundBuilder period(int period);

        /**
         * Only for Admins. Waiting time for each poll. Between 2 and 60 seconds.
         * @param period duration wait time between polls
         */
         default RoundBuilder period(Duration period) { return period((int) period.toSeconds()); }

        /**
         * @param startsAt Timestamp in milliseconds of broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        RoundBuilder startsAt(long startsAt);

        /**
         * @param startsAt Broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        default RoundBuilder startsAt(ZonedDateTime startsAt) { return startsAt(startsAt.toInstant().toEpochMilli()); }

        /**
         * @param now Broadcast round start, from a given {@code ZonedDateTime.now()} instance.
         */
        default RoundBuilder startsAt(Function<ZonedDateTime, ZonedDateTime> now) { return startsAt(now.apply(ZonedDateTime.now())); }

        /**
         * The start date is unknown, and the round will start automatically when the previous round completes.
         * @param startsAfterPrevious
         */
        RoundBuilder startsAfterPrevious(boolean startsAfterPrevious);

        /**
         * The start date is unknown, and the round will start automatically when the previous round completes.
         */
        default RoundBuilder startsAfterPrevious() { return startsAfterPrevious(true); }
    }

    interface RoundsParameters {
        /**
         * @param nb How many rounds to get.<br>
         *           {@code >= 1}
         *           Example: {@code nb=20}
         */
        RoundsParameters nb(int nb);
    }


    public enum Visibility {
        PUBLIC,
        UNLISTED,
        PRIVATE,
        ;

        static Visibility fromValue(String value) {
            return switch(value) {
                case "public" -> PUBLIC;
                case "unlisted" -> UNLISTED;
                case "private" -> PRIVATE;
                default -> null;
            };
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }

        public interface Provider {
            default Visibility _public() { return PUBLIC; }
            default Visibility _private() { return PRIVATE; }
            default Visibility _unlisted() { return UNLISTED; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    interface TiebreakParameters {
        TiebreakParameters AOB();
        TiebreakParameters APPO();
        TiebreakParameters APRO();
        TiebreakParameters ARO();
        TiebreakParameters ARO_C1();
        TiebreakParameters ARO_C2();
        TiebreakParameters ARO_M1();
        TiebreakParameters ARO_M2();
        TiebreakParameters BH();
        TiebreakParameters BH_C1();
        TiebreakParameters BH_C2();
        TiebreakParameters BH_M1();
        TiebreakParameters BH_M2();
        TiebreakParameters BPG();
        TiebreakParameters BWG();
        TiebreakParameters DE();
        TiebreakParameters FB();
        TiebreakParameters FB_C1();
        TiebreakParameters FB_C2();
        TiebreakParameters FB_M1();
        TiebreakParameters FB_M2();
        TiebreakParameters KS();
        TiebreakParameters PS();
        TiebreakParameters PS_C1();
        TiebreakParameters PS_C2();
        TiebreakParameters PS_M1();
        TiebreakParameters PS_M2();
        TiebreakParameters PTP();
        TiebreakParameters SB();
        TiebreakParameters SB_C1();
        TiebreakParameters SB_C2();
        TiebreakParameters SB_M1();
        TiebreakParameters SB_M2();
        TiebreakParameters TPR();
        TiebreakParameters WON();
    }
}
