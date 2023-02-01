package chariot.internal.impl;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;
import chariot.model.Enums.*;

public class UsersImpl extends Base implements Users {

    public UsersImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<User> byId(String userId, Consumer<UserParams> consumer) {
        return Endpoint.userById.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(UserParams.class)
                    .addCustomHandler("withTrophies", (args, map) -> {
                        if (args[0] instanceof Boolean b && b.booleanValue()) map.put("trophies", 1);
                    }).toMap(consumer))
                )
            .process(this);
    }

    @Override
    public Many<User> byIds(List<String> userIds) {
        return Endpoint.usersByIds.newRequest(request -> request
                .body(userIds.stream().collect(Collectors.joining(","))))
            .process(this);
    }

    @Override
    public One<Crosstable> crosstable(String userId1, String userId2, Consumer<CrosstableParams> consumer) {
        return Endpoint.crosstableByUserIds.newRequest(request -> request
                .path(userId1, userId2)
                .query(MapBuilder.of(CrosstableParams.class).toMap(consumer)))
            .process(this);
    }

    @Override
    public Many<Activity> activityById(String userId) {
        return Endpoint.activityById.newRequest(request -> request
                .path(userId))
            .process(this);
    }

    @Override
    public One<UserTopAll> top10() {
        return Endpoint.usersTopAll.newRequest(request -> {})
                .process(this);
    }

    @Override
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
            .process(this);
    }

    @Override
    public Many<StreamerStatus> liveStreamers() {
        return Endpoint.liveStreamers.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType) {
        return Endpoint.usersLeaderboard.newRequest(request -> request
                .path(nb, perfType.name()))
            .process(this);
    }

    @Override
    public Many<RatingHistory> ratingHistoryById(String userId) {
        return Endpoint.ratingHistoryById.newRequest(request -> request
                .path(userId))
            .process(this);
    }

    @Override
    public One<PerfStat> performanceStatisticsByIdAndType(String userId, PerfType type) {
        return Endpoint.perfStatByIdAndType.newRequest(request -> request
                .path(userId, type.name()))
            .process(this);
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
            .map(ids -> { return Endpoint.userStatusByIds.newRequest(request -> request
                        .query(MapBuilder.of(UserStatusParams.class)
                            .add("ids", ids)
                            .toMap(consumer)
                            ))
                    .process(this); })
            .flatMap(Many::stream);
        return Many.entries(userStatusStream);
    }

    @Override
    public Many<String> autocompleteNames(String term) {
        return Endpoint.usersNamesAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "false")))
            .process(this);
    }

    @Override
    public Many<LightUserWithStatus> autocompleteUsers(String term) {
        return Endpoint.usersStatusAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "true")))
            .process(this);
    }
}
