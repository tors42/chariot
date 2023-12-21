package chariot.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.*;
import chariot.model.Broadcast.Round;

public interface BroadcastsAuth extends Broadcasts {

    One<Broadcast> create(Consumer<BroadcastBuilder> params);
    One<Void>      update(String tourId, Consumer<BroadcastBuilder> params);

    One<MyRound>   createRound(String tourId, Consumer<RoundBuilder> params);
    One<Round>     updateRound(String roundId, Consumer<RoundBuilder> params);

    /**
     * Update your broadcast with new PGN. Only for broadcast without a source URL.<br>
     *
     * @param roundId The broadcast round ID (8 characters).
     * @param pgn The PGN. It can contain up to 64 games, separated by a double new line.
     * @return The number of new moves accepted.
     */
    One<Integer>   pushPgnByRoundId(String roundId, String pgn);

    /**
     * Stream all broadcast rounds you are a member of.<br>
     *
     * Also includes broadcasts rounds you did not create, but were invited to. Also
     * includes broadcasts rounds where you're a non-writing member. See the
     * {@code writeable} flag in the response. Rounds are ordered by rank, which is roughly
     * chronological, most recent first, slightly pondered with popularity.
     */
    Many<MyRound> myRounds(Consumer<RoundsBuilder> params);

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
         * @param description Short description of the broadcast tournament.<br/>
         *                    Length must be between 3 and 400 characters.<br/>
         *        Example: An 11 round classical tournament featuring the 9 highest rated players in the world. Including
         *                 Carlsen, Caruana, Ding, Aronian, Nakamura and more.
         */
        BroadcastBuilder shortDescription(String description);

        /**
         * @param markup Optional long description of the broadcast. Markdown is supported.<br/>
         *        Length must be less than 20,000 characters.
         */
        BroadcastBuilder longDescription(String markup);

        /**
         * @param tier For Lichess internal usage only. [3 4 5]
         */
        BroadcastBuilder tier(int tier);

        /**
         * Compute and display a simple leaderboard based on game results
         */
        BroadcastBuilder autoLeaderboard(boolean autoLeaderboard);

        /**
         * Compute and display a simple leaderboard based on game results
         */
        default BroadcastBuilder autoLeaderboard() { return autoLeaderboard(true); }

        /**
         * Replace player names, ratings and titles.<br>
         *
         * One line per player, formatted as such:<br>
         * {@code <Original name>;<Replacement name>;<Optional replacement rating>;<Optional replacement title>}<br>
         *
         * {@snippet :
         *   var broadcast = client.broadcasts().create(params -> params
         *      .name("Broadcast Name")
         *      .shortDescription("Short Broadcast Description")
         *      .longDescription("""
         *          A longer description,
         *          with *markdown* support.
         *          """)
         *      .players("""
         *          DrNykterstein;Magnus Carlsen;2863
         *          AnishGiri;Anish Giri;2764;GM
         *          """));
         * }
         */
        BroadcastBuilder players(String players);
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
        RoundBuilder syncUrl(String syncUrl);

        /**
         * @param delay The delay of the broadcast, in seconds
         */
        RoundBuilder delay(long delay);

        /**
         * @param delay The delay duration of the broadcast
         */
        default RoundBuilder delay(Duration delay) { return delay(delay.toSeconds()); }

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


    }

    interface RoundsBuilder {
        /**
         * @param nb How many rounds to get.<br>
         *           {@code >= 1}
         *           Example: {@code nb=20}
         */
        RoundsBuilder nb(int nb);
    }

}
