package chariot.internal.impl;

import module java.base;
import module chariot;

import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.internal.Util.MapBuilder;

import chariot.api.UsersApi.CrosstableParams;
import chariot.api.UsersApi.UserStatusParams;
import chariot.model.Enums.PerfType;
import chariot.model.Enums.PerfTypeNoCorr;

/**
 * Access registered users on Lichess.
 */
public abstract class UsersBaseHandler implements UsersApiBase {
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


    public One<Crosstable> crosstable(String userId1, String userId2) { return crosstable(userId1, userId2, _ -> {}); }
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
        return Endpoint.usersTopAll.newRequest(_ -> {})
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
        List<List<String>> batches = userIds.stream()
            .gather(Gatherers.windowFixed(100)).toList();

        Function<List<String>, Many<UserStatus>> fetchBatch = batch ->
            Endpoint.userStatusByIds.newRequest(request -> request
                    .query(MapBuilder.of(UserStatusParams.class)
                        .add("ids", String.join(",", batch)).toMap(consumer)))
            .process(requestHandler);

        Many<UserStatus> first = fetchBatch.apply(batches.getFirst());

        return switch(first) {
            case Entries(Stream<UserStatus> stream) -> Many.entries(Stream.concat(stream,
                        batches.stream().skip(1)
                        .map(fetchBatch)
                        .flatMap(Many::stream)));
            case Fail<UserStatus> fail -> fail;
        };
    }

    /**
     * Read the status of current live streamers<br>
     * This API is very fast and cheap on lichess side.<br>
     * So you can call it quite often (like once every 5 seconds).
     */
    public Many<LiveStreamer> liveStreamers() {
        return Endpoint.liveStreamers.newRequest(_ -> {})
            .process(requestHandler);
    }

    /**
     * Get the leaderboard for a single speed or variant (a.k.a. `perfType`).<br>
     * There is no leaderboard for correspondence or puzzles.
     *
     * @param nb How many users to fetch. Min 1, Max 100.
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

    <T> Many<T> requestBatchUsersByIds(List<String> ids, Function<UserData, T> mapper, Map<String,Object> paramMap) {
        Many<UserData> result = Endpoint.usersByIds.newRequest(request -> request
                .query(paramMap)
                .body(String.join(",", ids)))
            .process(requestHandler);
        return switch(result) {
            case Entries(var stream)  -> Many.entries(stream.map(mapper));
            case Fail(int s, var msg) -> Many.fail(s, msg);
        };
    }

}
