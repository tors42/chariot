package chariot.internal.impl;

import java.util.*;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class UsersAuthImpl extends UsersImpl implements UsersAuth {

    public UsersAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Ack> sendMessageToUser(String userId, String text) {
        return Endpoint.sendMessage.newRequest(request -> request
                .path(userId)
                .body(Map.of("text", text)))
            .process(this);
    }

    @Override
    public One<Ack> followUser(String userId) {
        return Endpoint.followUser.newRequest(request -> request
                .path(userId))
            .process(this);
    }

    @Override
    public One<Ack> unfollowUser(String userId) {
        return Endpoint.unfollowUser.newRequest(request -> request
                .path(userId))
            .process(this);
    }

    @Override
    public Many<String> autocompleteNames(String term, boolean friend) {
        return Endpoint.usersNamesAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "false", "friend", Boolean.toString(friend))))
            .process(this);
    }

    @Override
    public Many<LightUserWithStatus> autocompleteUsers(String term, boolean friend) {
        return Endpoint.usersStatusAutocomplete.newRequest(request -> request
                .query(Map.of("term", term, "object", "true", "friend", Boolean.toString(friend))))
            .process(this);
    }

}
