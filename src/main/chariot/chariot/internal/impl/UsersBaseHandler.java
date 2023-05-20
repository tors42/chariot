package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.api.UsersBase;
import chariot.api.Users.*;
import chariot.model.*;
import chariot.model.Enums.*;

/**
 * Access registered users on Lichess.
 */
public abstract class UsersBaseHandler implements UsersBase {
    protected final RequestHandler requestHandler;

    protected UsersBaseHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public static UsersHandler of(RequestHandler rh) {
        return new UsersHandler(rh);
    }

    public static UsersAuthHandler ofAuth(RequestHandler rh) {
        return new UsersAuthHandler(rh);
    }


    public Many<UserStatus> statusByIds(Collection<String> userIds) {
        return statusByIds(userIds, __ -> {});
    }

    /**
     * {@link #statusByIds(Collection)}
     * @param withGameIds If set to true, the id of the game the users are playing, if any, will be included. Default: false
     */
    public Many<UserStatus> statusByIds(Collection<String> userIds, boolean withGameIds) {
        return statusByIds(userIds, p -> p.withGameIds(withGameIds));
    }

    /**
     * {@link #statusByIds(Collection)}
     */
    public Many<UserStatus> statusByIds(boolean withGameIds, String... userIds) {
        return statusByIds(Set.of(userIds), withGameIds);
    }

    /**
     * {@link #statusByIds(Collection)}
     */
    public Many<UserStatus> statusByIds(String... userIds) {
        return statusByIds(Set.of(userIds));
    }

    /**
     * Get total number of games, and current score, of any two users.<br>
     * If the `matchup` flag is provided, and the users are currently playing, also gets the current match game number and scores.
     */
    public One<Crosstable> crosstable(String userId1, String userId2, Consumer<CrosstableParams> consumer) {
        return Endpoint.crosstableByUserIds.newRequest(request -> request
                .path(userId1, userId2)
                .query(MapBuilder.of(CrosstableParams.class).toMap(consumer)))
            .process(requestHandler);
    }


    public One<Crosstable> crosstable(String userId1, String userId2) { return crosstable(userId1, userId2, __ -> {}); }
    public One<Crosstable> crosstable(String userId1, String userId2, boolean matchup) { return crosstable(userId1, userId2, p -> p.matchup(matchup)); }


    /**
     * Read data to generate the activity feed of a user.
     *
     * @param userId
     */
    public Many<Activity> activityById(String userId) {
        return Endpoint.activityById.newRequest(request -> request
                .path(userId))
            .process(requestHandler);
    }

    /**
     * Get the top 10 players for each speed and variant.
     */
    public One<UserTopAll> top10() {
        return Endpoint.usersTopAll.newRequest(request -> {})
            .process(requestHandler);
    }

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
    public Many<UserStatus> statusByIds(Collection<String> userIds, Consumer<UserStatusParams> consumer) {
        int batchSize = 100;

        if (userIds.size() > batchSize) {
            return autoSplittingStatusByIds(userIds, consumer, batchSize);
        }

        return Endpoint.userStatusByIds.newRequest(request -> request
                .query(MapBuilder.of(UserStatusParams.class)
                    .add("ids", userIds.stream()
                        .collect(Collectors.joining(",")))
                    .toMap(consumer)))
            .process(requestHandler);
    }

    /**
     * Read the status of current live streamers<br>
     * This API is very fast and cheap on lichess side.<br>
     * So you can call it quite often (like once every 5 seconds).
     */
    public Many<LiveStreamer> liveStreamers() {
        return Endpoint.liveStreamers.newRequest(request -> {})
            .process(requestHandler);
    }

    /**
     * Get the leaderboard for a single speed or variant (a.k.a. `perfType`).<br>
     * There is no leaderboard for correspondence or puzzles.
     *
     * @param nb How many users to fetch. Min 1, Max 200.
     * @param perfType Which speed or variant leaderboard.
     */

    public One<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType) {
        return Endpoint.usersLeaderboard.newRequest(request -> request
                .path(nb, perfType.name()))
            .process(requestHandler);
    }

    /**
     * Read rating history of a user, for all performance types. There is at most one entry per day.
     *
     * @param userId
     */
    public Many<RatingHistory> ratingHistoryById(String userId) {
        return Endpoint.ratingHistoryById.newRequest(request -> request
                .path(userId))
            .process(requestHandler);
    }

    /**
     * Read performance statistics of a user, for a single performance.
     *
     * @param userId
     * @param type
     */
    public One<PerformanceStatistics> performanceStatisticsByIdAndType(String userId, PerfType type) {
        return Endpoint.perfStatByIdAndType.newRequest(request -> request
                .path(userId, type.name()))
            .process(requestHandler);
    }

    private Many<UserStatus> autoSplittingStatusByIds(Collection<String> userIds, Consumer<UserStatusParams> consumer, int batchSize) {
        String[] arr = userIds.toArray(new String[0]);
        int batches = (int) Math.ceil(arr.length / (float)batchSize);
        Stream<UserStatus> userStatusStream = Stream.iterate(0, batch -> batch + 1)
            .limit(batches)
            .map(batch -> {
                if (batch == batches) {
                    return Arrays.stream(arr, batch * batchSize, batch * batchSize + arr.length % batchSize);
                } else {
                    return Arrays.stream(arr, batch * batchSize, batch * batchSize + batchSize);
                }
            }
            )
            .map(stream -> stream.collect(Collectors.joining(",")))
            .map(ids -> {
                return Endpoint.userStatusByIds.newRequest(request -> request
                        .query(MapBuilder.of(UserStatusParams.class)
                            .add("ids", ids)
                            .toMap(consumer)
                            ))
                    .process(requestHandler);
            })
            .flatMap(Many::stream);
        return Many.entries(userStatusStream);
    }

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     */
    public Many<String> autocompleteNames(String term) {
        return Endpoint.usersNamesAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "false")))
                .process(requestHandler);
    }

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     */
    public Many<UserStatus> autocompleteUsers(String term) {
        return Endpoint.usersStatusAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "true")))
            .process(requestHandler);
    }

}
