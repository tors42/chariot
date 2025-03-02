package chariot.api;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.*;

import chariot.api.Builders.*;
import chariot.internal.Util;
import chariot.model.*;
import chariot.model.Enums.*;

public interface TournamentsApiAuth extends TournamentsApi {

    One<Arena> createArena(Consumer<ArenaBuilder> params);
    One<Arena> updateArena(String id, Consumer<ArenaBuilder> params);

    One<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds);
    default One<Arena> updateTeamBattle(String id, int nbLeaders, String... teamIds) { return updateTeamBattle(id, nbLeaders, Set.of(teamIds)); }

    One<Void>   terminateArena(String id);

    One<Void> joinArena(String id, Consumer<JoinArenaParams> params);
    default One<Void> joinArena(String id) { return joinArena(id, __ -> {});}

    /**
     * Leave a future Arena tournament, or take a break on an ongoing Arena tournament.<br>
     * It's possible to join again later. Points and streaks are preserved.
     * @param id The tournament ID. Example: "hL7vMrFQ"
     */
    One<Void> withdrawArena(String id);

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


    /**
     * Manually schedule the next round
     * <p>
     * Manually schedule the next round date and time of a Swiss tournament.<br>
     * This sets the {@code roundInterval} field to {@code 99999999}, i.e. manual scheduling.<br>
     * All further rounds will need to be manually scheduled, unless the {@code roundInterval} field is changed back to automatic scheduling.
     * @param id The tournament ID. Example: {@code hL7vMrFQ}
     * @param date The time to start the next round. Example: {@code java.time.ZonedDateTime.now().plusMinutes(5)}
     */
    One<Void> scheduleNextRoundSwiss(String id, ZonedDateTime date);
    /**
     * Manually schedule the next round
     * <p>
     * Manually schedule the next round date and time of a Swiss tournament.<br>
     * This sets the {@code roundInterval} field to {@code 99999999}, i.e. manual scheduling.<br>
     * All further rounds will need to be manually scheduled, unless the {@code roundInterval} field is changed back to automatic scheduling.
     * @param id The tournament ID. Example: {@code hL7vMrFQ}
     * @param date The time to start the next round. Example: {@code now -> now.plusMinutes(5)}
     */
    default One<Void> scheduleNextRoundSwiss(String id, UnaryOperator<ZonedDateTime> date) {
        return scheduleNextRoundSwiss(id, date.apply(ZonedDateTime.now()));
    }

    One<Void> terminateSwiss(String swissId);

    /**
     * Leave a future Swiss tournament, or take a break on an ongoing Swiss tournament. It's possible to join again later. Points are preserved.
     * @param id The tournament ID. Example: "hL7vMrFQ"
     */
    One<Void> withdrawSwiss(String id);


    One<Void> joinSwiss(String id, Consumer<JoinSwissParams> params);
    default One<Void> joinSwiss(String id) { return joinSwiss(id, __ -> {}); }

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
         * Optionally specify a variant. Default value is "standard".
         *
         * Note, to use custom FEN (FromPosition or Chess960 variants), the tournament can not be `rated` and any Chess960 FEN must be a valid Chess960 position.
         * @param variant The variant to play in the tournament
         */
        ArenaParams variant(Variant variant);
        default ArenaParams variant(Function<Variant.Provider, Variant> variant) { return variant(variant.apply(Variant.provider())); }

        /**
         * @param rated Games are rated and impact players ratings
         */
        ArenaParams rated(boolean rated);
        default ArenaParams rated() { return rated(true); }

        /**
         * @param position Custom initial position (in FEN). Variant must be `standard`, `fromPosition`, or `chess960` (if a valid 960 starting position), and the game cannot be rated.
         * @deprecated Use {@link #variant(Variant)} FromPosition or Chess960
         */
        @Deprecated
        default ArenaParams position(String position) { return variant(provider -> provider.standard(position)); }

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
         * @param withChat Whether the players can discuss in a chat
         */
        ArenaParams withChat(boolean withChat);
        default ArenaParams withChat() { return withChat(true); }

        /**
         * @param description Anything you want to tell players about the tournament
         */
        ArenaParams description(String description);

        /**
         * Make the tournament private, and restrict access with a entry code.<br>
         * You can either share this entry code directly with the users who should be able to join,<br>
         * or you could use it to create user-specific entry codes which you can share - see {@link TournamentsApiAuth#generateUserEntryCodes(String, Set)}.<br>
         * @param entryCode
         */
        ArenaParams entryCode(String entryCode);

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
         * @param days Minimum account age, days.<br>
         * Valid values: [ 1, 3, 7, 14, 30, 60, 90, 180, 365, 365 * 2, 365 * 3 ]
         */
        ArenaParams conditionAccountAge(int days);

        /**
         * @param allowList Predefined list of usernames that are allowed to join. If this list is non-empty, then usernames absent from this list will be forbidden to join. Example: {@code List.of("thibault", "german11")}
         */
        ArenaParams allowList(Collection<String> allowList);

        /**
         * @param titled Restrict entry to titled players.
         */
        ArenaParams conditionTitled(boolean titled);
        default ArenaParams conditionTitled() { return conditionTitled(true); }

        /// @param allowed Whether bots are allowed to join the tournament. Default `false`
        ArenaParams conditionBots(boolean allowed);
        default ArenaParams conditionBots() { return conditionBots(true); }

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
                 * @param now Timestamp to start the tournament at a given date and time, from a given {@code ZonedDateTime.now()} instance. Overrides the waitMinutes setting
                 */
                default StartTime atDate(Function<ZonedDateTime, ZonedDateTime> now) { return atDate(now.apply(ZonedDateTime.now())); }

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

    interface SwissBuilder extends ClockBuilder<SwissParams> {}

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
         * @param now Timestamp to start the tournament at a given date and time, from a given {@code ZonedDateTime.now()} instance.
         */
        default SwissParams startsAt(Function<ZonedDateTime, ZonedDateTime> now) { return startsAt(now.apply(ZonedDateTime.now())); }

        /**
         * How long to wait between each round, in seconds.
         * Leave empty for "auto".
         * [ 0 .. 86400 ]
         */
        SwissParams roundInterval(int roundInterval);

        default SwissParams manualRoundScheduling() { return roundInterval(99999999); }

        /**
         * Optionally specify a variant. Default value is "standard".
         *
         * Note, to use custom FEN, the tournament can not be `rated` and the variant must be standard.
         * @param variant The variant to play in the tournament
         */
        SwissParams variant(Variant variant);
        default SwissParams variant(Function<Variant.Provider, Variant> variant) { return variant(variant.apply(Variant.provider())); }

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
         * @param position Custom initial position (in FEN). Variant must be `standard`, `fromPosition`, or `chess960` (if a valid 960 starting position), and the game cannot be rated.
         * @deprecated Use {@link #variant(Variant)} FromPosition or Chess960
         */
        @Deprecated
        default SwissParams position(String position) { return variant(provider -> provider.standard(position)); }


        /**
         * @param allowList Predefined list of usernames that are allowed to join. If this list is non-empty, then usernames absent from this list will be forbidden to join. Example: {@code List.of("thibault", "german11")}
         */
        SwissParams allowList(Collection<String> allowList);

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
         * Manual pairings for the next round. Missing players will be considered absent and get zero points.
         */
        default SwissParams addManualPairing(String player1, String player2) { return addManualPairings(Set.of(new Pairing(player1, player2))); }

        /**
         * Manual pairings for the next round. Missing players will be considered absent and get zero points.
         */
        default SwissParams addManualPairing(Pairing pairing) { return addManualPairings(Set.of(pairing)); }

        /**
         * Manual pairings for the next round. Missing players will be considered absent and get zero points.
         */
        SwissParams addManualPairings(Collection<Pairing> pairings);

        /**
         * Give a bye (1 point) to a player, instead of pairing them with an opponent.
         */
        default SwissParams addManualBye(String player) { return addManualPairing(player, "1"); }

        record Pairing(String player1, String player2) {}

        /**
         * @param conditionMinRating Minimum rating to join. Leave empty to let everyone join the tournament.<br>
         * Valid values: 1000 1100 1200 1300 1400 1500 1600 1700 1800 1900 2000 2100 2200 2300 2400 2500 2600
         */
        SwissParams conditionMinRating(int conditionMinRating);

        /**
         * @param conditionMaxRating Maximum rating to join. Based on best rating reached in the last 7 days. Leave empty to let everyone join the tournament. <br>
         * Valid values: 2200 2100 2000 1900 1800 1700 1600 1500 1400 1300 1200 1100 1000 900 800
         */
        SwissParams conditionMaxRating(int conditionMaxRating);

        /**
         * @param conditionMinRatedGames Minimum number of rated games required to join.<br>
         * Valid values: [ 0 .. 200 ]
         */
        SwissParams conditionMinRatedGames(int conditionMinRatedGames);

        /**
         * To be allowed to join, participants must have played their previous swiss game
         */
        SwissParams conditionPlayYourGames(boolean playYourGames);
        /**
         * To be allowed to join, participants must have played their previous swiss game
         */
        default SwissParams conditionPlayYourGames() { return conditionPlayYourGames(true); }

        /**
         * @param days Minimum account age, days.<br>
         * Valid values: [ 1, 3, 7, 14, 30, 60, 90, 180, 365, 365 * 2, 365 * 3 ]
         */
        SwissParams conditionAccountAge(int days);

        /**
         * @param titled Only titled players can join
         */
        SwissParams conditionTitled(boolean titled);
        default SwissParams conditionTitled() { return conditionTitled(true); }
     }

    interface JoinSwissParams {
        JoinSwissParams entryCode(String entryCode);
    }

}
