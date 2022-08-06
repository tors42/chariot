package chariot.api;

import java.util.*;
import java.util.function.Consumer;

import chariot.model.*;
import chariot.model.Enums.*;

/**
 * Access registered users on Lichess.
 */
public interface Users {

    /**
     * Get public user data
     *
     * @param userId
     */
    default One<User> byId(String userId) { return byId(userId, p -> p.withTrophies(false)); }

    /**
     * Get public user data
     *
     * @param userId
     * @param params
     */
    One<User> byId(String userId, Consumer<UserParams> params);

    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    Many<User> byIds(List<String> userIds);

    default Many<User> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    /**
     * Read rating history of a user, for all performance types. There is at most one entry per day.
     *
     * @param userId
     */
    Many<RatingHistory> ratingHistoryById(String userId);

    /**
     * Read performance statistics of a user, for a single performance.
     *
     * @param userId
     * @param type
     */
    One<PerfStat> performanceStatisticsByIdAndType(String userId, PerfType type);

    /**
     * Get total number of games, and current score, of any two users.<br>
     * If the `matchup` flag is provided, and the users are currently playing, also gets the current match game number and scores.
     */
    One<Crosstable> crosstable(String userId1, String userId2, Consumer<CrosstableParams> params);
    default One<Crosstable> crosstable(String userId1, String userId2) { return crosstable(userId1, userId2, __ -> {}); }
    default One<Crosstable> crosstable(String userId1, String userId2, boolean matchup) { return crosstable(userId1, userId2, p -> p.matchup(matchup)); }

    /**
     * Get the top 10 players for each speed and variant.
     */
    One<UserTopAll> top10();

    /**
     * Get the leaderboard for a single speed or variant (a.k.a. `perfType`).<br>
     * There is no leaderboard for correspondence or puzzles.
     *
     * @param nb How many users to fetch. Min 1, Max 200.
     * @param perfType Which speed or variant leaderboard.
     */
    One<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType);

    /**
     * Read data to generate the activity feed of a user.
     *
     * @param userId
     */
    Many<Activity> activityById(String userId);


    Many<UserStatus> statusByIds(Collection<String> userIds, Consumer<UserStatusParams> params);

    /**
     * Read the `online`, `playing` and `streaming` flags of several users.<br/>
     * This API is very fast and cheap on lichess side,
     * so you can call it quite often (like once every 5 seconds).<br>
     * Use it to track players and know when they're connected on lichess and playing games.<br>
     * Implementation Note, if the set of user ids is larger than 100 user ids,<br>
     * the user ids will be split into groups of 100s and separate requests will be made for each group.<br>
     *
     * @param userIds
     */
    default Many<UserStatus> statusByIds(Collection<String> userIds) {
        return statusByIds(userIds, __ -> {});
    }

    /**
     * {@link #statusByIds(Collection)}
     * @param withGameIds If set to true, the id of the game the users are playing, if any, will be included. Default: false
     */
    default Many<UserStatus> statusByIds(Collection<String> userIds, boolean withGameIds) {
        return statusByIds(userIds, p -> p.withGameIds(withGameIds));
    }

    /**
     * {@link #statusByIds(Collection)}
     */
    default Many<UserStatus> statusByIds(boolean withGameIds, String... userIds) {
        return statusByIds(Set.of(userIds), withGameIds);
    }

    /**
     * {@link #statusByIds(Collection)}
     */
    default Many<UserStatus> statusByIds(String... userIds) {
        return statusByIds(Set.of(userIds));
    }

    /**
     * Read the status of current live streamers<br>
     * This API is very fast and cheap on lichess side.<br>
     * So you can call it quite often (like once every 5 seconds).
     */
    Many<StreamerStatus> liveStreamers();


    interface UserParams {
        /**
         * Whether or not to include any trophies in the result
         */
        UserParams withTrophies(boolean withTrophies);

        default UserParams withTrophies() { return withTrophies(true); }
    }

    interface CrosstableParams {
        /**
         * Whether or not to include matchup in the result
         */
        CrosstableParams matchup(boolean matchup);
        default CrosstableParams matchup() { return matchup(true); }
    }

    interface UserStatusParams {
        /**
         * Whether or not to include game IDs in the result
         */
        UserStatusParams withGameIds(boolean withGameIds);
        default UserStatusParams withGameIds() { return withGameIds(true); }
    }


}
