package chariot.internal.impl;

import chariot.model.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.UsersApi;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class UsersHandler extends UsersBaseHandler implements UsersApi {

    public UsersHandler(RequestHandler requestHandler) {
        super(requestHandler);
    }

    @Override
    public One<User> byId(String userId) { return byId(userId, p -> p.withTrophies(false)); }

    @Override
    public Many<User> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    @Override
    public One<User> byId(String userId, Consumer<UserParams> params) {
        var parameterMap = MapBuilder.of(UserParams.class)
            .addCustomHandler("withTrophies", (args, map) -> {
                if (args[0] instanceof Boolean b && b.booleanValue()) map.put("trophies", 1);
            })
            .addCustomHandler("withChallengeable", (args, map) -> {
                if (args[0] instanceof Boolean b && b.booleanValue()) map.put("challenge", 1);
            })
            .toMap(params);

        var result = Endpoint.userById.newRequest(request -> request
                .path(userId)
                .query(parameterMap)
                )
            .process(super.requestHandler);

        boolean trophies = parameterMap.containsKey("trophies");
        boolean challenge = parameterMap.containsKey("challenge");

        return result.mapOne(ud -> ud.toUser(trophies, challenge));
    }

    @Override
    public Many<User> byIds(List<String> userIds) {
        var result = Endpoint.usersByIds.newRequest(request -> request
                .body(userIds.stream().collect(Collectors.joining(","))))
            .process(super.requestHandler);
        return result.mapMany(UserData::toUser);
    }

}
