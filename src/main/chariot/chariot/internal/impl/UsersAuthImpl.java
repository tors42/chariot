package chariot.internal.impl;

import chariot.api.UsersAuth;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Ack;
import chariot.model.Result;

public class UsersAuthImpl extends UsersImpl implements UsersAuth {

    public UsersAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Ack> sendMessageToUser(String userId, String text) {
        var request = Endpoint.sendMessage.newRequest()
            .path(userId)
            .post("text=" + text)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> followUser(String userId) {
        var request = Endpoint.followUser.newRequest()
            .path(userId)
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> unfollowUser(String userId) {
        var request = Endpoint.unfollowUser.newRequest()
            .path(userId)
            .post()
            .build();
        return fetchOne(request);
    }

}
