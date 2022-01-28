package chariot.internal.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Activity;
import chariot.model.Crosstable;
import chariot.model.Leaderboard;
import chariot.model.PerfStat;
import chariot.model.Enums.PerfType;
import chariot.model.Enums.PerfTypeNoCorr;
import chariot.model.RatingHistory;
import chariot.model.Result;
import chariot.model.User;
import chariot.model.UserStatus;
import chariot.model.UserTopAll;

public class UsersImpl extends Base implements Internal.Users {

    public UsersImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<User> byId(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Result.zero();
        }
        var request = Endpoint.userById.newRequest()
            .path(userId)
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<User> byIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Result.zero();
        }

        var request = Endpoint.usersByIds.newRequest()
            .post(userIds.stream().collect(Collectors.joining(",")))
            .build();
        return fetchArr(request);
    }

    @Override
    public Result<Crosstable> crosstable(String userId1, String userId2, Optional<Boolean> matchup) {
        var requestBuilder = Endpoint.crosstableByUserIds.newRequest()
            .path(userId1, userId2);
        matchup.ifPresent(v -> requestBuilder.query(Map.of("matchup", v)));
        var request = requestBuilder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Activity[]> activityById(String userId) {
        var request = Endpoint.activityById.newRequest()
            .path(userId)
            .build();
        var a = fetchOne(request);

        return a;
    }

    @Override
    public Result<UserTopAll> top10() {
        var request = Endpoint.usersTopAll.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<UserStatus> statusByIds(Collection<String> userIds, boolean withGameIds) {
        int batchSize = 100;

        if (userIds.size() > batchSize) {
            return autoSplittingStatusByIds(userIds, withGameIds, batchSize);
        }

        var ids = userIds.stream()
            .collect(Collectors.joining(","));

        var request = Endpoint.userStatusByIds.newRequest()
            .query(Map.of("ids", ids, "withGameIds", withGameIds))
            .build();
        return fetchArr(request);
    }

    private Result<UserStatus> autoSplittingStatusByIds(Collection<String> userIds, boolean withGameIds, int batchSize) {
        String[] arr = userIds.toArray(new String[0]);
        int batches = (int) Math.ceil(arr.length / (float)batchSize);
        Stream<UserStatus> userStatus = Stream.iterate(0, batch -> batch + 1)
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
            .flatMap(ids -> {
                var request = Endpoint.userStatusByIds.newRequest()
                    .query(Map.of("ids", ids, "withGameIds", withGameIds))
                    .build();
                var result = fetchArr(request);
                return result.stream();
            });

        return Result.many(userStatus);
    }

    @Override
    public Result<UserStatus> liveStreamers() {
        var request = Endpoint.liveStreamers.newRequest()
            .build();
        return fetchArr(request);
    }

    @Override
    public Result<Leaderboard> leaderboard(int nb, PerfTypeNoCorr perfType) {
        var request = Endpoint.usersLeaderboard.newRequest()
            .path(nb, perfType.name())
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<RatingHistory> ratingHistoryById(String userId) {
        var request = Endpoint.ratingHistoryById.newRequest()
            .path(userId)
            .build();
        return fetchArr(request);
    }

    @Override
    public Result<PerfStat> performanceStatisticsByIdAndType(String userId, PerfType type) {
        var request = Endpoint.perfStatByIdAndType.newRequest()
            .path(userId, type.name())
            .build();
        return fetchOne(request);
    }

}
