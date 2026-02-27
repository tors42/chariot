package chariot.api;

import java.util.*;
import java.util.function.Consumer;

import chariot.api.UsersApi.*;
import chariot.model.*;
import chariot.model.Enums.*;

/**
 * Access registered users on Lichess.
 */
public interface UsersApiBase {

    default Many<UserStatus> statusByIds(Collection<String> userIds) {
        return statusByIds(userIds, _ -> {});
    }

    /**
     * {@link #statusByIds(Collection)}
     * @param withGameIds If set to true, the id of the game the users are playing, if any, will be included. Default: false
     *
     * @deprecated use {@link #statusByIds(Collection, Consumer)}
     */
    @Deprecated
    default Many<UserStatus> statusByIds(Collection<String> userIds, boolean withGameIds) {
        return statusByIds(userIds, p -> p.withGameIds(withGameIds));
    }

    /**
     * {@link #statusByIds(Collection)}
     *
     * @deprecated use {@link #statusByIds(Collection, Consumer)}
     */
    @Deprecated
    default Many<UserStatus> statusByIds(boolean withGameIds, String... userIds) {
        return statusByIds(Set.of(userIds), p -> p.withGameIds(withGameIds));
    }

    /**
     * {@link #statusByIds(Collection)}
     */
    default Many<UserStatus> statusByIds(String... userIds) {
        return statusByIds(Set.of(userIds), _ -> {});
    }

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
     * Read the `online` and `playing` flags of several users.<br/>
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
     * Read the `online` and `playing` flag of a user.
     */
     default One<UserStatus> statusById(String userId, Consumer<UserStatusParams> consumer) {
        Many<UserStatus> many = statusByIds(List.of(userId), consumer);
        if (many instanceof Fail<UserStatus> fail) {
            return fail;
        } else {
            return many.stream().findFirst().map(One::entry).orElse(One.fail(404, "not found (generated)"));
        }
     }

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
     * @param nb How many users to fetch. Min 1, Max 100.
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
