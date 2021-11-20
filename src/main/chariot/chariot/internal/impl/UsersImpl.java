package chariot.internal.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Result<UserStatus> statusByIds(Set<String> userIds, boolean withGameIds) {
        if (userIds.size() > 100) {
            return Result.fail("Max 100 user ids");
        }

        var ids = userIds.stream()
            .collect(Collectors.joining(","));

        var request = Endpoint.userStatusByIds.newRequest()
            .query(Map.of("ids", ids, "withGameIds", withGameIds))
            .build();
        return fetchArr(request);
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
