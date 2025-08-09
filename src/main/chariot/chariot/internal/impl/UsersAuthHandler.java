package chariot.internal.impl;

import chariot.model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.UsersApiAuth;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class UsersAuthHandler extends UsersBaseHandler implements UsersApiAuth {

    public UsersAuthHandler(RequestHandler requestHandler) {
        super(requestHandler);
    }

    @Override
    public One<UserAuth> byId(String userId) { return byId(userId, p -> p.withTrophies(false)); }


    @Override
    public Many<UserAuth> byIds(String ... userIds) {
        return byIds(List.of(userIds));
    }

    @Override
    public One<UserAuth> byId(String userId, Consumer<UserParams> params) {
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
        boolean challengeable = parameterMap.containsKey("challenge");

        return result.mapOne(ud -> ud.toUserAuth(trophies, challengeable));
    }

    @Override
    public Many<UserAuth> byIds(List<String> userIds) {
        var result = Endpoint.usersByIds.newRequest(request -> request
                .body(userIds.stream().collect(Collectors.joining(","))))
            .process(super.requestHandler);
        return result.mapMany(UserData::toUserAuth);
    }


    @Override
    public Ack sendMessageToUser(String userId, String text) {
        return Endpoint.sendMessage.newRequest(request -> request
                .path(userId)
                .body(Map.of("text", text)))
            .process(super.requestHandler);
    }

    @Override
    public Ack followUser(String userId) {
        return Endpoint.followUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    @Override
    public Ack unfollowUser(String userId) {
        return Endpoint.unfollowUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    @Override
    public Ack blockUser(String userId) {
        return Endpoint.blockUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    @Override
    public Ack unblockUser(String userId) {
        return Endpoint.unblockUser.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }

    @Override
    public Many<String> autocompleteNames(String term, boolean friend) {
        return Endpoint.usersNamesAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "false", "friend", Boolean.toString(friend))))
            .process(super.requestHandler);
    }

    @Override
    public Many<UserStatus> autocompleteUsers(String term, boolean friend) {
        return Endpoint.usersStatusAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "true", "friend", Boolean.toString(friend))))
            .process(super.requestHandler);
    }

    @Override
    public Ack writeNoteAboutUser(String userId, String text) {
        return Endpoint.writeNote.newRequest(request -> request
                .path(userId)
                .body(Map.of("text", text)))
            .process(super.requestHandler);
    }

    @Override
    public Many<Note> readNotesAboutUser(String userId) {
        return Endpoint.readNotes.newRequest(request -> request
                .path(userId))
            .process(super.requestHandler);
    }
}
