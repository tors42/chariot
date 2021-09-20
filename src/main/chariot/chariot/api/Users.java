package chariot.api;

import java.util.List;
import java.util.Set;

import chariot.model.Activity;
import chariot.model.Crosstable;
import chariot.model.Enums.PerfType;
import chariot.model.Enums.PerfTypeNoCorr;
import chariot.model.Leaderboard;
import chariot.model.PerfStat;
import chariot.model.RatingHistory;
import chariot.model.Result;
import chariot.model.User;
import chariot.model.UserStatus;
import chariot.model.UserTopAll;

/**
 * Access registered users on Lichess.
 */
public interface Users {

    /**
     * Get public user data
     *
     * @param userId
     */
    Result<User> byId(String userId);

    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    Result<User> byIds(List<String> userIds);

    default Result<User> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    /**
     * Read rating history of a user, for all performance types. There is at most one entry per day.
     *
     * @param userId
     */
    Result<RatingHistory> ratingHistoryById(String userId);

    /**
     * Read performance statistics of a user, for a single performance.
     *
     * @param userId
     * @param type
     */
    Result<PerfStat> performanceStatisticsByIdAndType(String userId, PerfType type);

    /**
     * Get total number of games, and current score, of any two users.<br>
     * If the `matchup` flag is provided, and the users are currently playing, also gets the current match game number and scores.
     */
    Result<Crosstable> crosstable(String userId1, String userId2, boolean matchup);
    Result<Crosstable> crosstable(String userId1, String userId2);

    /**
     * Get the top 10 players for each speed and variant.
     */
    Result<UserTopAll> top10();

    /**
     * Get the leaderboard for a single speed or variant (a.k.a. `perfType`).<br>
     * There is no leaderboard for correspondence or puzzles.
     *
     * @param nb How many users to fetch. Min 1, Max 200.
     * @param perfType Which speed or variant leaderboard.
     */
    Result<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType);

    /**
     * Read data to generate the activity feed of a user.
     *
     * @param userId
     */
    Result<Activity[]> activityById(String userId);

    /**
     * Read the `online`, `playing` and `streaming` flags of several users.<br/>
     * This API is very fast and cheap on lichess side,
     * so you can call it quite often (like once every 5 seconds).<br>
     * Use it to track players and know when they're connected on lichess and playing games.
     * @param userIds Up to 100 IDs
     */
    Result<UserStatus> statusByIds(Set<String> userIds);

    /**
     * Read the status of current live streamers<br>
     * This API is very fast and cheap on lichess side.<br>
     * So you can call it quite often (like once every 5 seconds).
     */
    Result<UserStatus> liveStreamers();

}
