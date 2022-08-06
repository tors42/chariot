package chariot.api;

import chariot.model.Ack;

public interface UsersAuth extends Users {

    One<Ack> sendMessageToUser(String userId, String text);

    One<Ack> followUser(String userId);
    One<Ack> unfollowUser(String userId);

}
