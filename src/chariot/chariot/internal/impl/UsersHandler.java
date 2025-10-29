package chariot.internal.impl;

import module java.base;
import module chariot;

import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
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

        return switch(result) {
            case Some(var ud) -> One.entry(ud.toUser(trophies, challenge));
            case Fail(int s, String m) -> One.fail(s, m);
        };
    }

    @Override
    public Many<User> byIds(Collection<String> userIds) {
        List<List<String>> batches = userIds.stream()
            .gather(Gatherers.windowFixed(300)).toList();

        Many<User> first = requestBatchUsersByIds(batches.getFirst(), UserData::toUser);

        return switch(first) {
            case Entries(Stream<User> stream) -> Many.entries(Stream.concat(stream,
                        batches.stream().skip(1)
                        .map(batch -> requestBatchUsersByIds(batch, UserData::toUser))
                        .flatMap(Many::stream)));
            case Fail<User> fail -> fail;
        };
    }
}
