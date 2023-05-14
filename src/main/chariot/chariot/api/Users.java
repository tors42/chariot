package chariot.api;

import chariot.model.*;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.internal.impl.UsersHandler;

public class Users extends UsersHandler {

    public Users(RequestHandler requestHandler) {
        super(requestHandler);
    }

    /**
     * Get public user data
     *
     * @param userId
     */
    public One<User> byId(String userId) { return byId(userId, p -> p.withTrophies(false)); }


    public Many<User> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    /**
     * Get public user data
     *
     * @param userId
     * @param params
     */
    public One<User> byId(String userId, Consumer<UserParams> params) {
        var result = Endpoint.userById.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(UserParams.class)
                    .addCustomHandler("withTrophies", (args, map) -> {
                        if (args[0] instanceof Boolean b && b.booleanValue()) map.put("trophies", 1);
                    }).toMap(params))
                )
            .process(super.requestHandler);
        return result.mapOne(User.class::cast);

    }

    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    public Many<User> byIds(List<String> userIds) {
        var result = Endpoint.usersByIds.newRequest(request -> request
                .body(userIds.stream().collect(Collectors.joining(","))))
            .process(super.requestHandler);
        return result.mapMany(User.class::cast);
    }


    public interface UserParams {
        /**
         * Whether or not to include any trophies in the result
         */
        UserParams withTrophies(boolean withTrophies);

        default UserParams withTrophies() { return withTrophies(true); }
    }

    public interface CrosstableParams {
        /**
         * Whether or not to include matchup in the result
         */
        CrosstableParams matchup(boolean matchup);
        default CrosstableParams matchup() { return matchup(true); }
    }

    public interface UserStatusParams {
        /**
         * Whether or not to include game IDs in the result
         */
        UserStatusParams withGameIds(boolean withGameIds);
        default UserStatusParams withGameIds() { return withGameIds(true); }
    }

}
