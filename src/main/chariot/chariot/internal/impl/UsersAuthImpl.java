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
                .post(Map.of("text", text)))
            .process(this);
    }

    @Override
    public One<Ack> followUser(String userId) {
        return Endpoint.followUser.newRequest(request -> request
                .path(userId)
                .post())
            .process(this);
    }

    @Override
    public One<Ack> unfollowUser(String userId) {
        return Endpoint.unfollowUser.newRequest(request -> request
                .path(userId)
                .post())
            .process(this);
    }
}
