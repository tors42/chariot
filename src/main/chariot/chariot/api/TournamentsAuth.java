package chariot.api;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.api.Builders.Clock;
import chariot.internal.Util;
import chariot.model.Ack;
import chariot.model.Arena;
import chariot.model.Enums.*;
import chariot.model.Result;
import chariot.model.Swiss;

public interface TournamentsAuth extends Tournaments {

    Result<Arena> createArena(Consumer<ArenaBuilder> params);
    Result<Arena> updateArena(String id, Consumer<ArenaBuilder> params);
    Result<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds);
    Result<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds);
    Result<Ack>   terminateArena(String id);
    Result<Swiss> createSwiss(String teamId, Consumer<SwissBuilder> params);
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
    Result<Swiss> updateSwiss(String id, Consumer<SwissBuilder> params);
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


    /**
     * Generate user entry codes based on a tournament entry code, for a set of user ids.<br>
     * This way you can share the user specific entry codes without needing to share the tournament entry code.
     *
     * @param tournamentEntryCode, The tournament entry code.
     * @param userIds A set of user ids for whom you want to generate specific entry codes for.
     * @return A map with user ids mapped to user entry codes.
     */
    default Map<String, String> generateUserEntryCodes(String tournamentEntryCode, Set<String> userIds) {
        return Util.generateUserEntryCodes(tournamentEntryCode, userIds);
    }

    /**
     * Generate a user entry code based on a tournament entry code.<br>
     * This way you can share the user specific entry codes without needing to share the tournament entry code.
     *
     * @param tournamentEntryCode The tournament entry code.
     * @param userId The user id for whom you want to generate a specific entry code.
     * @return A user specific entry code.
     */
    default String generateUserEntryCode(String tournamentEntryCode, String userId) {
        return Util.generateUserEntryCodes(tournamentEntryCode, Set.of(userId)).get(userId);
    }

    interface ArenaBuilder extends Clock<ArenaParams> {}

    interface ArenaParams {

        /**
         * @param name The tournament name. Leave empty to get a random Grandmaster name
         */
        ArenaParams name(String name);

        /**
         * @param minutes How long the tournament lasts, in minutes [ 0 .. 360 ] Default: 100 minutes
         */
        ArenaParams minutes(int minutes);

        /**
         * @param startTime When the tournament starts. Skipping this parameter defaults to in 5 minutes.
         */
        ArenaParams startTime(Function<StartTime.Provider, StartTime> startTime);

        /**
         * @param variant The variant to use in tournament games
         */
        ArenaParams variant(VariantName variant);

        /**
         * @param rated Games are rated and impact players ratings
         */
        ArenaParams rated(boolean rated);

        /**
         * @param position Custom initial position (in FEN) for all games of the tournament. Must be a legal chess position. Only works with standard chess, not variants (except Chess960).
         */
        ArenaParams position(String position);

        /**
         * @param berserkable  Whether the players can use berserk
         */
        ArenaParams berserkable(boolean berserkable);

        /**
         * @param streakable After 2 wins, consecutive wins grant 4 points instead of 2.
         */
        ArenaParams streakable(boolean streakable);

        /**
         * @param hasChat Whether the players can discuss in a chat
         */
        ArenaParams hasChat(boolean hasChat);

        /**
         * @param description Anything you want to tell players about the tournament
         */
        ArenaParams description(String description);

        /**
         * Make the tournament private, and restrict access with a entry code.<br>
         * You can either share this entry code directly with the users who should be able to join,<br>
         * or you could use it to create user-specific entry codes which you can share - see {@link TournamentsAuth#generateUserEntryCodes(String, Set)}.<br>
         * @param entryCode
         */
        ArenaParams entryCode(String entryCode);

        @Deprecated
        default ArenaParams password(String entryCode) { return entryCode(entryCode); }

        /**
         * @param teamBattleByTeam Set the ID of a team you lead to create a team battle. The other teams can be added using the team battle edit endpoint.
         */
        ArenaParams teamBattleByTeam(String teamBattleByTeam);

        /**
         * @param conditionTeam Restrict entry to members of a team. The teamId is the last part of a team URL, e.g. https://lichess.org/team/coders has teamId = coders.
         */
        ArenaParams conditionTeam(String conditionTeam);

        /**
         * @param conditionMinRating Minimum rating to join.
         */
        ArenaParams conditionMinRating(int conditionMinRating);

        /**
         * @param conditionMaxRating Maximum rating to join. Based on best rating reached in the last 7 days.
         */
        ArenaParams conditionMaxRating(int conditionMaxRating);

        /**
         * @param conditionMinRatedGames Minimum number of rated games required to join.
         */
        ArenaParams conditionMinRatedGames(int conditionMinRatedGames);


        sealed interface StartTime {
            interface Provider {
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

            static Provider provider() { return new Provider() {}; }
            record InMinutes(int waitMinutes) implements StartTime {
                public InMinutes {
                    if (waitMinutes < 0 || waitMinutes > 360) throw new RuntimeException("waitMinutes [%d] must be between [ 0 .. 360 ]".formatted(waitMinutes));
                }
            }
            record AtDate(long startDate) implements StartTime {}
        }
    }

    interface SwissBuilder extends Clock<SwissParams> {}

    interface SwissParams {

        /**
         * @param nbRounds Maximum number of rounds to play [ 3 .. 100 ]
         */
        SwissParams nbRounds(int nbRounds);

        /**
         * @param name The tournament name. Leave empty to get a random Grandmaster name.
         */
        SwissParams name(String name);

        SwissParams rated(boolean rated);

        /**
         * Timestamp in milliseconds to start the tournament at a given date and time.
         * By default, it starts 10 minutes after creation.
         */
        SwissParams startsAt(long startsAt);

        /**
         * How long to wait between each round, in seconds.
         * Leave empty for "auto".
         * [ 0 .. 86400 ]
         * Set to 99999999 to manually schedule each round from the tournament UI.
         */
        SwissParams roundInterval(int roundInterval);

        SwissParams variant(VariantName variant);

        /*
         * Anything you want to tell players about the tournament
         */
        SwissParams description(String description);

        /**
         * Make the tournament restricted with a entry code.
         * @param entryCode
         */
        SwissParams entryCode(String entryCode);

        /**
         * Who can read and write in the chat.
         * Default only team members.
         */
        SwissParams chatFor(ChatFor chatFor);

        /**
         * Usernames of two players that must not play together.
         */
        default SwissParams addForbiddenPairing(String player1, String player2) { return addForbiddenPairings(Set.of(new ForbiddenPairing(player1, player2))); }

        /**
         * Usernames of two players that must not play together.
         */
        default SwissParams addForbiddenPairing(ForbiddenPairing forbiddenPairing) { return addForbiddenPairings(Set.of(forbiddenPairing)); }

        /**
         * Set of usernames of players that must not play together.
         */
        SwissParams addForbiddenPairings(Collection<ForbiddenPairing> forbiddenPairings);

        default SwissParams chatFor(Function<ChatFor.Provider, ChatFor> chatFor) { return chatFor(chatFor.apply(ChatFor.provider())); };
        default SwissParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

        record ForbiddenPairing(String player1, String player2) {}
    }

}
