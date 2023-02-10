package chariot.api;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.*;

import chariot.api.Builders.*;
import chariot.internal.Util;
import chariot.model.*;
import chariot.model.Enums.*;

public interface TournamentsAuth extends Tournaments {

    One<Arena> createArena(Consumer<ArenaBuilder> params);
    One<Arena> updateArena(String id, Consumer<ArenaBuilder> params);

    One<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds);
    default One<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds) { return updateTeamBattle(id, nbLeaders, Set.of(teamIds)); }

    One<Ack>   terminateArena(String id);

    One<Ack> joinArena(String id, Consumer<JoinArenaParams> params);
    default One<Ack> joinArena(String id) { return joinArena(id, __ -> {});}

    /**
     * Leave a future Arena tournament, or take a break on an ongoing Arena tournament.<br>
     * It's possible to join again later. Points and streaks are preserved.
     * @param id The tournament ID. Example: "hL7vMrFQ"
     */
    One<Ack> withdrawArena(String id);

    One<Swiss> createSwiss(String teamId, Consumer<SwissBuilder> params);

    /**
     * Update a Swiss tournament.<br>
     * Be mindful not to make important changes to ongoing tournaments.
     * @param id The tournament ID. Example: hL7vMrFQ
     * @param params Parameters of the tournament
     * {@snippet :
     *   String token = "..."; // token with scope tournament:write
     *   String swissId = "hL7vMrFQ";
     *   var client = Client.auth(token);
     *   client.tournaments().updateSwiss(swissId, params -> params.clock(300, 30).description("Another description"));
     * }
     *
     */
    One<Swiss> updateSwiss(String id, Consumer<SwissBuilder> params);

    One<Ack> terminateSwiss(String swissId);

    /**
     * Leave a future Swiss tournament, or take a break on an ongoing Swiss tournament. It's possible to join again later. Points are preserved.
     * @param id The tournament ID. Example: "hL7vMrFQ"
     */
    One<Ack> withdrawSwiss(String id);


    One<Ack> joinSwiss(String id, Consumer<JoinSwissParams> params);
    default One<Ack> joinSwiss(String id) { return joinSwiss(id, __ -> {}); }

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

    interface ArenaBuilder extends ClockMinute<ArenaParams> {}

    interface ArenaParams {

        /**
         * @param name The tournament name. Leave empty to get a random Grandmaster name
         */
        ArenaParams name(String name);

        /**
         * @param minutes How long the tournament lasts, in minutes.<br>
         * [ 20, 25, 30, 35, 40, 45, 50, 55, 60, 70, 80, 90, 100, 110, 120, 150, 180, 210, 240, 270, 300, 330, 360, 420, 480, 540, 600, 720 ]<br>
         * Default: 100 minutes
         */
        ArenaParams minutes(int minutes);

        /**
         * @param startTime When the tournament starts. Skipping this parameter defaults to in 5 minutes.
         */
        ArenaParams startTime(Function<StartTime.Provider, StartTime> startTime);

        /**
         * @param startTime When the tournament starts. Skipping this parameter defaults to in 5 minutes.
         */
        default ArenaParams startTime(ZonedDateTime startTime) { return startTime(s -> s.atDate(startTime)); }

        /**
         * @param variant The variant to use in tournament games
         */
        ArenaParams variant(VariantName variant);

        /**
         * @param rated Games are rated and impact players ratings
         */
        ArenaParams rated(boolean rated);
        default ArenaParams rated() { return rated(true); }

        /**
         * @param position Custom initial position (in FEN) for all games of the tournament. Must be a legal chess position. Only works with standard chess, not variants (except Chess960).
         */
        ArenaParams position(String position);

        /**
         * @param berserkable  Whether the players can use berserk
         */
        ArenaParams berserkable(boolean berserkable);
        default ArenaParams berserkable() { return berserkable(true); }

        /**
         * @param streakable After 2 wins, consecutive wins grant 4 points instead of 2.
         */
        ArenaParams streakable(boolean streakable);
        default ArenaParams streakable() { return streakable(true); }

        /**
         * @param hasChat Whether the players can discuss in a chat
         */
        ArenaParams hasChat(boolean hasChat);
        default ArenaParams hasChat() { return hasChat(true); }

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

        /**
         * @param allowList Predefined list of usernames that are allowed to join. If this list is non-empty, then usernames absent from this list will be forbidden to join. Adding {@code %titled} to the list additionally allows any titled player to join. Example: {@code List.of("thibault", "german11", "%titled")}
         */
        ArenaParams allowList(List<String> allowList);


        sealed interface StartTime {
            interface Provider {
                /**
                 * @param waitMinutes How long to wait before starting the tournament, from now, in minutes<br>
                 * [ 1, 2, 3, 5, 10, 15, 20, 30, 45, 60 ]
                 */
                default StartTime inMinutes(int waitMinutes) { return new InMinutes(waitMinutes); }

                /**
                 * @param startDate Timestamp to start the tournament at a given date and time. Overrides the waitMinutes setting
                 */
                default StartTime atDate(ZonedDateTime startDate) { return new AtDate(startDate.toInstant().toEpochMilli()); }
                /**
                 * @param startDate Timestamp to start the tournament at a given date and time. Overrides the waitMinutes setting
                 */
                default StartTime atDate(long startDate) { return new AtDate(startDate); }
             }

            static Provider provider() { return new Provider() {}; }
            record InMinutes(int waitMinutes) implements StartTime {}
            record AtDate(long startDate) implements StartTime {}
        }
    }

    interface JoinArenaParams {
        /** The tournament entry code, if one is required. Can also be a user-specific entry code generated and shared by the organizer. */
        JoinArenaParams entryCode(String entryCode);
        /** The team to join the tournament with, for team battle tournaments */
        JoinArenaParams team(String team);
        /**
         * If the tournament is started, attempt to pair the user, even if they are not connected to the tournament page. <br/>
         * This expires after one minute, to avoid pairing a user who is long gone. You may call "join" again to extend the waiting. <br/>
         * Default: {@code false}
         */
        JoinArenaParams pairMeAsap(boolean pairMeAsap);
        /** {@see #pairMeAsap(boolean)} */
        default JoinArenaParams pairMeAsap() { return pairMeAsap(true); }
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
        default SwissParams rated() { return rated(true); }

        /**
         * Timestamp in milliseconds to start the tournament at a given date and time.
         * By default, it starts 10 minutes after creation.
         */
        SwissParams startsAt(long startsAt);

        /**
         * @param zonedDateTime Timestamp to start the tournament at a given date and time.
         */
        default SwissParams startsAt(ZonedDateTime zonedDateTime) { return startsAt(zonedDateTime.toInstant().toEpochMilli()); }

        /**
         * How long to wait between each round, in seconds.
         * Leave empty for "auto".
         * [ 0 .. 86400 ]
         * Set to 99999999 to manually schedule each round from the tournament UI.
         */
        SwissParams roundInterval(int roundInterval);

        SwissParams variant(VariantName variant);
        default SwissParams variant(Function<VariantName.Provider, VariantName> variant) { return variant(variant.apply(VariantName.provider())); }

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
        default SwissParams chatFor(Function<ChatFor.Provider, ChatFor> chatFor) { return chatFor(chatFor.apply(ChatFor.provider())); };

        /**
         * @param allowList Predefined list of usernames that are allowed to join. If this list is non-empty, then usernames absent from this list will be forbidden to join. Adding {@code %titled} to the list additionally allows any titled player to join. Example: {@code List.of("thibault", "german11", "%titled")}
         */
        SwissParams allowList(List<String> allowList);

        /**
         * Usernames of two players that must not play together.
         */
        default SwissParams addForbiddenPairing(String player1, String player2) { return addForbiddenPairings(Set.of(new Pairing(player1, player2))); }

        /**
         * Usernames of two players that must not play together.
         */
        default SwissParams addForbiddenPairing(Pairing forbiddenPairing) { return addForbiddenPairings(Set.of(forbiddenPairing)); }

        /**
         * Set of usernames of players that must not play together.
         */
        SwissParams addForbiddenPairings(Collection<Pairing> forbiddenPairings);


        /**
         * Manual pairings for the next round. Missing players will be given a bye, which is worth 1 point.
         */
        default SwissParams addManualPairing(String player1, String player2) { return addManualPairings(Set.of(new Pairing(player1, player2))); }

        /**
         * Manual pairings for the next round. Missing players will be given a bye, which is worth 1 point.
         */
        default SwissParams addManualPairing(Pairing forbiddenPairing) { return addManualPairings(Set.of(forbiddenPairing)); }

        /**
         * Manual pairings for the next round. Missing players will be given a bye, which is worth 1 point.
         */
        SwissParams addManualPairings(Collection<Pairing> manualPairings);



        record Pairing(String player1, String player2) {}
    }

    interface JoinSwissParams {
        JoinSwissParams entryCode(String entryCode);
    }

}
