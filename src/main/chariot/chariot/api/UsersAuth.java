package chariot.api;

import chariot.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.Users.UserParams;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.internal.impl.UsersHandler;

public class UsersAuth extends UsersHandler {

    public UsersAuth(RequestHandler requestHandler) {
        super(requestHandler);
    }

    /**
     * Get public user data
     *
     * @param userId
     */
    public One<UserAuth> byId(String userId) { return byId(userId, p -> p.withTrophies(false)); }


    public Many<UserAuth> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    /**
     * Get public user data
     *
     * @param userId
     * @param params
     */
    public One<UserAuth> byId(String userId, Consumer<UserParams> params) {
        var result = Endpoint.userById.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(UserParams.class)
                    .addCustomHandler("withTrophies", (args, map) -> {
                        if (args[0] instanceof Boolean b && b.booleanValue()) map.put("trophies", 1);
                    }).toMap(params))
                )
            .process(super.requestHandler);
        return result.mapOne(UserAuth.class::cast);
    }

    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    public Many<UserAuth> byIds(List<String> userIds) {
        var result = Endpoint.usersByIds.newRequest(request -> request
                .body(userIds.stream().collect(Collectors.joining(","))))
            .process(super.requestHandler);
        return result.mapMany(UserAuth.class::cast);
    }


    public One<Void> sendMessageToUser(String userId, String text) {
        return Endpoint.sendMessage.newRequest(request -> request
                .path(userId)
                .body(Map.of("text", text)))
            .process(super.requestHandler);
    }

    public One<Void> followUser(String userId) {
        return Endpoint.followUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    public One<Void> unfollowUser(String userId) {
        return Endpoint.unfollowUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friend If true, returns followed players matching {@code term} if any, else returns other players.
     */
    public Many<String> autocompleteNames(String term, boolean friend) {
        return Endpoint.usersNamesAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "false", "friend", Boolean.toString(friend))))
            .process(super.requestHandler);
    }


    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friend If true, returns followed players matching {@code term} if any, else returns other players.
     */
    public Many<UserStatus> autocompleteUsers(String term, boolean friend) {
        return Endpoint.usersStatusAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "true", "friend", Boolean.toString(friend))))
            .process(super.requestHandler);
    }

}
