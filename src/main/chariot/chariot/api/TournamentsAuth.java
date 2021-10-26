package chariot.api;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.function.Function;

import chariot.model.Ack;
import chariot.model.Arena;
import chariot.model.Enums.*;
import chariot.model.Result;
import chariot.model.Swiss;

public interface TournamentsAuth extends Tournaments {

    Result<Arena> createArena(Function<ArenaBBuilder, ArenaBuilder> params);
    Result<Arena> updateArena(String id, Function<ArenaBBuilder, ArenaBuilder> params);
    Result<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds);
    Result<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds);
    Result<Ack>   terminateArena(String id);
    Result<Swiss> createSwiss(String teamId, Function<SwissBBuilder, SwissBuilder> params);
    /**
     * Update a Swiss tournament.<br>
     * Be mindful not to make important changes to ongoing tournaments.
     * @param id The tournament ID. Example: hL7vMrFQ
     * @param params Parameters of the tournament
     * <pre>{@code
     *   String token = "..."; // token with scope tournament:write
     *   String swissId = "hL7vMrFQ";
     *   var client = Client.auth(token);
     *   client.tournaments().updateSwiss(swissId, params -> params.clock(300, 30).description("Another description"));
     * }</pre>
     *
     */
    Result<Swiss> updateSwiss(String id, Function<SwissBBuilder, SwissBuilder> params);
    Result<Ack>   terminateSwiss(String swissId);

    Result<Ack>   joinArena(String id);
    Result<Ack>   joinArena(String id, String password);
    Result<Ack>   joinArenaForTeam(String id, String team);
    Result<Ack>   joinArenaForTeam(String id, String team, String password);

    /**
     * Leave a future Arena tournament, or take a break on an ongoing Arena tournament.<br>
     * It's possible to join again later. Points and streaks are preserved.
     * @param id The tournament ID. Example: "hL7vMrFQ"
     */
    Result<Ack>   withdrawArena(String id);

    Result<Ack>   joinSwiss(String id);
    Result<Ack>   joinSwiss(String id, String password);

    interface ArenaBBuilder {
        /**
         * @param clockInitial Clock initial time in minutes
         * @param clockIncrement Clock increment in seconds [ 0 .. 60 ]
         * @param arenaMinutes How long the tournament lasts, in minutes [ 0 .. 360 ]
         */
        public ArenaBuilder clock(ClockInitial clockInitial, int clockIncrement, int arenaMinutes);

        /**
         * {@link chariot.api.TournamentsAuth.ArenaBBuilder#clock}
         */
        public ArenaBuilder clock(Function<ClockInitial.Provider, ClockInitial> clockInitial, int clockIncrement, int arenaMinutes);
    }

    interface ArenaBuilder {

        /**
         * @param name The tournament name. Leave empty to get a random Grandmaster name
         */
        public ArenaBuilder name(String name);

        /**
         * @param startTime When the tournament starts. Skipping this parameter defaults to in 5 minutes.
         */
        public ArenaBuilder startTime(Function<StartTime.Provider, StartTime> startTime);

        /**
         * @param variant The variant to use in tournament games
         */
        public ArenaBuilder variant(VariantName variant);

        /**
         * @param rated Games are rated and impact players ratings
         */
        public ArenaBuilder rated(boolean rated);

        /**
         * @param position Custom initial position (in FEN) for all games of the tournament. Must be a legal chess position. Only works with standard chess, not variants (except Chess960).
         */
        public ArenaBuilder position(String position);

        /**
         * @param berserkable  Whether the players can use berserk
         */
        public ArenaBuilder berserkable(boolean berserkable);

        /**
         * @param streakable After 2 wins, consecutive wins grant 4 points instead of 2.
         */
        public ArenaBuilder streakable(boolean streakable);

        /**
         * @param hasChat Whether the players can discuss in a chat
         */
        public ArenaBuilder hasChat(boolean hasChat);

        /**
         * @param description Anything you want to tell players about the tournament
         */
        public ArenaBuilder description(String description);

        /**
         * @param password Make the tournament private, and restrict access with a password
         */
        public ArenaBuilder password(String password);

        /**
         * @param teamBattleByTeam Set the ID of a team you lead to create a team battle. The other teams can be added using the team battle edit endpoint.
         */
        public ArenaBuilder teamBattleByTeam(String teamBattleByTeam);

        /**
         * @param conditionTeam Restrict entry to members of a team. The teamId is the last part of a team URL, e.g. https://lichess.org/team/coders has teamId = coders.
         */
        public ArenaBuilder conditionTeam(String conditionTeam);

        /**
         * @param conditionMinRating Minimum rating to join.
         */
        public ArenaBuilder conditionMinRating(int conditionMinRating);

        /**
         * @param conditionMaxRating Maximum rating to join. Based on best rating reached in the last 7 days.
         */
        public ArenaBuilder conditionMaxRating(int conditionMaxRating);

        /**
         * @param conditionMinRatedGames Minimum number of rated games required to join.
         */
        public ArenaBuilder conditionMinRatedGames(int conditionMinRatedGames);


        sealed interface StartTime {
            public interface Provider {
                /**
                 * @param waitMinutes How long to wait before starting the tournament, from now, in minutes [ 0 .. 360 ]
                 */
                default StartTime inMinutes(int waitMinutes) { return new InMinutes(waitMinutes); }

                /**
                 * @param startDate Timestamp to start the tournament at a given date and time. Overrides the waitMinutes setting
                 */
                 default StartTime atDate(ZonedDateTime startDate) { return new AtDate(startDate.toEpochSecond()); }
                /**
                 * @param startDate Timestamp to start the tournament at a given date and time. Overrides the waitMinutes setting
                 */
                 default StartTime atDate(long startDate) { return new AtDate(startDate); }
             }

            public static Provider provider() { return new Provider() {}; }
            public record InMinutes(int waitMinutes) implements StartTime {
                public InMinutes {
                    if (waitMinutes < 0 || waitMinutes > 360) throw new RuntimeException("waitMinutes [%d] must be between [ 0 .. 360 ]".formatted(waitMinutes));
                }
            }
            public record AtDate(long startDate) implements StartTime {}
        }
    }

    interface SwissBBuilder {
        /**
         * @param clockInitial Clock initial time in seconds [ 0 .. 3600 ]
         * @param clockIncrement Clock increment in seconds [ 0 .. 600 ]
         */
        SwissBuilder clock(int clockInitial, int clockIncrement);
    }

    interface SwissBuilder {

        /**
         * @param nbRounds Maximum number of rounds to play [ 3 .. 100 ]
         */
        public SwissBuilder nbRounds(int nbRounds);

        /**
         * @param name The tournament name. Leave empty to get a random Grandmaster name.
         */
        public SwissBuilder name(String name);

        public SwissBuilder rated(boolean rated);

        /**
         * Timestamp in milliseconds to start the tournament at a given date and time.
         * By default, it starts 10 minutes after creation.
         */
        public SwissBuilder startsAt(long startsAt);

        /**
         * How long to wait between each round, in seconds.
         * Leave empty for "auto".
         * [ 0 .. 86400 ]
         * Set to 99999999 to manually schedule each round from the tournament UI.
         */
        public SwissBuilder roundInterval(int roundInterval);

        public SwissBuilder variant(VariantName variant);

        public SwissBuilder variant(Function<VariantName.Provider, VariantName> variant);

        /*
         * Anything you want to tell players about the tournament
         */
        public SwissBuilder description(String description);

        /**
         * Who can read and write in the chat.
         * Default only team members.
         */
        public SwissBuilder chatFor(ChatFor chatFor);

        /**
         * Who can read and write in the chat.
         * Default only team members.
         */
        public SwissBuilder chatFor(Function<ChatFor.Provider, ChatFor> chatFor);
    }

}
