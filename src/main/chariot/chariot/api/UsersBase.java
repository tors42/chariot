package chariot.api;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.Users.*;
import chariot.model.*;
import chariot.model.Enums.*;

/**
 * Access registered users on Lichess.
 */
public interface UsersBase {

    Many<UserStatus> statusByIds(Collection<String> userIds);

    /**
     * {@link #statusByIds(Collection)}
     * @param withGameIds If set to true, the id of the game the users are playing, if any, will be included. Default: false
     */
    Many<UserStatus> statusByIds(Collection<String> userIds, boolean withGameIds);

    /**
     * {@link #statusByIds(Collection)}
     */
    Many<UserStatus> statusByIds(boolean withGameIds, String... userIds);

    /**
     * {@link #statusByIds(Collection)}
     */
    Many<UserStatus> statusByIds(String... userIds);

    /**
     * Get total number of games, and current score, of any two users.<br>
     * If the `matchup` flag is provided, and the users are currently playing, also gets the current match game number and scores.
     */
    One<Crosstable> crosstable(String userId1, String userId2, Consumer<CrosstableParams> consumer);

    One<Crosstable> crosstable(String userId1, String userId2);
    One<Crosstable> crosstable(String userId1, String userId2, boolean matchup);


    /**
     * Read data to generate the activity feed of a user.
     *
     * @param userId
     */
    Many<Activity> activityById(String userId);

    /**
     * Get the top 10 players for each speed and variant.
     */
    One<UserTopAll> top10();

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
    Many<UserStatus> statusByIds(Collection<String> userIds, Consumer<UserStatusParams> consumer);

    /**
     * Read the status of current live streamers<br>
     * This API is very fast and cheap on lichess side.<br>
     * So you can call it quite often (like once every 5 seconds).
     */
    Many<LiveStreamer> liveStreamers();

    /**
     * Get the leaderboard for a single speed or variant (a.k.a. `perfType`).<br>
     * There is no leaderboard for correspondence or puzzles.
     *
     * @param nb How many users to fetch. Min 1, Max 200.
     * @param perfType Which speed or variant leaderboard.
     */
    One<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType);

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
    One<PerformanceStatistics> performanceStatisticsByIdAndType(String userId, PerfType type);

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     */
    Many<String> autocompleteNames(String term);

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     */
    Many<UserStatus> autocompleteUsers(String term);

}
